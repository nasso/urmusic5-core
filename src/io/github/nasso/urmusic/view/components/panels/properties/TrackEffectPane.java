package io.github.nasso.urmusic.view.components.panels.properties;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class TrackEffectPane extends JPanel {
	private static final long serialVersionUID = -6215719147611637543L;

	private TrackEffectInstance fx;
	
	private JPanel headerPane;
	private JLabel labelName;
	
	public TrackEffectPane(TrackEffectInstance fx) {
		this.buildUI();
		
		this.setEffectInstance(fx);
	}
	
	private void buildUI() {
		this.setLayout(new BorderLayout());
		
		this.headerPane = new JPanel();
		this.headerPane.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createBevelBorder(BevelBorder.RAISED),
				BorderFactory.createEmptyBorder(2, 2, 2, 2)
			)
		);
		this.labelName = new JLabel();
		this.labelName.setFont(this.labelName.getFont().deriveFont(Font.BOLD, 12));
		this.labelName.setHorizontalAlignment(SwingConstants.LEFT);
		
		BoxLayout headerbl = new BoxLayout(this.headerPane, BoxLayout.X_AXIS);
		this.headerPane.setLayout(headerbl);
		this.headerPane.add(this.labelName);
		
		this.add(this.headerPane, BorderLayout.NORTH);
	}
	
	public void dispose() {
		
	}

	public TrackEffectInstance getEffectInstance() {
		return this.fx;
	}

	public void setEffectInstance(TrackEffectInstance fx) {
		this.fx = fx;
		
		this.labelName.setText(UrmusicStrings.getString("effect." + fx.getEffectClass().getEffectClassName() + ".name"));
	}
}
