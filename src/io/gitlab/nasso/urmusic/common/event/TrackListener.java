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
package io.gitlab.nasso.urmusic.common.event;

import io.gitlab.nasso.urmusic.model.project.Track;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public interface TrackListener {
	public void nameChanged(Track source, String newName);
	public void enabledStateChanged(Track source, boolean isEnabledNow);
	
	public void rangesChanged(Track source);

	public void effectAdded(Track source, TrackEffectInstance e, int pos);
	public void effectRemoved(Track source, TrackEffectInstance e, int pos);
	public void effectMoved(Track source, TrackEffectInstance e, int oldPos, int newPos);
}
