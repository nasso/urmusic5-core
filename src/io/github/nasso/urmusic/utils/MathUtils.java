package io.github.nasso.urmusic.utils;

import org.joml.Matrix3f;

public class MathUtils {
	public static final float PI = (float) Math.PI;
	public static final float PI_2 = PI * 2.0f;
	public static final float HALF_PI = PI * 0.5f;
	public static final float PI_INV = 1.0f / PI;
	
	private static Matrix3f _mat3 = new Matrix3f();
	
	public static Matrix3f setSkewX(Matrix3f mat, float angle) {
		mat.identity();
		mat.m10((float) Math.tan(angle));
		return mat;
	}
	
	public static Matrix3f setSkewY(Matrix3f mat, float angle) {
		mat.identity();
		mat.m10((float) Math.tan(angle));
		return mat;
	}
	
	public static Matrix3f translation(Matrix3f mat, float x, float y) {
		mat.identity();
		mat.m20(x);
		mat.m21(y);
		return mat;
	}
	
	public static Matrix3f rotation(Matrix3f mat, float a) {
		float cs = (float) Math.cos(a);
		float sn = (float) Math.sin(a);
		
		mat.identity();
		mat.m00(cs);
		mat.m01(sn);
		mat.m10(-sn);
		mat.m11(cs);
		
		return mat;
	}
	
	public static Matrix3f scaling(Matrix3f mat, float x, float y) {
		mat.identity();
		mat.m00(x);
		mat.m11(y);
		return mat;
	}
	
	public static Matrix3f set(Matrix3f mat, float a, float b, float c, float d, float e, float f) {
		mat.identity();
		mat.m00(a);
		mat.m01(b);
		mat.m10(c);
		mat.m11(d);
		mat.m20(e);
		mat.m21(f);
		return mat;
	}
	
	public static Matrix3f skewX(Matrix3f mat, float angle) {
		return mat.mul(setSkewX(_mat3, angle));
	}
	
	public static Matrix3f skewY(Matrix3f mat, float angle) {
		return mat.mul(setSkewY(_mat3, angle));
	}
	
	public static Matrix3f translate(Matrix3f mat, float x, float y) {
		return mat.mul(translation(_mat3, x, y));
	}
	
	public static Matrix3f rotate(Matrix3f mat, float a) {
		return mat.mul(rotation(_mat3, a));
	}
	
	public static Matrix3f scale(Matrix3f mat, float x, float y) {
		return mat.mul(scaling(_mat3, x, y));
	}
	
	public static Matrix3f apply(Matrix3f mat, float a, float b, float c, float d, float e, float f) {
		return mat.mul(set(_mat3, a, b, c, d, e, f));
	}
	
	public static float quadCurve(float p0, float cp, float p1, float t) {
		return (1.0f - t) * (1.0f - t) * p0 + 2.0f * (1.0f - t) * t * cp + t * t * p1;
	}
	
	public static float cubicBezier(float p0, float cp0, float cp1, float p1, float t) {
		return (1.0f - t) * quadCurve(p0, cp0, cp1, t) + t * quadCurve(cp0, cp1, p1, t);
	}
	
	public static void intersectRects(float[] dest, float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
		dest[0] = Math.max(ax, bx); // x
		dest[1] = Math.max(ay, by); // y
		dest[2] = Math.max(0.0f, Math.min(ax + aw, bx + bw) - dest[0]); // w
		dest[3] = Math.max(0.0f, Math.min(ay + ah, by + bh) - dest[1]); // h
	}
	
	public static byte clamp(byte x, byte min, byte max) {
		return x < min ? min : x > max ? max : x;
	}
	
	public static short clamp(short x, short min, short max) {
		return x < min ? min : x > max ? max : x;
	}
	
	public static int clamp(int x, int min, int max) {
		return x < min ? min : x > max ? max : x;
	}
	
	public static float clamp(float x, float min, float max) {
		return x < min ? min : x > max ? max : x;
	}
	
	public static double clamp(double x, double min, double max) {
		return x < min ? min : x > max ? max : x;
	}
	
	public static long clamp(long x, long min, long max) {
		return x < min ? min : x > max ? max : x;
	}
	
	public static byte lerp(byte a, byte b, byte x) {
		return (byte) (a + x * (b - a));
	}
	
	public static short lerp(short a, short b, float x) {
		return (short) (a + x * (b - a));
	}
	
	public static int lerp(int a, int b, float x) {
		return (int) (a + x * (b - a));
	}
	
	public static float lerp(float a, float b, float x) {
		return a + x * (b - a);
	}
	
	public static double lerp(double a, double b, float x) {
		return a + x * (b - a);
	}
	
	public static long lerp(long a, long b, float x) {
		return (long) (a + x * (b - a));
	}
	
	public static boolean rangeContains(byte x, byte min, byte max) {
		return x >= min && x <= max;
	}
	
	public static boolean rangeContains(short x, short min, short max) {
		return x >= min && x <= max;
	}
	
	public static boolean rangeContains(int x, int min, int max) {
		return x >= min && x <= max;
	}
	
	public static boolean rangeContains(float x, float min, float max) {
		return x >= min && x <= max;
	}
	
	public static boolean rangeContains(double x, double min, double max) {
		return x >= min && x <= max;
	}
	
	public static boolean rangeContains(long x, long min, long max) {
		return x >= min && x <= max;
	}
	
	public static boolean boxContains(float x, float y, float boxX, float boxY, float boxWidth, float boxHeight) {
		return rangeContains(x, boxX, boxX + boxWidth) && rangeContains(y, boxY, boxY + boxHeight);
	}
}
