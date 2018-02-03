package io.github.nasso.urmusic.view.components.panels.properties;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import io.github.nasso.urmusic.model.event.EffectInstanceListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.ControlParam;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;

public class TrackEffectPane extends JPanel implements EffectInstanceListener {
	private static final long serialVersionUID = -6215719147611637543L;

	private TrackEffectInstance fx;
	
	private JPanel headerPane;
	private JLabel labelExpand;
	private JCheckBox chbxEnabled;
	private JLabel labelName;
	
	public TrackEffectPane(TrackEffectInstance fx) {
		this.buildUI();
		
		this.setEffectInstance(fx);
	}
	
	private void buildUI() {
		this.setLayout(new BorderLayout());
		
		this.headerPane = new JPanel();
		this.headerPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		this.labelExpand = new JLabel(UrmusicUIRes.TRI_RIGHT_ICON);
		
		this.chbxEnabled = new JCheckBox();
		this.chbxEnabled.addActionListener((e) -> {
			this.fx.setEnabled(this.chbxEnabled.isSelected());
		});
		
		this.labelName = new JLabel();
		this.labelName.setFont(this.labelName.getFont().deriveFont(Font.BOLD, 12));
		this.labelName.setHorizontalAlignment(SwingConstants.LEFT);
		
		BoxLayout headerbl = new BoxLayout(this.headerPane, BoxLayout.X_AXIS);
		this.headerPane.setLayout(headerbl);
		this.headerPane.add(this.labelExpand);
		this.headerPane.add(this.chbxEnabled);
		this.headerPane.add(this.labelName);
		
		this.add(this.headerPane, BorderLayout.NORTH);
	}
	
	public void dispose() {
		
	}

	public TrackEffectInstance getEffectInstance() {
		return this.fx;
	}

	public void setEffectInstance(TrackEffectInstance newFx) {
		if(this.fx != null) {
			this.fx.removeEffectInstanceListener(this);
		}
		
		this.fx = newFx;
		
		if(this.fx != null) {
			this.fx.addEffectInstanceListener(this);
			
			this.labelName.setText(UrmusicStrings.getString("effect." + this.fx.getEffectClass().getEffectClassName() + ".name"));
			this.chbxEnabled.setSelected(this.fx.isEnabled());
		}
	}

	public void enabledStateChanged(TrackEffectInstance source, boolean isEnabledNow) {
		this.chbxEnabled.setSelected(isEnabledNow);
	}

	public void parameterAdded(TrackEffectInstance source, String name, ControlParam<?> ctrl) {
	}

	public void parameterRemoved(TrackEffectInstance source, String name, ControlParam<?> ctrl) {
	}

	public void dirtyFlagged(TrackEffectInstance source) {
	}
}
