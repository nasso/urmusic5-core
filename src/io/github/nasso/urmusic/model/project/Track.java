package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.TrackEffectsListener;
import io.github.nasso.urmusic.model.event.TrackRangesListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.utils.IntRange;

public abstract class Track<T extends TrackEffectInstance> {
	private static final class IntRangeImpl implements IntRange {
		private int start, end;
		
		public IntRangeImpl(int start, int end) {
			this.setStart(start);
			this.setEnd(end);
		}

		public int getStart() {
			return this.start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getEnd() {
			return this.end;
		}

		public void setEnd(int end) {
			this.end = end;
		}
		
	}
	
	private String name;
	private List<T> effects = new ArrayList<>();
	
	/**
	 * @see Track#getActivityRangesLengths()
	 */
	private List<IntRange> activityRangesLengths = new ArrayList<>();
	private List<IntRange> unmodifiableRanges = Collections.unmodifiableList(this.activityRangesLengths);
	
	private List<TrackRangesListener> rangesListeners = new ArrayList<>();
	private List<TrackEffectsListener> effectListListeners = new ArrayList<>();
	
	public Track() {
		this.addActiveRange(0, 600);
	}
	
	public void dispose() {
		UrmusicModel.disposeTrack(this);
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getEffectCount() {
		return this.effects.size();
	}
	
	public T getEffect(int i) {
		return this.effects.get(i);
	}
	
	public void addEffect(T e) {
		this.addEffect(e, this.getEffectCount());
	}
	
	public void addEffect(T e, int i) {
		this.effects.add(i, e);
		
		this.notifyEffectAdded(e, i);
	}
	
	public T removeEffect(T e) {
		return this.removeEffect(this.effects.indexOf(e));
	}
	
	public T removeEffect(int i) {
		if(!this.checkEffectIndex(i)) return null;
		
		T item = this.getEffect(i);
		this.effects.remove(i);
		this.notifyEffectRemoved(item, i);
		
		return item;
	}
	
	public void moveEffect(T e, int newPos) {
		if(!this.checkEffectIndex(newPos)) return;
		
		int ei = this.effects.indexOf(e);
		if(ei >= 0) this.moveEffect(ei, newPos);
	}
	
	public void moveEffect(int currPos, int newPos) {
		if(!this.checkEffectIndex(currPos) || !this.checkEffectIndex(newPos)) return;
		
		T item = this.getEffect(currPos);
		this.effects.remove(currPos);
		this.effects.add(newPos, item);
		
		this.notifyEffectMoved(item, currPos, newPos);
	}
	
	private boolean checkEffectIndex(int i) {
		return i >= 0 && i < this.getEffectCount();
	}
	
	public void addEffectsListener(TrackEffectsListener l) {
		this.effectListListeners.add(l);
	}
	
	public void removeEffectsListener(TrackEffectsListener l) {
		this.effectListListeners.remove(l);
	}
	
	private void notifyEffectAdded(T e, int pos) {
		for(TrackEffectsListener l : this.effectListListeners) {
			l.effectAdded(e, pos);
		}
	}
	
	private void notifyEffectRemoved(T e, int pos) {
		for(TrackEffectsListener l : this.effectListListeners) {
			l.effectRemoved(e, pos);
		}
	}
	
	private void notifyEffectMoved(T e, int oldPos, int newPos) {
		for(TrackEffectsListener l : this.effectListListeners) {
			l.effectMoved(e, oldPos, newPos);
		}
	}
	
	public void addTrackRangesListener(TrackRangesListener listener) {
		this.rangesListeners.add(listener);
	}
	
	public void removeTrackRangesListener(TrackRangesListener listener) {
		this.rangesListeners.remove(listener);
	}
	
	/**
	 * @return An unmodifiable list of int ranges corresponding to the frames where this Track is enabled in the timeline.
	 */
	public List<IntRange> getActivityRangesLengths() {
		return this.unmodifiableRanges;
	}
	
	public IntRange addActiveRange(int start, int len) {
		IntRange r = new IntRangeImpl(start, start + len);
		
		this.activityRangesLengths.add(r);
		
		this.fireTrackRangesChangedEvent();
		
		return r;
	}
	
	public void removeActiveRange(IntRange r) {
		this.activityRangesLengths.remove(r);
		
		this.fireTrackRangesChangedEvent();
	}
	
	/**
	 * If the given frame is on an active range, split this range at this point.<br>
	 * If it isn't, nothing happens.
	 * @param frame
	 */
	public void splitAt(int frame) {
		int i = 0; 
		IntRange ri = null;
		
		for(i = 0; i < this.activityRangesLengths.size(); i++) {
			ri = this.activityRangesLengths.get(i);
			
			if(ri.contains(frame)) break;
		}
		
		if(ri == null) return;
		IntRangeImpl r = (IntRangeImpl) ri;
		IntRangeImpl r2 = new IntRangeImpl(frame, r.getEnd());
		r.setEnd(frame);
		
		this.activityRangesLengths.add(i + 1, r2);
		
		this.fireTrackRangesChangedEvent();
	}
	
	/**
	 * Returns true if the track is active on the given frame.
	 */
	public boolean isActiveAt(int frame) {
		for(IntRange r : this.activityRangesLengths) {
			if(r.contains(frame)) return true;
		}
		
		return false;
	}
	
	private void fireTrackRangesChangedEvent() {
		for(TrackRangesListener l : this.rangesListeners) {
			l.rangesChanged(this);
		}
	}
}
