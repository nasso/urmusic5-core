package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.common.easing.CubicBezierEasing;
import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.easing.FramesEasing;
import io.github.nasso.urmusic.common.easing.StepEasing;

class EasingFuncChunk implements Chunk {
	static final int ID = buildBigInt('E', 'A', 'S', 'E');
	static final EasingFuncChunk DUMMY = EasingFuncChunk.from(EasingFunction.LINEAR);
	private static final EasingFunction[] FUNC_CONSTANTS = {
			EasingFunction.LINEAR,
			
			EasingFunction.EASE_IN_SINE,
			EasingFunction.EASE_OUT_SINE,
			EasingFunction.EASE_IN_OUT_SINE,
			
			EasingFunction.EASE_IN_QUAD,
			EasingFunction.EASE_OUT_QUAD,
			EasingFunction.EASE_IN_OUT_QUAD,
			
			EasingFunction.EASE_IN_CUBIC,
			EasingFunction.EASE_OUT_CUBIC,
			EasingFunction.EASE_IN_OUT_CUBIC,
			
			EasingFunction.EASE_IN_QUART,
			EasingFunction.EASE_OUT_QUART,
			EasingFunction.EASE_IN_OUT_QUART,
			
			EasingFunction.EASE_IN_QUINT,
			EasingFunction.EASE_OUT_QUINT,
			EasingFunction.EASE_IN_OUT_QUINT,
			
			EasingFunction.EASE_IN_EXPO,
			EasingFunction.EASE_OUT_EXPO,
			EasingFunction.EASE_IN_OUT_EXPO,
			
			EasingFunction.EASE_IN_CIRC,
			EasingFunction.EASE_OUT_CIRC,
			EasingFunction.EASE_IN_OUT_CIRC,
			
			EasingFunction.EASE_IN_BACK,
			EasingFunction.EASE_OUT_BACK,
			EasingFunction.EASE_IN_OUT_BACK,
			
			EasingFunction.EASE_IN_ELASTIC,
			EasingFunction.EASE_OUT_ELASTIC,
			EasingFunction.EASE_IN_OUT_ELASTIC,
			
			EasingFunction.EASE_OUT_BOUNCE,
			EasingFunction.EASE_IN_BOUNCE,
			EasingFunction.EASE_IN_OUT_BOUNCE
	};
	/*
	private static final int FUNC_LINEAR = 0;
	private static final int FUNC_EASE_IN_SINE = 1;
	private static final int FUNC_EASE_OUT_SINE = 2;
	private static final int FUNC_EASE_IN_OUT_SINE = 3;
	private static final int FUNC_EASE_IN_QUAD = 4;
	private static final int FUNC_EASE_OUT_QUAD = 5;
	private static final int FUNC_EASE_IN_OUT_QUAD = 6;
	private static final int FUNC_EASE_IN_CUBIC = 7;
	private static final int FUNC_EASE_OUT_CUBIC = 8;
	private static final int FUNC_EASE_IN_OUT_CUBIC = 9;
	private static final int FUNC_EASE_IN_QUART = 10;
	private static final int FUNC_EASE_OUT_QUART = 11;
	private static final int FUNC_EASE_IN_OUT_QUART = 12;
	private static final int FUNC_EASE_IN_QUINT = 13;
	private static final int FUNC_EASE_OUT_QUINT = 14;
	private static final int FUNC_EASE_IN_OUT_QUINT = 15;
	private static final int FUNC_EASE_IN_EXPO = 16;
	private static final int FUNC_EASE_OUT_EXPO = 17;
	private static final int FUNC_EASE_IN_OUT_EXPO = 18;
	private static final int FUNC_EASE_IN_CIRC = 19;
	private static final int FUNC_EASE_OUT_CIRC = 20;
	private static final int FUNC_EASE_IN_OUT_CIRC = 21;
	private static final int FUNC_EASE_IN_BACK = 22;
	private static final int FUNC_EASE_OUT_BACK = 23;
	private static final int FUNC_EASE_IN_OUT_BACK = 24;
	private static final int FUNC_EASE_IN_ELASTIC = 25;
	private static final int FUNC_EASE_OUT_ELASTIC = 26;
	private static final int FUNC_EASE_IN_OUT_ELASTIC = 27;
	private static final int FUNC_EASE_OUT_BOUNCE = 28;
	private static final int FUNC_EASE_IN_BOUNCE = 29;
	private static final int FUNC_EASE_IN_OUT_BOUNCE = 30;
	*/
	private static final int FUNC_CUBICBEZIER = 31;
	private static final int FUNC_FRAMES = 32;
	private static final int FUNC_STEPS = 33;
	
	EasingFunction func;
	
	public int size() {
		if(this.func instanceof CubicBezierEasing) {
			return
					+ 8 // header
					+ 1 // func
					+ 4 * 4; // x1, y1, x2, y2
		} else if(this.func instanceof FramesEasing) {
			return
					+ 8 // header
					+ 1 // func
					+ 4; // nb frames
		} else if(this.func instanceof StepEasing) {
			return
					+ 8 // header
					+ 1 // func
					+ 4; // nb steps (positive -> direction start, negative -> direction end)
		}

		return
				+ 8 // header
				+ 1; // func
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		if(this.func instanceof CubicBezierEasing) {
			CubicBezierEasing bz = (CubicBezierEasing) this.func;
			
			out.write(FUNC_CUBICBEZIER);
			writeBigInt(out, Float.floatToIntBits(bz.x1));
			writeBigInt(out, Float.floatToIntBits(bz.y1));
			writeBigInt(out, Float.floatToIntBits(bz.x2));
			writeBigInt(out, Float.floatToIntBits(bz.y2));
		} else if(this.func instanceof FramesEasing) {
			FramesEasing f = (FramesEasing) this.func;
			
			out.write(FUNC_FRAMES);
			writeBigInt(out, f.number_of_frames);
		} else if(this.func instanceof StepEasing) {
			StepEasing se = (StepEasing) this.func;
			
			out.write(FUNC_STEPS);
			writeBigInt(out, se.direction == StepEasing.Direction.START ? se.number_of_steps : -se.number_of_steps);
		} else {
			for(int i = 0; i < FUNC_CONSTANTS.length; i++) {
				if(this.func == FUNC_CONSTANTS[i]) {
					out.write(i);
					break;
				}
			}
		}
	}

	public static EasingFuncChunk from(EasingFunction func) {
		EasingFuncChunk ch = new EasingFuncChunk();
		
		ch.func = func;
		
		return ch;
	}
}
