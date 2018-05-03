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
urmusic = {};
(function commonSetup() {
	var UrmusicModel = Java.type("io.github.nasso.urmusic.model.UrmusicModel");
	var _audio = UrmusicModel.getAudioRenderer();
	
	urmusic.audio = {};
	Object.defineProperty(urmusic.audio, "sampleRate", {
		get: function() { return _audio.getSampleRate(); },
		set: undefined
	});
	
	Object.defineProperty(urmusic.audio, "bufferSize", {
		get: function() { return _audio.getBufferSize(); },
		set: undefined
	});
	
	urmusic.audio.maxFreqValue = function(f_time, f_duration) {
		if(typeof f_time !== "number") throw new Error("Invalid argument for 'f_time': expected number, got " + (typeof f_time));
		if(f_duration !== undefined && typeof f_duration !== "number") throw new Error("Invalid argument for 'f_duration': expected number, got " + (typeof f_duration));
		
		if(f_duration === undefined) return _audio.maxFreqValue(f_time);
		else return _audio.maxFreqValue(f_time, f_duration);
	};
	
	urmusic.audio.minFreqValue = function(f_time, f_duration) {
		if(typeof f_time !== "number") throw new Error("Invalid argument for 'f_time': expected number, got " + (typeof f_time));
		if(f_duration !== undefined && typeof f_duration !== "number") throw new Error("Invalid argument for 'f_duration': expected number, got " + (typeof f_duration));
		
		if(f_duration === undefined) return _audio.minFreqValue(f_time);
		else return _audio.minFreqValue(f_time, f_duration);
	};
	
	urmusic.audio.peakToPeakAmp = function(f_time, f_duration) {
		if(typeof f_time !== "number") throw new Error("Invalid argument for 'f_time': expected number, got " + (typeof f_time));
		if(typeof f_duration !== "number") throw new Error("Invalid argument for 'f_duration': expected number, got " + (typeof f_duration));
		
		return _audio.peakToPeakAmp(f_time, f_duration);
	};
	
	urmusic.audio.peakAmp = function(f_time, f_duration) {
		if(typeof f_time !== "number") throw new Error("Invalid argument for 'f_time': expected number, got " + (typeof f_time));
		if(typeof f_duration !== "number") throw new Error("Invalid argument for 'f_duration': expected number, got " + (typeof f_duration));
		
		return _audio.peakAmp(f_time, f_duration);
	};
	
	urmusic.audio.peakAmp = function(f_time, f_duration) {
		if(typeof f_time !== "number") throw new Error("Invalid argument for 'f_time': expected number, got " + (typeof f_time));
		if(typeof f_duration !== "number") throw new Error("Invalid argument for 'f_duration': expected number, got " + (typeof f_duration));
		
		return _audio.peakAmp(f_time, f_duration);
	};
})();

function clamp(value, min, max) {
	return value < min ? min : value > max ? max : value;
}

function map(value, a, b, c, d) {
	var clamped = clamp(value, a, b);
	return c + ((clamped - a) / (b - a)) * (d - c);
}
