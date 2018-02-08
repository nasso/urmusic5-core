package io.github.nasso.urmusic.common;

public class MutableRGBA32 implements RGBA32 {
	private int value = 0x000000FF;
	
	public MutableRGBA32() {
	}
	
	public MutableRGBA32(int rgba) {
		this.setRGBA(rgba);
	}
	
	public MutableRGBA32(int r, int g, int b, int a) {
		this.setRGBA(r, g, b, a);
	}
	
	public MutableRGBA32(float r, float g, float b, float a) {
		this.setRGBAf(r, g, b, a);
	}
	
	public MutableRGBA32(RGBA32 rgba) {
		this.set(rgba);
	}
	
	public void setRed(int val) {
		this.setRGBA(val, this.getGreen(), this.getBlue(), this.getAlpha());
	}
	
	public void setGreen(int val) {
		this.setRGBA(this.getRed(), val, this.getBlue(), this.getAlpha());
	}
	
	public void setBlue(int val) {
		this.setRGBA(this.getRed(), this.getGreen(), val, this.getAlpha());
	}
	
	public void setAlpha(int val) {
		this.setRGBA(this.getRed(), this.getGreen(), this.getBlue(), val);
	}
	
	public void setRedf(float val) {
		this.setRed((int) (val * 0xFF));
	}
	
	public void setGreenf(float val) {
		this.setGreen((int) (val * 0xFF));
	}
	
	public void setBluef(float val) {
		this.setBlue((int) (val * 0xFF));
	}
	
	public void setAlphaf(float val) {
		this.setAlpha((int) (val * 0xFF));
	}

	public void setRGBA(int value) {
		this.value = value;
	}
	
	public void setRGBA(int r, int g, int b, int a) {
		this.setRGBA(
			((r & 0xFF) << 24) |
			((g & 0xFF) << 16) |
			((b & 0xFF) << 8) |
			(a & 0xFF)
		);
	}
	
	public void setRGBAf(float r, float g, float b, float a) {
		this.setRGBA(
			(int) (r * 0xFF),
			(int) (g * 0xFF),
			(int) (b * 0xFF),
			(int) (a * 0xFF)
		);
	}
	
	public void set(RGBA32 rgba) {
		this.setRGBA(rgba.getRGBA());
	}
	
	public void setFade(RGBA32 a, RGBA32 b, float f) {
		this.setRGBA(
			MathUtils.lerp(a.getRed(), b.getRed(), f),
			MathUtils.lerp(a.getGreen(), b.getGreen(), f),
			MathUtils.lerp(a.getBlue(), b.getBlue(), f),
			MathUtils.lerp(a.getAlpha(), b.getAlpha(), f)
		);
	}
	
	public int getRed() {
		return (this.value >> 24) & 0xFF;
	}
	
	public int getGreen() {
		return (this.value >> 16) & 0xFF;
	}
	
	public int getBlue() {
		return (this.value >> 8) & 0xFF;
	}
	
	public int getAlpha() {
		return this.value & 0xFF;
	}
	
	public int getRGBA() {
		return this.value;
	}
	
	public float getRedf() {
		return (float) this.getRed() / 0xFF;
	}
	
	public float getGreenf() {
		return (float) this.getGreen() / 0xFF;
	}
	
	public float getBluef() {
		return (float) this.getBlue() / 0xFF;
	}
	
	public float getAlphaf() {
		return (float) this.getAlpha() / 0xFF;
	}
	
	public RGBA32 clone() {
		return new MutableRGBA32(this);
	}
	
	public String toString() {
		String istr = Integer.toHexString(this.value);
		return "#" + ("00000000" + istr).substring(istr.length());
	}
	
	public boolean equals(Object o) {
		return o instanceof RGBA32 && ((RGBA32) o).getRGBA() == this.getRGBA();
	}
}
