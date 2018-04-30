/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.github.nasso.urmusic.view.panes.script_editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import io.github.nasso.urmusic.common.ScriptRuntimeErrorListener;
import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectScript;
import io.github.nasso.urmusic.view.UrmusicView;
import io.github.nasso.urmusic.view.components.UrmViewPane;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;

public class ScriptEditorView extends UrmViewPane implements FocusListener<TrackEffectInstance>, ScriptRuntimeErrorListener {
	private static final Color ERROR_LINE_HIGHLIGHT_COLOR = new Color(0xffcccc);
	public static final String VIEW_NAME = "scriptEditor";

	private TrackEffectScript script;
	
	private RTextScrollPane scroller;
	private RSyntaxTextArea editor;
	
	private Timer updateTimer;
	
	public ScriptEditorView() {
		this.editor = new RSyntaxTextArea();
		this.scroller = new RTextScrollPane(this.editor);
		
		this.editor.setTabSize(4);
		this.editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		this.editor.setCodeFoldingEnabled(true);
		
		this.editor.addFocusListener(new java.awt.event.FocusListener() {
			public void focusLost(FocusEvent e) {
				UrmusicView.freeKeyEvent();
			}
			
			public void focusGained(FocusEvent e) {
				UrmusicView.blockKeyEvent();
			}
		});
		
		this.editor.addMouseWheelListener(new MouseWheelListener() {
			private RSyntaxTextAreaEditorKit.IncreaseFontSizeAction inc = new RSyntaxTextAreaEditorKit.IncreaseFontSizeAction();
			private RSyntaxTextAreaEditorKit.DecreaseFontSizeAction dec = new RSyntaxTextAreaEditorKit.DecreaseFontSizeAction();
			
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.isControlDown()) {
					Font fnt = ScriptEditorView.this.editor.getFont();
					
					if(e.getPreciseWheelRotation() < 0) {
						if(fnt.getSize() >= 72) return;
						
						this.inc.actionPerformedImpl(null, ScriptEditorView.this.editor);
					} else {
						if(fnt.getSize() <= 8) return;
						
						this.dec.actionPerformedImpl(null, ScriptEditorView.this.editor);
					}
					
					ScriptEditorView.this.scroller.getGutter().setLineNumberFont(ScriptEditorView.this.editor.getFont());
				} else {
					ScriptEditorView.this.scroller.dispatchEvent(e);
				}
			}
		});
		
		this.scroller.getGutter().setLineNumberFont(this.editor.getFont());
		this.scroller.setIconRowHeaderEnabled(true);
		
		this.updateTimer = new Timer(1000, (e) -> {
			this.editor.removeAllLineHighlights();
			this.scroller.getGutter().removeAllTrackingIcons();
			
			UrmusicController.updateScriptSource(this.script, this.editor.getText());
		});

		this.updateTimer.setCoalesce(false);
		this.updateTimer.setRepeats(false);
		
		this.editor.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
			}
			
			public void insertUpdate(DocumentEvent e) {
			}
			
			public void changedUpdate(DocumentEvent e) {
				ScriptEditorView.this.updateTimer.restart();
			}
		});
		
		this.setLayout(new BorderLayout());
		this.add(this.scroller);
		
		if(UrmusicController.getFocusedTrackEffectInstance() != null) {
			this.updateContent(UrmusicController.getFocusedTrackEffectInstance().getScript());
		}
		
		UrmusicController.addTrackEffectInstanceFocusListener(this);
	}
	
	private void updateContent(TrackEffectScript script) {
		if(script != null) {
			if(this.script != null) this.script.removeErrorListener(this);
			
			this.script = script;
			this.script.addErrorListener(this);
			
			this.editor.setText(script.getSource());
			this.editor.discardAllEdits();
		}
	}

	public void focusChanged(TrackEffectInstance oldFocus, TrackEffectInstance newFocus) {
		this.updateContent(newFocus == null ? null : newFocus.getScript());
	}
	
	public void dispose() {
		UrmusicController.removeTrackEffectInstanceFocusListener(this);
	}

	public void onError(String message, int line, int column) {
		if(line <= 0) line = 1;
		
		try {
			this.scroller.getGutter().addLineTrackingIcon(line - 1, UrmusicUIRes.ERROR_ICON, "<html><pre>" + message + "</pre></html>");
			this.editor.addLineHighlight(line - 1, ERROR_LINE_HIGHLIGHT_COLOR);
		} catch(BadLocationException e1) {
			e1.printStackTrace();
		}
	}
}
