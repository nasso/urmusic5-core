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
package io.gitlab.nasso.urmusic.model.renderer.video;

import java.awt.Component;

import org.joml.Matrix4f;

import com.jogamp.opengl.GLAutoDrawable;

import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.UrmusicModel;

public class GLPreviewer {
	public static enum ViewMode {
		CUSTOM,
		FIT,
		FIT_MAX,
		ORIGINAL
	}
	
	private ViewMode viewMode = ViewMode.FIT;
	
	private float camX = 0.0f, camY = 0.0f, camZoom = 1.0f;
	private Matrix4f _mat4 = new Matrix4f();
	
	Component panel;
	GLRenderer mainRenderer;
	GLPreviewRenderer renderer;
	
	GLPreviewer(GLRenderer glRenderer) {
		this.mainRenderer = glRenderer;
		this.renderer = new GLPreviewRenderer(this);
	}
	
	public int getPreviewTexture() {
		return this.mainRenderer.getLastTextureFor(UrmusicController.getFocusedComposition(), UrmusicController.getFrameCursor());
	}
	
	public Component getPanel() {
		return this.panel;
	}
	
	private float calcScaleFactor(float sw, float sh, float rw, float rh) {
		float s;
		
		if(this.viewMode == ViewMode.FIT_MAX) s = Math.min(Math.min(sw / rw, sh / rh), 1.0f);
		else if(this.viewMode == ViewMode.ORIGINAL) s = 1.0f;
		else s = Math.min(sw / rw, sh / rh);
		
		return s;
	}
	
	public Matrix4f calcPreviewTransform(float sw, float sh) {
		// Calc ratio
		float rw = UrmusicModel.getCurrentProject().getMainComposition().getWidth();
		float rh = UrmusicModel.getCurrentProject().getMainComposition().getHeight();
		float s = this.calcScaleFactor(sw, sh, rw, rh);
		
		float w = rw * s, h = rh * s;
		
		this._mat4.identity();
		this._mat4.scale(w / sw, h / sh, 1.0f);
		this._mat4.translate(-this.camX / w * 2, -this.camY / h * 2, 0.0f);
		this._mat4.scale(this.camZoom);
		
		return this._mat4;
	}
	
	public void updateCamera(float camX, float camY, float camZoom) {
		this.camX = camX;
		this.camY = camY;
		this.camZoom = camZoom;
	}

	public int xPosToUI(float x, float sw, float sh) {
		float rw = UrmusicController.getFocusedComposition().getWidth();
		float rh = UrmusicController.getFocusedComposition().getHeight();
		
		float s = this.calcScaleFactor(sw, sh, rw, rh);
		float w = rw * s;
		float bx = (sw - w) * 0.5f;
		float uix = bx + (x / rw + 0.5f) * w;
		
		uix -= sw * 0.5f;
		uix *= this.camZoom;
		uix += sw * 0.5f;
		uix -= this.camX;
		
		return Math.round(uix);
	}
	
	public int yPosToUI(float y, float sw, float sh) {
		float rw = UrmusicController.getFocusedComposition().getWidth();
		float rh = UrmusicController.getFocusedComposition().getHeight();
		
		float s = this.calcScaleFactor(sw, sh, rw, rh);
		float h = rh * s;
		float by = (sh - h) * 0.5f;
		float uiy = by + (-y / rh + 0.5f) * h;

		uiy -= sh * 0.5f;
		uiy *= this.camZoom;
		uiy += sh * 0.5f;
		uiy -= this.camY;
		
		return Math.round(uiy);
	}
	
	public float xUIToPos(int x, float sw, float sh) {
		float rw = UrmusicController.getFocusedComposition().getWidth();
		float rh = UrmusicController.getFocusedComposition().getHeight();
		
		float s = this.calcScaleFactor(sw, sh, rw, rh);
		float w = rw * s;
		float bx = (sw - w) / 2f;
		float uix = x + this.camX;
		
		uix -= sw * 0.5f;
		uix /= this.camZoom;
		uix += sw * 0.5f;
		
		return ((uix - bx) / w - 0.5f) * rw;
	}
	
	public float yUIToPos(int y, float sw, float sh) {
		float rw = UrmusicController.getFocusedComposition().getWidth();
		float rh = UrmusicController.getFocusedComposition().getHeight();
		
		float s = this.calcScaleFactor(sw, sh, rw, rh);
		float h = rh * s;
		float by = (sh - h) / 2f;
		float uiy = y + this.camY;
		
		uiy -= sh * 0.5f;
		uiy /= this.camZoom;
		uiy += sh * 0.5f;
		
		return -((uiy - by) / h - 0.5f) * rh;
	}

	public ViewMode getViewMode() {
		return this.viewMode;
	}

	public void setViewMode(ViewMode viewMode) {
		if(this.viewMode == viewMode) return;
		
		this.viewMode = viewMode;
	}
	
	public void dispose() {
		if(this.panel != null)
			((GLAutoDrawable) this.panel).destroy();
	}
}
