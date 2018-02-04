package io.github.nasso.urmusic.view.components.panels.properties;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.ControlParamListener;
import io.github.nasso.urmusic.model.event.FrameCursorListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.ControlParam;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.KeyFrame;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.utils.MathUtils;
import io.github.nasso.urmusic.view.components.panels.properties.controls.ControlParamUI;
import io.github.nasso.urmusic.view.components.panels.properties.controls.FloatParamUI;
import io.github.nasso.urmusic.view.components.panels.properties.controls.RGBA32ParamUI;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ControlParamPane extends JPanel implements FrameCursorListener, ControlParamListener {
	private static final long serialVersionUID = -6007745267301626934L;
	private static final Color PARAM_LINE_A = new Color(0xdddddd), PARAM_LINE_B = new Color(0xeeeeee);

	private ControlParamUI<?> controlui = null;
	
	private JLabel keyframeIconLabel;
	
	public ControlParamPane(TrackEffectInstance fx, ControlParam<?> ctrl, int i) {
		this.setBackground(i % 2 == 0 ? PARAM_LINE_A : PARAM_LINE_B);
		this.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
			BorderFactory.createEmptyBorder(4, 4, 4, 4)
		));
		
		JLabel controlName = new JLabel();
		controlName.setText(UrmusicStrings.getString("effect." + fx.getEffectClass().getEffectClassName() + ".param." + ctrl.getName() + ".name"));
		controlName.setFont(controlName.getFont().deriveFont(Font.PLAIN, 12));
		
		this.keyframeIconLabel = new JLabel(UrmusicUIRes.KEY_FRAME_ICON);
		this.keyframeIconLabel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()))
					ControlParamPane.this.toggleKeyFrame();
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		BoxLayout bl = new BoxLayout(this, BoxLayout.X_AXIS);
		this.setLayout(bl);
		
		this.add(this.keyframeIconLabel);
		this.add(Box.createHorizontalStrut(4));
		this.add(controlName);
		this.add(Box.createHorizontalStrut(64));
		this.add(Box.createHorizontalGlue());
		
		if(ctrl instanceof FloatParam) {
			this.add(this.controlui = new FloatParamUI((FloatParam) ctrl));
		} else if(ctrl instanceof RGBA32Param) {
			this.add(this.controlui = new RGBA32ParamUI((RGBA32Param) ctrl));
		}
		
		ctrl.addControlParamListener(this);
		UrmusicModel.addFrameCursorListener(this);
		
		this.update(UrmusicModel.getFrameCursor());
	}
	
	private void toggleKeyFrame() {
		if(this.controlui != null) {
			ControlParam param = this.controlui.getParam();
			int frame = UrmusicModel.getFrameCursor();
			
			KeyFrame kf;
			if((kf = param.getKeyFrameAt(frame)) != null)
				param.removeKeyFrame(kf);
			else
				param.addKeyFrame(UrmusicModel.getFrameCursor());
		}
	}
	
	private void update(int frame) {
		this.controlui.updateControl(frame);
				
		if(this.controlui.getParam().getKeyFrameAt(frame) != null && this.keyframeIconLabel.getIcon() != UrmusicUIRes.KEY_FRAME_ICON_BLUE)
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON_BLUE);
		else if(this.keyframeIconLabel.getIcon() != UrmusicUIRes.KEY_FRAME_ICON)
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON);
	}
	
	public void dispose() {
		UrmusicModel.removeFrameCursorListener(this);
	}
	
	public void frameChanged(int oldPosition, int newPosition) {
		if(this.controlui != null)
			SwingUtilities.invokeLater(() -> this.update(newPosition));
	}

	public void valueChanged(ControlParam source, Object newVal) {
		this.update(UrmusicModel.getFrameCursor());
	}

	public void keyFrameAdded(ControlParam source, KeyFrame kf) {
		this.update(UrmusicModel.getFrameCursor());
	}

	public void keyFrameRemoved(ControlParam source, KeyFrame kf) {
		this.update(UrmusicModel.getFrameCursor());
	}
}
