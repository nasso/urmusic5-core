package io.github.nasso.urmusic.view.components.panels.properties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import io.github.nasso.urmusic.model.event.EffectInstanceListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.ControlParam;
import io.github.nasso.urmusic.utils.MathUtils;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;
import io.github.nasso.urmusic.view.layout.VListLayout;

public class TrackEffectPane extends JPanel implements EffectInstanceListener {
	private static final long serialVersionUID = -6215719147611637543L;

	private TrackEffectInstance fx;
	
	private boolean expanded = false;
	
	private JPanel headerPane;
	private JLabel labelExpand;
	private JCheckBox chbxEnabled;
	private JLabel labelName;
	
	private JPanel contentPane;
	
	public TrackEffectPane(TrackEffectInstance fx) {
		this.buildUI();
		
		this.setEffectInstance(fx);
	}
	
	private void buildUI() {
		this.setLayout(new BorderLayout());
		
		MouseListener expandOnClick = new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()))
					TrackEffectPane.this.toggleExpand();
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		};
		
		MouseListener expandOnDoubleClick = new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()) && e.getClickCount() == 2)
					TrackEffectPane.this.toggleExpand();
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		};
		
		this.headerPane = new JPanel();
		this.headerPane.addMouseListener(expandOnDoubleClick);
		this.headerPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		this.labelExpand = new JLabel(UrmusicUIRes.TRI_RIGHT_ICON);
		this.labelExpand.addMouseListener(expandOnClick);
		
		this.chbxEnabled = new JCheckBox();
		this.chbxEnabled.addActionListener((e) -> {
			this.fx.setEnabled(this.chbxEnabled.isSelected());
		});
		
		this.labelName = new JLabel();
		this.labelName.addMouseListener(expandOnDoubleClick);
		this.labelName.setFont(this.labelName.getFont().deriveFont(Font.BOLD, 12));
		this.labelName.setHorizontalAlignment(SwingConstants.LEFT);
		
		BoxLayout headerbl = new BoxLayout(this.headerPane, BoxLayout.X_AXIS);
		this.headerPane.setLayout(headerbl);
		this.headerPane.add(Box.createHorizontalStrut(2));
		this.headerPane.add(this.labelExpand);
		this.headerPane.add(Box.createHorizontalStrut(2));
		this.headerPane.add(this.chbxEnabled);
		this.headerPane.add(Box.createHorizontalStrut(2));
		this.headerPane.add(this.labelName);
		
		this.add(this.headerPane, BorderLayout.NORTH);
		
		this.contentPane = new JPanel(new VListLayout(0));
		this.contentPane.setVisible(false);
		
		this.add(this.contentPane, BorderLayout.CENTER);
	}
	
	public void dispose() {
		for(Component c : this.contentPane.getComponents()) {
			if(c instanceof ControlParamPane)
				((ControlParamPane) c).dispose();
		}
	}

	private void addParam(ControlParam<?> ctrl, int i) {
		this.contentPane.add(new ControlParamPane(this.fx, ctrl, i), i);
	}
	
	private void removeParam(int i) {
		this.contentPane.remove(i);
	}
	
	private void toggleExpand() {
		if(this.expanded) this.minimize();
		else this.expand();
	}
	
	private void expand() {
		this.labelExpand.setIcon(UrmusicUIRes.TRI_DOWN_ICON);
		this.contentPane.setVisible(true);
		
		this.expanded = true;
	}
	
	private void minimize() {
		this.labelExpand.setIcon(UrmusicUIRes.TRI_RIGHT_ICON);
		this.contentPane.setVisible(false);
		
		this.expanded = false;
	}
	
	public TrackEffectInstance getEffectInstance() {
		return this.fx;
	}

	public void setEffectInstance(TrackEffectInstance newFx) {
		if(this.fx == newFx) return;
		
		if(this.fx != null) {
			for(int i = 0; i < this.fx.getParameterCount(); i++) {
				this.removeParam(i);
			}
			
			this.fx.removeEffectInstanceListener(this);
		}
		
		this.fx = newFx;
		
		if(this.fx != null) {
			for(int i = 0; i < this.fx.getParameterCount(); i++) {
				this.addParam(this.fx.getParameter(i), i);
			}
			
			this.fx.addEffectInstanceListener(this);
			
			this.labelName.setText(UrmusicStrings.getString("effect." + this.fx.getEffectClass().getEffectClassName() + ".name"));
			this.chbxEnabled.setSelected(this.fx.isEnabled());
		}
	}

	public void enabledStateChanged(TrackEffectInstance source, boolean isEnabledNow) {
		this.chbxEnabled.setSelected(isEnabledNow);
	}

	public void parameterAdded(TrackEffectInstance source, int i, ControlParam<?> ctrl) {
		SwingUtilities.invokeLater(() -> this.addParam(ctrl, i));
	}

	public void parameterRemoved(TrackEffectInstance source, int i, ControlParam<?> ctrl) {
		SwingUtilities.invokeLater(() -> this.removeParam(i));
	}

	public void dirtyFlagged(TrackEffectInstance source) {
	}
}
