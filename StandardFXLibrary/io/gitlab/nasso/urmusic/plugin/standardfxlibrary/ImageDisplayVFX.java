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
package io.gitlab.nasso.urmusic.plugin.standardfxlibrary;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.gitlab.nasso.urmusic.common.DataUtils;
import io.gitlab.nasso.urmusic.common.event.EffectParamListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.BoundsParam;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.model.project.param.FileParam;
import io.gitlab.nasso.urmusic.model.project.param.FloatParam;
import io.gitlab.nasso.urmusic.model.project.param.KeyFrame;
import io.gitlab.nasso.urmusic.model.project.param.OptionParam;
import io.gitlab.nasso.urmusic.model.renderer.video.NGLUtils;

public class ImageDisplayVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_source = "source";
	private static final String PNAME_bounds = "bounds";
	private static final String PNAME_blendingMode = "blendingMode";
	private static final String PNAME_opacity = "opacity";
	
	private NGLUtils glu = new NGLUtils("image display global", ImageDisplayVFX.class.getClassLoader());
	
	private int prog, quadVAO;
	private int loc_xform, loc_inputTex, loc_imageTex, loc_blending, loc_opacity;
	
	public class ImageDisplayVFXInstance extends TrackEffectInstance implements VideoEffectInstance  {
		private Matrix4f xform = new Matrix4f();
		private Path lastSrc = null;
		private BufferedImage loadedImage = null;
		private int tex;
		
		private boolean textureLoaded = true;
		
		private EffectParamListener<Path> fileListener;
		
		public void setupParameters() {
			FileParam source = new FileParam(PNAME_source);
			source.addEffectParamListener(this.fileListener = new EffectParamListener<Path>() {
				private Vector4f _vec4 = new Vector4f();
				
				public void valueChanged(EffectParam<Path> source, Path newVal) {
					ImageDisplayVFXInstance.this.lastSrc = newVal.toAbsolutePath();
					ImageDisplayVFXInstance.this.reloadImage();
					
					BoundsParam bounds = (BoundsParam) ImageDisplayVFXInstance.this.getParamByID(PNAME_bounds);
					
					int frame = UrmusicController.getFrameCursor();
					this._vec4.set(bounds.getValue(frame));
					this._vec4.z = ImageDisplayVFXInstance.this.loadedImage.getWidth();
					this._vec4.w = ImageDisplayVFXInstance.this.loadedImage.getHeight();
					bounds.setValue(this._vec4, frame);
				}
				
				public void keyFrameAdded(EffectParam<Path> source, KeyFrame<Path> kf) {
					
				}
				
				public void keyFrameRemoved(EffectParam<Path> source, KeyFrame<Path> kf) {
					
				}
			});
			
			this.addParameter(source);
			
			this.addParameter(new BoundsParam(PNAME_bounds, 0, 0, 256, 256, 1, 1, 1, 1, true));
			this.addParameter(new OptionParam(PNAME_blendingMode, 0,
				"srcOver",
				"dstOver",
				"srcIn",
				"dstIn",
				"srcOut",
				"dstOut",
				"srcAtop",
				"dstAtop",
				"copy",
				"add",
				"xor"
			));
			this.addParameter(new FloatParam(PNAME_opacity, 1.0f, 0.01f, 0.0f, 1.0f));
		}
		
		private void reloadImage() {
			this.loadedImage = DataUtils.loadImage(this.lastSrc.toString());
			this.textureLoaded = false;
		}
		
		public void setupVideo(GL3 gl) {
			this.tex = ImageDisplayVFX.this.glu.genTexture(gl);
			
			gl.glBindTexture(GL.GL_TEXTURE_2D, this.tex);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Path src = (Path) args.parameters.get(PNAME_source);
			Vector4fc bounds = (Vector4fc) args.parameters.get(PNAME_bounds);
			int blendingMode = (int) args.parameters.get(PNAME_blendingMode);
			float opacity = (float) args.parameters.get(PNAME_opacity);
			
			if(src == null) {
				args.cancelled = true;
				return;
			}
			
			src = src.toAbsolutePath();
			if(!src.equals(this.lastSrc) && Files.exists(src) && Files.isRegularFile(src)) {
				this.lastSrc = src;
				this.reloadImage();
			}
			
			if(!this.textureLoaded) {
				ImageDisplayVFX.this.glu.loadImageToTexture(gl, this.tex, this.loadedImage, true);
				this.textureLoaded = true;
			}
			
			this.xform.identity();
			this.xform.translate((bounds.x() + bounds.z() / 2f) / args.width * 2f, -(bounds.y() + bounds.w() / 2f) / args.height * 2f, 0.0f);
			this.xform.scale(bounds.z() / args.width, bounds.w() / args.height, 1.0f);
			
			gl.glUseProgram(ImageDisplayVFX.this.prog);
			ImageDisplayVFX.this.glu.uniformMatrix(gl, ImageDisplayVFX.this.loc_xform, this.xform);
			ImageDisplayVFX.this.glu.uniformTexture(gl, ImageDisplayVFX.this.loc_inputTex, args.texInput, 0);
			ImageDisplayVFX.this.glu.uniformTexture(gl, ImageDisplayVFX.this.loc_imageTex, this.tex, 1);
			gl.glUniform1i(ImageDisplayVFX.this.loc_blending, blendingMode);
			gl.glUniform1f(ImageDisplayVFX.this.loc_opacity, opacity);
			
			gl.glBindVertexArray(ImageDisplayVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
			ImageDisplayVFX.this.glu.deleteTexture(gl, this.tex);
			((FileParam) this.getParamByID(PNAME_source)).removeEffectParamListener(this.fileListener);
		}
	}
	
	public TrackEffectInstance instance() {
		return new ImageDisplayVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/image_display/", "main_vert.vs", "main_frag.fs");
		this.loc_xform = gl.glGetUniformLocation(this.prog, "xform");
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_imageTex = gl.glGetUniformLocation(this.prog, "imageTex");
		this.loc_blending = gl.glGetUniformLocation(this.prog, "blendingMode");
		this.loc_opacity = gl.glGetUniformLocation(this.prog, "opacity");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}
	
	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	public void effectMain() {
	}
	
	public String getEffectClassID() {
		return "image_display";
	}
}
