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
package io.github.nasso.urmusic.common;

import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public class MathUtils {
	public static final float PI = (float) Math.PI;
	public static final float PI_2 = MathUtils.PI * 2.0f;
	public static final float HALF_PI = MathUtils.PI * 0.5f;
	public static final float PI_INV = 1.0f / MathUtils.PI;
	
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
		return mat.mul(MathUtils.setSkewX(MathUtils._mat3, angle));
	}
	
	public static Matrix3f skewY(Matrix3f mat, float angle) {
		return mat.mul(MathUtils.setSkewY(MathUtils._mat3, angle));
	}
	
	public static Matrix3f translate(Matrix3f mat, float x, float y) {
		return mat.mul(MathUtils.translation(MathUtils._mat3, x, y));
	}
	
	public static Matrix3f rotate(Matrix3f mat, float a) {
		return mat.mul(MathUtils.rotation(MathUtils._mat3, a));
	}
	
	public static Matrix3f scale(Matrix3f mat, float x, float y) {
		return mat.mul(MathUtils.scaling(MathUtils._mat3, x, y));
	}
	
	public static Matrix3f apply(Matrix3f mat, float a, float b, float c, float d, float e, float f) {
		return mat.mul(MathUtils.set(MathUtils._mat3, a, b, c, d, e, f));
	}
	
	public static float quadCurve(float p0, float cp, float p1, float t) {
		return (1.0f - t) * (1.0f - t) * p0 + 2.0f * (1.0f - t) * t * cp + t * t * p1;
	}
	
	public static float cubicBezier(float p0, float cp0, float cp1, float p1, float t) {
		return (1.0f - t) * MathUtils.quadCurve(p0, cp0, cp1, t) + t * MathUtils.quadCurve(cp0, cp1, p1, t);
	}
	
	public static void intersectRects(float[] dest, float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
		dest[0] = Math.max(ax, bx); // x
		dest[1] = Math.max(ay, by); // y
		dest[2] = Math.max(0.0f, Math.min(ax + aw, bx + bw) - dest[0]); // w
		dest[3] = Math.max(0.0f, Math.min(ay + ah, by + bh) - dest[1]); // h
	}
	
	public static float cosf(float a) {
		return (float) Math.cos(a);
	}
	
	public static float sinf(float a) {
		return (float) Math.sin(a);
	}
	
	public static float tanf(float a) {
		return (float) Math.tan(a);
	}
	
	public static float powf(float a, float b) {
		return (float) Math.pow(a, b);
	}
	
	public static float sqrtf(float x) {
		return (float) Math.sqrt(x);
	}
	
	public static float randomf() {
		return (float) Math.random();
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
		return MathUtils.rangeContains(x, boxX, boxX + boxWidth) && MathUtils.rangeContains(y, boxY, boxY + boxHeight);
	}
	
	public static boolean colinear(float v0x, float v0y, float v1x, float v1y) {
		return v0x * v1y == v0y * v1x;
	}
	
	public static boolean colinear(Vector2fc a, Vector2fc b) {
		return MathUtils.colinear(a.x(), a.y(), b.x(), b.y());
	}
	
	public static boolean aligned(float x0, float y0, float x1, float y1, float x2, float y2) {
		return MathUtils.colinear(x1 - x0, y1 - y0, x2 - x0, y2 - y0);
	}
	
	public static boolean alignedInOrder(float x0, float y0, float x1, float y1, float x2, float y2) {
		return 
			(
				(x0 <= x1 && x1 <= x2) ||
				(x0 >= x1 && x1 >= x2) // Technically checking only on 1 component is enough
			) &&
			MathUtils.aligned(x0, y0, x1, y1, x2, y2);
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
				MathUtils.rangeContains(dest.x, Math.min(ax, bx), Math.max(ax, bx)) &&
				MathUtils.rangeContains(dest.x, Math.min(cx, dx), Math.max(cx, dx));
	}
	
	public static void applyBlackmanWindow(float[] buffer, int length) {
		float factor = MathUtils.PI / (length - 1);
		
		for(int i = 0; i < length; ++i)
			buffer[i] *= (float) (0.42 - (0.5 * Math.cos(2 * factor * i)) + (0.08 * Math.cos(4 * factor * i)));
	}
	
	public static float addressArray(float[] array, int i) {
		return array[MathUtils.clamp(i, 0, array.length - 1)];
	}
	
	public static float getValue(float[] array, float index, boolean quadInterpolation) {
		if(quadInterpolation) {
			int rdn = (int) Math.floor(index + 0.5);
			
			// @format:off
			return MathUtils.quadCurve(
					MathUtils.lerp(
							MathUtils.addressArray(array, rdn - 1),
							MathUtils.addressArray(array, rdn),
							0.5f),
						MathUtils.addressArray(array, rdn),
						MathUtils.lerp(MathUtils.addressArray(array, rdn),
							MathUtils.addressArray(array, rdn + 1),
							0.5f),
						index - rdn + 0.5f);
			// @format:on
		} else {
			int flr = (int) Math.floor(index);
			int cel = (int) Math.ceil(index);
			
			float flrv = MathUtils.addressArray(array, flr);
			float celv = MathUtils.addressArray(array, cel);
			
			return MathUtils.lerp(flrv, celv, index - flr);
		}
	}
	
	public static String prettyTime(float s, boolean withMillis) {
		if(s == 0.0f) return withMillis ? "0:00.000" : "0:00";
		
		int millis = (int) Math.floor((s % 60f) * 1000);
		int seconds = (int) Math.floor(s % 60f);
		int minutes = (int) Math.floor(s / 60f % 60f);
		int hours = (int) Math.floor(s / 3600f);
		
		String zeroMin = "0" + minutes;
		zeroMin = zeroMin.substring(zeroMin.length() - 2);
		
		String zeroSec = "0" + seconds;
		zeroSec = zeroSec.substring(zeroSec.length() - 2);
		
		if(withMillis) {
			String zeroMilli = "000" + millis;
			zeroMilli = zeroMilli.substring(zeroMilli.length() - 3);
			
			zeroSec += "." + zeroMilli;
		}
		
		if(hours != 0) return hours + ":" + zeroMin + ":" + zeroSec;
		else return minutes + ":" + zeroSec;
	}
}
