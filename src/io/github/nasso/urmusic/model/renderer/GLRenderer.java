package io.github.nasso.urmusic.model.renderer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

public abstract class GLRenderer implements GLEventListener {
	public final Renderer mainRenderer;
	
	public GLRenderer(Renderer renderer) {
		this.mainRenderer = renderer;
	}
	
	public abstract void init();
	public abstract void display();
	public abstract void dispose();
	
	public abstract void setContext(GL ctx);
	public abstract GL getContext();
	
	protected abstract GLProfile getProfile();
	
	public abstract GLEventListener createPreviewRenderer();

	public void init(GLAutoDrawable drawable) {
		this.setContext(drawable.getGL());
		
		this.init();
	}
	
	public void dispose(GLAutoDrawable drawable) {
		this.setContext(drawable.getGL());

		this.dispose();
	}
	
	public void display(GLAutoDrawable drawable) {
		this.setContext(drawable.getGL());
		
		this.display();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }
}
