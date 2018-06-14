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
package io.gitlab.nasso.urmusic.view.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

import io.gitlab.nasso.urmusic.common.DataUtils;

public class UrmusicSplittedPaneState {
	private int viewID = 0;
	private boolean splitted = false;
	private boolean vertical = false;
	private int splitLocation = 200;
	private int posX = 100, posY = 100, width = 800, height = 600, extendedState = JFrame.NORMAL;
	private UrmusicSplittedPaneState stateA = null;
	private UrmusicSplittedPaneState stateB = null;

	public int getViewID() {
		return this.viewID;
	}
	
	public void setViewID(int viewID) {
		this.viewID = viewID;
	}
	
	public boolean isSplitted() {
		return this.splitted;
	}

	public void setSplitted(boolean splitted) {
		this.splitted = splitted;
	}

	public boolean isVertical() {
		return this.vertical;
	}

	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}

	public int getSplitLocation() {
		return this.splitLocation;
	}

	public void setSplitLocation(int splitLocation) {
		this.splitLocation = splitLocation;
	}

	public int getPosX() {
		return this.posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return this.posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getExtendedState() {
		return this.extendedState;
	}

	public void setExtendedState(int extendedState) {
		this.extendedState = extendedState;
	}

	public UrmusicSplittedPaneState getStateA() {
		return this.stateA;
	}

	public void setStateA(UrmusicSplittedPaneState stateA) {
		this.stateA = stateA;
	}

	public UrmusicSplittedPaneState getStateB() {
		return this.stateB;
	}

	public void setStateB(UrmusicSplittedPaneState stateB) {
		this.stateB = stateB;
	}
	
	public void write(OutputStream out) throws IOException {
		DataUtils.writeBigInt(out, this.viewID);
		DataUtils.writeBigShort(out, (short) (this.splitted ? 1 : 0));
		DataUtils.writeBigShort(out, (short) (this.vertical ? 1 : 0));
		DataUtils.writeBigInt(out, this.splitLocation);
		DataUtils.writeBigInt(out, this.getPosX());
		DataUtils.writeBigInt(out, this.getPosY());
		DataUtils.writeBigInt(out, this.getWidth());
		DataUtils.writeBigInt(out, this.getHeight());
		DataUtils.writeBigInt(out, this.getExtendedState());
		
		if(this.splitted) {
			this.stateA.write(out);
			this.stateB.write(out);
		}
	}
	
	public void read(InputStream in) throws IOException {
		this.setViewID(DataUtils.readBigInt(in));
		this.setSplitted(DataUtils.readBigShort(in) == 1);
		this.setVertical(DataUtils.readBigShort(in) == 1);
		this.setSplitLocation(DataUtils.readBigInt(in));
		this.setPosX(DataUtils.readBigInt(in));
		this.setPosY(DataUtils.readBigInt(in));
		this.setWidth(DataUtils.readBigInt(in));
		this.setHeight(DataUtils.readBigInt(in));
		this.setExtendedState(DataUtils.readBigInt(in));
		
		if(this.splitted) {
			(this.stateA == null ? this.stateA = new UrmusicSplittedPaneState() : this.stateA).read(in);
			(this.stateB == null ? this.stateB = new UrmusicSplittedPaneState() : this.stateB).read(in);
		}
	}
}
