package io.gitlab.nasso.urmusic.model.scripting;

import java.util.Random;

import io.gitlab.nasso.urmusic.common.easing.EasingFunction;

public class ScriptAPIUtils {
	private ScriptAPIUtils() {}
	
	private static Random random = new Random();
	
	public static float seedRandom(short seed, float time) {
		long full = (((long) seed) << 32) | Float.floatToIntBits(time);
		
		random.setSeed(full);
		return random.nextFloat();
	}
	
	public static float shaker(float rate, float time, short seed) {
		int prevKey = (int) Math.floor(time / rate);
		int nextKey = prevKey + 1;

		long seedRoot = ((long) seed) << 32;
		
		random.setSeed(seedRoot | Float.floatToIntBits(prevKey / 1000.0f));
		float prevVal = random.nextFloat();
		
		random.setSeed(seedRoot | Float.floatToIntBits(nextKey / 1000.0f));
		float nextVal = random.nextFloat();
		
		return EasingFunction.LINEAR.apply(time - (prevKey * rate), prevVal, nextVal - prevVal, rate);
	}
}
