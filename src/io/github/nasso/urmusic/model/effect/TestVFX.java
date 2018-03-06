package io.github.nasso.urmusic.model.effect;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.renderer.video.glvg.GLVG;
import io.github.nasso.urmusic.model.renderer.video.glvg.VGLineCap;

public class TestVFX extends TrackEffect implements VideoEffect {
	private class TestVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private Point2DParam pt0 = new Point2DParam("pt0", -250, 20);
		private GLVG vg;
		
		public TestVFXInstance() {
			this.addParameter(this.pt0);
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
		}

		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc pt0 = this.pt0.getValue(args.time);
			
			this.vg.begin(gl, args.width, args.height);
			
			this.vg.setFillColor(0xFF0000FF);
			this.vg.setStrokeColor(0xFF0000FF);
			
			this.vg.setLineWidth(2);
			this.vg.setLineCaps(VGLineCap.ROUND);
			
			this.vg.beginPath();
			this.vg.moveTo(0, 0);
			this.vg.lineTo(pt0.x(), -pt0.y());
			this.vg.stroke();
			
			this.vg.end(args.fboOutput);
		}

		public void disposeVideo(GL3 gl) {
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
	}

	public void globalVideoDispose(GL3 gl) {
	}

	public TrackEffectInstance instance() {
		return new TestVFXInstance();
	}

	public void effectMain() {
	}

	public String getEffectClassName() {
		return "test";
	}
}
