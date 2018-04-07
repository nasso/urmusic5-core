package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.FloatRange;
import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.event.TrackListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class Track {
	public static final class TrackActivityRange implements FloatRange {
		private Track track;
		private float start, end;
		
		public TrackActivityRange(Track t, float start, float end) {
			this.track = t;
			this.start = Math.min(start, end);
			this.end = Math.max(start, end);
		}

		public Track getTrack() {
			return this.track;
		}
		
		public float getStart() {
			return this.start;
		}

		public void setStart(float start) {
			start = Math.min(start, this.end - 1);
			start = Math.max(start, this.startFrameMin());
			
			if(this.start == start) return;
			this.start = start;
			this.track.notifyTrackRangesChangedEvent();
		}

		public float getEnd() {
			return this.end;
		}

		public void setEnd(float end) {
			end = Math.max(end, this.start + 1);
			end = Math.min(end, this.endFrameMax());
			
			if(this.end == end) return;
			this.end = end;
			this.track.notifyTrackRangesChangedEvent();
		}
		
		public float getLength() {
			return this.end - this.start;
		}
		
		public void moveTo(float newStart) {
			float len = this.getLength();
			newStart = MathUtils.clamp(newStart, this.startFrameMin(), this.endFrameMax() - len);
			
			if(this.start == newStart) return;
			
			this.start = newStart;
			this.end = newStart + len;
			
			this.track.notifyTrackRangesChangedEvent();
		}
		
		private float startFrameMin() {
			int thisIndex = this.track.activityRangesLengths.indexOf(this);
			
			if(thisIndex <= 0) return 0;
			
			return this.track.activityRangesLengths.get(thisIndex - 1).end;
		}
		
		private float endFrameMax() {
			int thisIndex = this.track.activityRangesLengths.indexOf(this);
			
			if(thisIndex == this.track.activityRangesLengths.size() - 1) return Float.MAX_VALUE;
			
			return this.track.activityRangesLengths.get(thisIndex + 1).start;
		}
	}
	
	private String name = "Unnamed";
	private boolean enabled = true;
	
	private List<TrackEffectInstance> effects = new ArrayList<>();
	private List<TrackEffectInstance> effectsUnmodifiable = Collections.unmodifiableList(this.effects);
	
	/**
	 * @see Track#getActivityRanges()
	 */
	private List<TrackActivityRange> activityRangesLengths = new ArrayList<>();
	private List<TrackActivityRange> unmodifiableRanges = Collections.unmodifiableList(this.activityRangesLengths);
	
	private List<TrackListener> listeners = new ArrayList<>();
	
	public Track(float initRangeLen) {
		this.addActiveRange(0, initRangeLen);
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if(this.name.equals(name)) return;
		
		this.name = name;
		this.notifyNameChanged();
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		if(this.enabled == enabled) return;
		
		this.enabled = enabled;
		
		this.notifyEnabledStateChanged();
	}
	
	public int getEffectCount() {
		return this.effects.size();
	}
	
	public TrackEffectInstance getEffect(int i) {
		return this.effects.get(i);
	}
	
	public List<TrackEffectInstance> getEffects() {
		return this.effectsUnmodifiable;
	}
	
	public void addEffect(TrackEffectInstance e) {
		this.addEffect(e, this.getEffectCount());
	}
	
	public void addEffect(TrackEffectInstance e, int i) {
		this.effects.add(i, e);
		
		this.notifyEffectAdded(e, i);
	}
	
	public TrackEffectInstance removeEffect(TrackEffectInstance e) {
		return this.removeEffect(this.effects.indexOf(e));
	}
	
	public TrackEffectInstance removeEffect(int i) {
		if(!this.checkEffectIndex(i)) return null;
		
		TrackEffectInstance item = this.getEffect(i);
		this.effects.remove(i);
		
		this.notifyEffectRemoved(item, i);
		
		return item;
	}
	
	public void moveEffect(TrackEffectInstance e, int newPos) {
		if(!this.checkEffectIndex(newPos)) return;
		
		int ei = this.effects.indexOf(e);
		if(ei >= 0) this.moveEffect(ei, newPos);
	}
	
	public void moveEffect(int currPos, int newPos) {
		if(!this.checkEffectIndex(currPos) || !this.checkEffectIndex(newPos)) return;
		
		TrackEffectInstance item = this.getEffect(currPos);
		this.effects.remove(currPos);
		this.effects.add(newPos, item);
		
		this.notifyEffectMoved(item, currPos, newPos);
	}
	
	private boolean checkEffectIndex(int i) {
		return i >= 0 && i < this.getEffectCount();
	}
	
	public void addTrackListener(TrackListener l) {
		this.listeners.add(l);
	}
	
	public void removeTrackListener(TrackListener l) {
		this.listeners.remove(l);
	}
	
	/**
	 * @return An unmodifiable list of ranges corresponding to the frames where this Track is enabled in the timeline.
	 */
	public List<TrackActivityRange> getActivityRanges() {
		return this.unmodifiableRanges;
	}
	
	public TrackActivityRange addActiveRange(float start, float len) {
		TrackActivityRange r = new TrackActivityRange(this, start, start + len);
		
		this.activityRangesLengths.add(r);
		
		this.notifyTrackRangesChangedEvent();
		
		return r;
	}
	
	public void removeActiveRange(TrackActivityRange r) {
		this.activityRangesLengths.remove(r);
		
		this.notifyTrackRangesChangedEvent();
	}
	
	/**
	 * If the given frame is on an active range, split this range at this point.<br>
	 * If it isn't, nothing happens.
	 * @param time
	 */
	public void splitAt(float time) {
		int i = 0; 
		TrackActivityRange r = null;
		
		for(i = 0; i < this.activityRangesLengths.size(); i++) {
			if((r = this.activityRangesLengths.get(i)).contains(time)) break;
			
			r = null;
		}
		
		if(r == null) return;
		TrackActivityRange r2 = new TrackActivityRange(this, time, r.getEnd());
		r.setEnd(time);
		
		this.activityRangesLengths.add(i + 1, r2);
		
		this.notifyTrackRangesChangedEvent();
	}
	
	/**
	 * Returns true if the track is active on the given frame.
	 */
	public boolean isActiveAt(float time) {
		return this.getRangeAt(time) != null;
	}
	
	public TrackActivityRange getRangeAt(float time) {
		for(TrackActivityRange r : this.activityRangesLengths) {
			if(r.contains(time)) return r;
		}
		
		return null;
	}
	
	private void notifyNameChanged() {
		for(TrackListener l : this.listeners) {
			l.nameChanged(this, this.getName());
		}
	}
	
	private void notifyEnabledStateChanged() {
		for(TrackListener l : this.listeners) {
			l.enabledStateChanged(this, this.isEnabled());
		}
	}
	
	private void notifyEffectAdded(TrackEffectInstance e, int pos) {
		for(TrackListener l : this.listeners) {
			l.effectAdded(this, e, pos);
		}
	}
	
	private void notifyEffectRemoved(TrackEffectInstance e, int pos) {
		for(TrackListener l : this.listeners) {
			l.effectRemoved(this, e, pos);
		}
	}
	
	private void notifyEffectMoved(TrackEffectInstance e, int oldPos, int newPos) {
		for(TrackListener l : this.listeners) {
			l.effectMoved(this, e, oldPos, newPos);
		}
	}
	
	private void notifyTrackRangesChangedEvent() {
		for(TrackListener l : this.listeners) {
			l.rangesChanged(this);
		}
	}
}
