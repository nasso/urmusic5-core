package io.github.nasso.urmusic.model.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.event.TrackRangesListener;
import io.github.nasso.urmusic.utils.IntRange;

public abstract class Track {
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
	
	/**
	 * @see Track#getActivityRangesLengths()
	 */
	private List<IntRange> activityRangesLengths = new ArrayList<>();
	private List<IntRange> unmodifiableRanges = Collections.unmodifiableList(this.activityRangesLengths);
	
	private List<TrackRangesListener> rangesListeners = new ArrayList<>();
	
	public Track() {
		this.addActiveRange(0, 600);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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
