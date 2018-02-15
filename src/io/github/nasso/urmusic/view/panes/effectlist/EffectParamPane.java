package io.github.nasso.urmusic.view.panes.effectlist;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.event.EffectParamListener;
import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.common.event.FrameCursorListener;
import io.github.nasso.urmusic.common.event.KeyFrameListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.KeyFrame;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;
import io.github.nasso.urmusic.view.panes.effectlist.controls.EffectParamUI;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EffectParamPane extends JPanel implements FrameCursorListener, EffectParamListener, MouseListener, FocusListener<EffectParam<?>>, KeyFrameListener {
	private static final Color PARAM_LINE_COLOR = new Color(0xffffff);
	private static final Color PARAM_LINE_SELECTED_COLOR = new Color(0xeeeeee);

	private EffectParam<?> param;
	private EffectParamUI<?> controlui = null;
	
	private JLabel keyframeIconLabel;
	
	public EffectParamPane(TrackEffectInstance fx, EffectParam<?> param, int i) {
		this.param = param;
		
		this.setBackground(UrmusicModel.getFocusedEffectParameter() == param ? PARAM_LINE_SELECTED_COLOR : PARAM_LINE_COLOR);
		this.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
			BorderFactory.createEmptyBorder(4, 4, 4, 4)
		));
		
		JLabel controlName = new JLabel();
		controlName.setText(UrmusicStrings.getString("effect." + fx.getEffectClass().getEffectClassName() + ".param." + param.getName() + ".name"));
		controlName.setFont(controlName.getFont().deriveFont(Font.PLAIN, 12));
		
		this.keyframeIconLabel = new JLabel(UrmusicUIRes.KEY_FRAME_ICON);
		this.keyframeIconLabel.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()))
					EffectParamPane.this.toggleKeyFrame();
			}
		});
		
		BoxLayout bl = new BoxLayout(this, BoxLayout.X_AXIS);
		this.setLayout(bl);
		
		this.add(this.keyframeIconLabel);
		this.add(Box.createHorizontalStrut(4));
		this.add(controlName);
		this.add(Box.createHorizontalStrut(64));
		this.add(Box.createHorizontalGlue());
		
		this.controlui = EffectParamUI.createParamUI(fx, this.param);
		if(this.controlui != null) this.add(this.controlui);
		
		this.addMouseListener(this);
		this.param.addEffectParamListener(this);
		UrmusicModel.addFrameCursorListener(this);
		UrmusicModel.addEffectParameterFocusListener(this);
		
		for(int j = 0; j < this.param.getKeyFrameCount(); j++) {
			this.param.getKeyFrame(j).addKeyFrameListener(this);
		}
		
		this.update(UrmusicModel.getFrameCursor());
	}
	
	private void toggleKeyFrame() {
		EffectParam param = this.param;
		int frame = UrmusicModel.getFrameCursor();
		
		KeyFrame kf;
		if((kf = param.getKeyFrameAt(frame)) != null)
			param.removeKeyFrame(kf);
		else
			param.addKeyFrame(UrmusicModel.getFrameCursor());
	}
	
	public void focusChanged(EffectParam<?> oldFocus, EffectParam<?> newFocus) {
		SwingUtilities.invokeLater(() -> {
			if(this.param == oldFocus) {
				this.setBackground(PARAM_LINE_COLOR);
				this.repaint();
			}
			
			if(this.param == newFocus) {
				this.setBackground(PARAM_LINE_SELECTED_COLOR);
				this.repaint();
			}
		});
	}
	
	private void update(int frame) {
		if(this.controlui != null) this.controlui.updateControl(frame);
		
		if(this.param.getKeyFrameAt(frame) != null)
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON_RED);
		else if(this.param.getKeyFrameCount() != 0)
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON_BLUE);
		else
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON);
	}
	
	public void focusParam() {
		UrmusicController.focusEffectParameter(this.param);
	}
	
	public void dispose() {
		for(int i = 0; i < this.param.getKeyFrameCount(); i++) {
			this.param.getKeyFrame(i).removeKeyFrameListener(this);
		}
		
		this.param.removeEffectParamListener(this);
		UrmusicModel.removeFrameCursorListener(this);
		UrmusicModel.removeEffectParameterFocusListener(this);
	}
	
	public void frameChanged(int oldPosition, int newPosition) {
		SwingUtilities.invokeLater(() -> this.update(newPosition));
	}

	public void valueChanged(EffectParam source, Object newVal) {
		this.update(UrmusicModel.getFrameCursor());
	}

	public void keyFrameAdded(EffectParam source, KeyFrame kf) {
		kf.addKeyFrameListener(this);
		this.update(UrmusicModel.getFrameCursor());
	}

	public void keyFrameRemoved(EffectParam source, KeyFrame kf) {
		kf.removeKeyFrameListener(this);
		this.update(UrmusicModel.getFrameCursor());
	}
	
	public void valueChanged(KeyFrame source, Object newValue) {
		this.update(UrmusicModel.getFrameCursor());
	}

	public void frameChanged(KeyFrame source, int newFrame) {
		this.update(UrmusicModel.getFrameCursor());
	}

	public void interpChanged(KeyFrame source, EasingFunction newInterp) {
		this.update(UrmusicModel.getFrameCursor());
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
		this.focusParam();
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}

}
