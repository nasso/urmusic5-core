package io.github.nasso.urmusic.utils;

public interface RGBA32 extends Cloneable {
	public int getRed();
	public int getGreen();
	public int getBlue();
	public int getAlpha();
	public int getRGBA();
	
	public float getRedf();
	public float getGreenf();
	public float getBluef();
	public float getAlphaf();
	
	public RGBA32 clone();
}
