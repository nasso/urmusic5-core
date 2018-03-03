package io.github.nasso.urmusic.common;

import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector2fc;

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
	
	public static boolean colinear(float v0x, float v0y, float v1x, float v1y) {
		return v0x * v1y == v0y * v1x;
	}
	
	public static boolean colinear(Vector2fc a, Vector2fc b) {
		return colinear(a.x(), a.y(), b.x(), b.y());
	}
	
	public static boolean aligned(float x0, float y0, float x1, float y1, float x2, float y2) {
		return colinear(x1 - x0, y1 - y0, x2 - x0, y2 - y0);
	}
	
	/**
	 * Stores the intersection between [AB] and [CD] if any, in dest.
	 * Returns false when the lines do not intersect, leaving <tt>dest</tt> as-is, true otherwise.<br />
	 */
	public static boolean intersection(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, Vector2f dest) {
		// 1. Define the lines such as:
		// AB: a1 * x + b1 * y + c1 = 0
		// CD: a2 * x + b2 * y + c2 = 0
		
		float
			a1, b1, c1,
			a2, b2, c2;
		
		a1 = by - ay;
		a2 = dy - cy;
		
		b1 = ax - bx;
		b2 = cx - dx;
		
		c1 = -a1 * ax - b1 * ay;
		c2 = -a2 * cx - b2 * cy;
		
		// 2. Find that point
		// See: https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Using_homogeneous_coordinates
		float w = a1 * b2 - a2 * b1;
		if(w == 0) return false;
		
		dest.x = (b1 * c2 - b2 * c1) / w;
		dest.y = (a2 * c1 - a1 * c2) / w;
		
		return 
				rangeContains(dest.x, Math.min(ax, bx), Math.max(ax, bx)) &&
				rangeContains(dest.x, Math.min(cx, dx), Math.max(cx, dx));
	}
	
	public static void applyBlackmanWindow(float[] buffer, int length) {
		float factor = MathUtils.PI / (length - 1);
		
		for(int i = 0; i < length; ++i)
			buffer[i] = buffer[i] * (float) (0.42 - (0.5 * Math.cos(2 * factor * i)) + (0.08 * Math.cos(4 * factor * i)));
	}
	
	public static float addressArray(float[] array, int i, float outValue) {
		if(i < 0 || i >= array.length) {
			return outValue;
		} else {
			return array[i];
		}
	}
	
	public static float getValue(float[] array, float index, boolean quadInterpolation, float minValue) {
		if(quadInterpolation) {
			int rdn = (int) Math.floor(index + 0.5);
			
			// @format:off
			return quadCurve(
					lerp(
							addressArray(array, rdn - 1, minValue),
							addressArray(array, rdn, minValue),
							0.5f),
						addressArray(array, rdn, minValue),
						lerp(addressArray(array, rdn, minValue),
							addressArray(array, rdn + 1, minValue),
							0.5f),
						index - rdn + 0.5f);
			// @format:on
		} else {
			int flr = (int) Math.floor(index);
			int cel = (int) Math.ceil(index);
			
			float flrv = addressArray(array, flr, minValue);
			float celv = addressArray(array, cel, minValue);
			
			return lerp(flrv, celv, index - flr);
		}
	}
}
