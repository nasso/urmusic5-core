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
urmusic = urm = {};
(function commonSetup() {
	var UrmusicModel = Java.type("io.gitlab.nasso.urmusic.model.UrmusicModel");
	var _audio = UrmusicModel.getAudioRenderer();

	urm.clamp = function(value, min, max) {
		return value < min ? min : value > max ? max : value;
	};

	urm.map = function(value, a, b, c, d) {
		var clamped = urm.clamp(value, a, b);
		return c + ((clamped - a) / (b - a)) * (d - c);
	};
	
	urm.lerp = function(a, b, x) {
		return a + x * (b - a);
	};
	
	urm.audio = {};
	Object.defineProperty(urm.audio, "duration", {
		get: function() { return _audio.getDuration(); },
		set: undefined
	});
	
	Object.defineProperty(urm.audio, "sampleRate", {
		get: function() { return _audio.getSampleRate(); },
		set: undefined
	});
	
	Object.defineProperty(urm.audio, "bufferSize", {
		get: function() { return _audio.getBufferSize(); },
		set: undefined
	});
	
	urm.audio.maxFreqValue = function(n_time, n_duration) {
		if(typeof n_time !== "number") throw new Error("Invalid argument for 'n_time': expected number, got " + (typeof n_time));
		if(n_duration !== undefined && typeof n_duration !== "number") throw new Error("Invalid argument for 'n_duration': expected number, got " + (typeof n_duration));
		
		if(n_duration === undefined) return _audio.maxFreqValue(n_time);
		else return _audio.maxFreqValue(n_time, n_duration);
	};
	
	urm.audio.minFreqValue = function(n_time, n_duration) {
		if(typeof n_time !== "number") throw new Error("Invalid argument for 'n_time': expected number, got " + (typeof n_time));
		if(n_duration !== undefined && typeof n_duration !== "number") throw new Error("Invalid argument for 'n_duration': expected number, got " + (typeof n_duration));
		
		if(n_duration === undefined) return _audio.minFreqValue(n_time);
		else return _audio.minFreqValue(n_time, n_duration);
	};
	
	urm.audio.peakToPeakAmp = function(n_time, n_duration) {
		if(typeof n_time !== "number") throw new Error("Invalid argument for 'n_time': expected number, got " + (typeof n_time));
		if(typeof n_duration !== "number") throw new Error("Invalid argument for 'n_duration': expected number, got " + (typeof n_duration));
		
		return _audio.peakToPeakAmp(n_time, n_duration);
	};
	
	urm.audio.peakAmp = function(n_time, n_duration) {
		if(typeof n_time !== "number") throw new Error("Invalid argument for 'n_time': expected number, got " + (typeof n_time));
		if(typeof n_duration !== "number") throw new Error("Invalid argument for 'n_duration': expected number, got " + (typeof n_duration));
		
		return _audio.peakAmp(n_time, n_duration);
	};
	
	var remote = Java.type("io.gitlab.nasso.urmusic.model.scripting.ScriptAPIUtils");
	
	urm.seedRandom = function(n_seed, n_time) {
		if(typeof n_seed !== "number") throw new Error("Invalid argument for 'n_seed': expected number, got " + (typeof n_seed));
		if(typeof n_time !== "number") throw new Error("Invalid argument for 'n_time': expected number, got " + (typeof n_time));
		
		return remote.seedRandom(n_seed & 0xFFFF, n_time);
	};
	
	urm.shaker = function(n_rate, n_time, n_seed) {
		if(typeof n_rate !== "number") throw new Error("Invalid argument for 'n_rate': expected number, got " + (typeof n_rate));
		if(typeof n_time !== "number") throw new Error("Invalid argument for 'n_time': expected number, got " + (typeof n_time));
		if(n_seed !== undefined && typeof n_seed !== "number") throw new Error("Invalid argument for 'n_seed': expected number, got " + (typeof n_seed));
		
		if(n_seed === undefined) n_seed = 0x0000;
		
		return remote.shaker(n_rate, n_time, n_seed & 0xFFFF);
	};
})();
