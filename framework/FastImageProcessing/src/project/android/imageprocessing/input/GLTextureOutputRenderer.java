package project.android.imageprocessing.input;

import java.util.ArrayList;
import java.util.List;

import project.android.imageprocessing.GLRenderer;
import project.android.imageprocessing.output.GLTextureInputRenderer;


import android.opengl.GLES20;

/**
 * A output producing extension of GLRenderer.  
 * This class produces its output in the form of a texture and then sends that texture to
 * all of the filters or endpoints that have been added as targets to this renderer.
 * @author Chris Batt
 */
public abstract class GLTextureOutputRenderer extends GLRenderer {
	protected int[] frameBuffer;
	protected int[] texture_out;
	protected int[] depthRenderBuffer;

	private int previousWidth;
	private int previousHeight;
	
	protected List<GLTextureInputRenderer> targets;
	
	/**
	 * Creates a GLTextureOutputRenderer which initially has an empty list of targets.
	 */
	public GLTextureOutputRenderer() {
		super();
		targets = new ArrayList<GLTextureInputRenderer>();
	}
	
	protected void sizeChanged() {
		
	}
	
	/* (non-Javadoc)
	 * @see project.android.imageprocessing.GLRenderer#onDrawFrame()
	 */
	@Override
	public void onDrawFrame() {
		if(previousWidth != width || previousHeight != height) {
			sizeChanged();
			initFBO();
		}
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
		
		super.onDrawFrame();
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		
		for(GLTextureInputRenderer target : targets) {
			target.newTextureReady(texture_out[0], this);
		}
	}
	
	private void initFBO() {
		if(frameBuffer != null) {
			GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
			frameBuffer = null;
		}
		if(texture_out != null) {
			GLES20.glDeleteTextures(1, texture_out, 0);
			texture_out = null;
		}
		if(depthRenderBuffer != null) {
			GLES20.glDeleteRenderbuffers(1, depthRenderBuffer, 0);
			depthRenderBuffer = null;
		}
		frameBuffer = new int[1];
		texture_out = new int[1];
		depthRenderBuffer = new int[1];
		GLES20.glGenFramebuffers(1, frameBuffer, 0);
		GLES20.glGenRenderbuffers(1, depthRenderBuffer, 0);
		GLES20.glGenTextures(1, texture_out, 0);
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_out[0]);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, null);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture_out[0], 0);
		
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);
		
		if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException(this+": Failed to set up render buffer");
		}
		
		previousWidth = width;
		previousHeight = height;
	}
	
	/* (non-Javadoc)
	 * @see project.android.imageprocessing.GLRenderer#onSurfaceCreated()
	 */
	@Override
	public void onSurfaceCreated() {
		super.onSurfaceCreated();
	}
	
	/**
	 * Adds the given target to the list of targets that this renderer sends its output to.
	 * @param target
	 * The target which should be added to the list of targets that this renderer sends its output to.
	 */
	public void addTarget(GLTextureInputRenderer target) {
		targets.add(target);
	}
	
	/**
	 * Removes the given target from the list of targets that this renderer sends its output to.
	 * @param target
	 * The target which should be removed from the list of targets that this renderer sends its output to.
	 */
	public void removeTarget(GLTextureInputRenderer target) {
		targets.remove(target);
	}
	
	/**
	 * Returns a list of all the targets that this renderer should send its output to.
	 * @return targets 
	 */
	public List<GLTextureInputRenderer> getTargets() {
		return targets;
	}
	
	/* (non-Javadoc)
	 * @see project.android.imageprocessing.GLRenderer#setRenderSize(int, int)
	 */
	@Override
	public void setRenderSize(int width, int height) {
		super.setRenderSize(width, height);
		previousWidth = 0;
		previousHeight = 0;
	}
}
