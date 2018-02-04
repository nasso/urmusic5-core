package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.event.EffectInstanceListener;
import io.github.nasso.urmusic.model.event.TrackListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.utils.IntRange;
import io.github.nasso.urmusic.utils.MathUtils;

public class Track implements EffectInstanceListener {
	public static final class TrackActivityRange implements IntRange {
		private Track track;
		private int start, end;
		
		public TrackActivityRange(Track t, int start, int end) {
			this.track = t;
			this.start = Math.min(start, end);
			this.end = Math.max(start, end);
		}

		public Track getTrack() {
			return this.track;
		}
		
		public int getStart() {
			return this.start;
		}

		public void setStart(int start) {
			start = Math.min(start, this.end - 1);
			start = Math.max(start, this.startFrameMin());
			
			if(this.start == start) return;
			this.start = start;
			this.track.notifyTrackRangesChangedEvent();
		}

		public int getEnd() {
			return this.end;
		}

		public void setEnd(int end) {
			end = Math.max(end, this.start + 1);
			end = Math.min(end, this.endFrameMax());
			
			if(this.end == end) return;
			this.end = end;
			this.track.notifyTrackRangesChangedEvent();
		}
		
		public int getLength() {
			return this.end - this.start;
		}
		
		public void moveTo(int newStart) {
			int len = this.getLength();
			newStart = MathUtils.clamp(newStart, this.startFrameMin(), this.endFrameMax() - len);
			
			if(this.start == newStart) return;
			
			this.start = newStart;
			this.end = newStart + len;
			
			this.track.notifyTrackRangesChangedEvent();
		}
		
		private int startFrameMin() {
			int thisIndex = this.track.activityRangesLengths.indexOf(this);
			
			if(thisIndex <= 0) return 0;
			
			return this.track.activityRangesLengths.get(thisIndex - 1).end + 1;
		}
		
		private int endFrameMax() {
			int thisIndex = this.track.activityRangesLengths.indexOf(this);
			
			if(thisIndex == this.track.activityRangesLengths.size() - 1) return Integer.MAX_VALUE;
			
			return this.track.activityRangesLengths.get(thisIndex + 1).start - 1;
		}
	}
	
	private String name = "Unnamed";
	private boolean enabled = true;
	private List<TrackEffectInstance> effects = new ArrayList<>();
	
	/**
	 * @see Track#getActivityRangesLengths()
	 */
	private List<TrackActivityRange> activityRangesLengths = new ArrayList<>();
	private List<TrackActivityRange> unmodifiableRanges = Collections.unmodifiableList(this.activityRangesLengths);
	
	private List<TrackListener> listeners = new ArrayList<>();
	
	public Track(int initRangeLen) {
		this.addActiveRange(0, initRangeLen - 1);
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
	
	public void addEffect(TrackEffectInstance e) {
		this.addEffect(e, this.getEffectCount());
	}
	
	public void addEffect(TrackEffectInstance e, int i) {
		this.effects.add(i, e);
		
		e.addEffectInstanceListener(this);
		this.notifyEffectAdded(e, i);
	}
	
	public TrackEffectInstance removeEffect(TrackEffectInstance e) {
		return this.removeEffect(this.effects.indexOf(e));
	}
	
	public TrackEffectInstance removeEffect(int i) {
		if(!this.checkEffectIndex(i)) return null;
		
		TrackEffectInstance item = this.getEffect(i);
		this.effects.remove(i);
		
		item.removeEffectInstanceListener(this);
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
	 * @return An unmodifiable list of int ranges corresponding to the frames where this Track is enabled in the timeline.
	 */
	public List<TrackActivityRange> getActivityRangesLengths() {
		return this.unmodifiableRanges;
	}
	
	public TrackActivityRange addActiveRange(int start, int len) {
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
	 * @param frame
	 */
	public void splitAt(int frame) {
		int i = 0; 
		TrackActivityRange r = null;
		
		for(i = 0; i < this.activityRangesLengths.size(); i++) {
			if((r = this.activityRangesLengths.get(i)).contains(frame)) break;
			
			r = null;
		}
		
		if(r == null) return;
		TrackActivityRange r2 = new TrackActivityRange(this, frame, r.getEnd());
		r.setEnd(frame - 1);
		
		this.activityRangesLengths.add(i + 1, r2);
		
		this.notifyTrackRangesChangedEvent();
	}
	
	/**
	 * Returns true if the track is active on the given frame.
	 */
	public boolean isActiveAt(int frame) {
		return this.getRangeAt(frame) != null;
	}
	
	public TrackActivityRange getRangeAt(int frame) {
		for(TrackActivityRange r : this.activityRangesLengths) {
			if(r.contains(frame)) return r;
		}
		
		return null;
	}
	
	public void dirtyFlagged(TrackEffectInstance source) {
		this.notifyDirtyFlagged();
	}

	public void enabledStateChanged(TrackEffectInstance source, boolean isEnabledNow) {
		this.notifyDirtyFlagged();
	}

	public void parameterAdded(TrackEffectInstance source, int i, EffectParam<?> ctrl) {
	}

	public void parameterRemoved(TrackEffectInstance source, int i, EffectParam<?> ctrl) {
	}
	
	private void notifyDirtyFlagged() {
		for(TrackListener l : this.listeners) {
			l.dirtyFlagged(this);
		}
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
