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
		private Point2DParam[] points = new Point2DParam[8];
		private GLVG vg;
		
		public TestVFXInstance() {
			for(int i = 0; i < this.points.length; i++) {
				float p = (float) i / this.points.length;
				
				float cs = (float) (Math.cos(p * Math.PI * 2) * 200);
				float sn = (float) (Math.sin(p * Math.PI * 2) * 200);
				
				this.points[i] = new Point2DParam("pt" + i, cs, sn);
				
				this.addParameter(this.points[i]);
			}
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
			
			this.vg.setFillColor(0x0000FFFF);
			this.vg.setStrokeColor(0xFF0000FF);
			
			this.vg.setLineWidth(8);
			this.vg.setLineCaps(VGLineCap.ROUND);
		}

		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			this.vg.begin(gl, args.width, args.height);

			this.vg.setLineWidth(64);

			this.vg.beginPath();
			for(int i = 0; i < this.points.length; i++) {
				Vector2fc p = this.points[i].getValue(args.time);
				
				this.vg.lineTo(p.x(), -p.y());
			}
			this.vg.closePath();
			// this.vg.fill();
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
