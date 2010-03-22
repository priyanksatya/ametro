/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.render;

import android.graphics.*;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;
import org.ametro.model.SubwayTransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class RenderProgram {

	public static final int TYPE_LINE = 1;
	public static final int TYPE_TRANSFER_BACKGROUND = 2;
	public static final int TYPE_TRANSFER = 4;
	public static final int TYPE_STATION = 8;
	public static final int TYPE_STATION_NAME = 16;
	public static final int TYPE_BACKGROUND = 32;

	public static final int ONLY_TRANSPORT = TYPE_LINE | TYPE_TRANSFER_BACKGROUND | TYPE_TRANSFER | TYPE_STATION;
	public static final int ALL = ONLY_TRANSPORT | TYPE_STATION_NAME;

	RenderElement[] mElements;
	boolean[] mVisibility;
	Rect[] mBounds;
	int[] mTypes;
	int mRenderFilter;
	SubwayMap mSubwayMap;
	ArrayList<RenderElement> mRenderQueue;

	HashMap<SubwaySegment, RenderElement> segmentIndex = new HashMap<SubwaySegment, RenderElement>();
	HashMap<SubwayStation, RenderElement> stationIndex = new HashMap<SubwayStation, RenderElement>();
	HashMap<SubwayStation, RenderElement> stationNameIndex = new HashMap<SubwayStation, RenderElement>();
	HashMap<SubwayTransfer, RenderElement> transferBackgroundIndex = new HashMap<SubwayTransfer, RenderElement>();
	HashMap<SubwayTransfer, RenderElement> transferIndex = new HashMap<SubwayTransfer, RenderElement>();
	

	public void setRenderFilter(int renderFilter) {
		mRenderFilter = renderFilter;
	}

	public RenderProgram(SubwayMap subwayMap) {
		mSubwayMap = subwayMap;
		mRenderQueue = new ArrayList<RenderElement>();
		drawLines(subwayMap, mRenderQueue);
		drawTransfers(subwayMap, mRenderQueue);
		drawStations(subwayMap, mRenderQueue);
		updateRenderQueue();
		mRenderFilter = ALL;
	}

	private void updateRenderQueue() {
		Collections.sort(mRenderQueue);
		mElements = mRenderQueue.toArray(new RenderElement[mRenderQueue.size()]);
		final int count = mElements.length;
		mVisibility = new boolean[count];
		mBounds = new Rect[count];
		mTypes = new int[count];
		for (int i = 0; i < count; i++) {
			mVisibility[i] = false;
			mBounds[i] = mElements[i].boundingBox;
			mTypes[i] = mElements[i].type;
		}
	}

	public void invalidateVisible(RectF viewport) {
		final int offset = 10;
		final Rect v = new Rect(
				(int) (viewport.left - offset),
				(int) (viewport.top - offset),
				(int) (viewport.right + offset),
				(int) (viewport.bottom + offset));
		final Rect[] bounds = mBounds;
		final boolean[] visibility = mVisibility;
		final int[] filters = mTypes;
		final int filter = mRenderFilter;
		final int count = bounds.length;
		for (int i = 0; i < count; i++) {
			final Rect box = new Rect(bounds[i]);
			visibility[i] = ((filters[i] & filter) > 0) && Rect.intersects(v, box);
		}
	}

	
	public void updateSelection(List<SubwayStation> stations, List<SubwaySegment> segments, List<SubwayTransfer> transfers){
		if(stations!=null || segments!=null){
			for(RenderElement elem : mElements){
				elem.setSelection(false);
			}
		
			if(stations!=null){
				for(SubwayStation station : stations){
					
					stationIndex.get(station).setSelection(true);
					
					RenderElement stationName = stationNameIndex.get(station);
					if(stationName!=null){
						stationName.setSelection(true);
					}
				}
			}
			if(transfers!=null){
				for(SubwayTransfer transfer : transfers){
					RenderElement elem;
					elem = transferBackgroundIndex.get(transfer);
					if(elem!=null){
						elem.setSelection(true);
					}
					elem = transferIndex.get(transfer);
					if(elem!=null){
						elem.setSelection(true);
					}
				}
			}
			if(segments!=null){
				for(SubwaySegment segment : segments){
					RenderElement elem = segmentIndex.get(segment);
					if(elem!=null){
						elem.setSelection(true);
					}else{
						SubwaySegment opposite = mSubwayMap.getSegment(segment.toStationId, segment.fromStationId);
						if(opposite!=null){
							elem = segmentIndex.get(opposite);
							if(elem!=null){
								elem.setSelection(true);
							}
						}
					}
				}
			}
			
		}else{
			for(RenderElement elem : mElements){
				elem.setSelection(true);
			}
		}
		updateRenderQueue();
	}

	public void draw(Canvas canvas) {
		canvas.save();
		final RenderElement[] elements = mElements;
		final boolean[] visibility = mVisibility;
		final int count = elements.length;
		canvas.drawColor(Color.WHITE);
		for (int i = 0; i < count; i++) {
			if (visibility[i]) {
				elements[i].draw(canvas);
			}
		}
		canvas.restore();
	}

	private void drawStations(SubwayMap subwayMap, ArrayList<RenderElement> renderQueue) {
		for (SubwayStation station : subwayMap.stations) {
			if (station.point != null) {
				RenderElement stationElement = new RenderStation(subwayMap, station);
				renderQueue.add(stationElement);
				stationIndex.put(station, stationElement);
				if (station.rect != null && station.name != null) {
					RenderElement stationNameElement = new RenderStationName(subwayMap, station);
					renderQueue.add(stationNameElement);
					stationNameIndex.put(station, stationNameElement);
				}
			}
		}
	}

	private void drawTransfers(SubwayMap subwayMap, ArrayList<RenderElement> renderQueue) {
		for (SubwayTransfer transfer : subwayMap.transfers) {
			if( (transfer.flags & SubwayTransfer.INVISIBLE) != 0){
				continue;
			}
			
			RenderElement elementBackground = new RenderTransferBackground(subwayMap, transfer);
			RenderElement elementTransfer = new RenderTransfer(subwayMap, transfer);

			renderQueue.add(elementBackground);
			renderQueue.add(elementTransfer);
			
			transferIndex.put(transfer, elementTransfer);
			transferBackgroundIndex.put(transfer, elementBackground);
		}
	}

	private void drawLines(SubwayMap subwayMap, ArrayList<RenderElement> renderQueue) {
		HashSet<Integer> exclusions = new HashSet<Integer>();
		for (SubwaySegment segment : subwayMap.segments) {
			if (exclusions.contains(segment.id)) continue;
			if ((segment.flags & SubwaySegment.INVISIBLE) == 0) {
				SubwayStation from = subwayMap.stations[segment.fromStationId];
				SubwayStation to = subwayMap.stations[segment.toStationId];
				if (from.point != null && to.point != null) {
					SubwaySegment opposite = subwayMap.getSegment(to, from);
					Point[] additionalPoints = subwayMap.getSegmentsNodes(segment.id);
					Point[] reversePoints = opposite == null ? null : subwayMap.getSegmentsNodes(opposite.id);
					boolean additionalForward = additionalPoints != null;
					boolean additionalBackward = reversePoints != null;
					if (!additionalForward && additionalBackward) {
					} else {
						RenderElement element = new RenderSegment(subwayMap, segment);
						renderQueue.add(element);
						segmentIndex.put(segment, element);
						if (opposite != null) {
							exclusions.add(opposite.id);
						}
					}
				}
			}
		}
	}

	public void clearVisibility() {
		final boolean[] visibility = mVisibility;
		final int count = visibility.length;
		for (int i = 0; i < count; i++) {
			visibility[i] = false;
		}
	}

	public void addVisibility(RectF viewport) {
		final int offset = 10;
		final Rect v = new Rect(
				(int) (viewport.left - offset),
				(int) (viewport.top - offset),
				(int) (viewport.right + offset),
				(int) (viewport.bottom + offset));
		final Rect[] bounds = mBounds;
		final boolean[] visibility = mVisibility;
		final int count = bounds.length;
		for (int i = 0; i < count; i++) {
			visibility[i] |= Rect.intersects(v, bounds[i]);
		}
	}
	
	public void addVisibility2(RectF viewport1, RectF viewport2) {
		final int offset = 10;
		final Rect v1 = new Rect(
				(int) (viewport1.left - offset),
				(int) (viewport1.top - offset),
				(int) (viewport1.right + offset),
				(int) (viewport1.bottom + offset));
		final Rect v2 = new Rect(
				(int) (viewport2.left - offset),
				(int) (viewport2.top - offset),
				(int) (viewport2.right + offset),
				(int) (viewport2.bottom + offset));
		final Rect[] bounds = mBounds;
		final boolean[] visibility = mVisibility;
		final int count = bounds.length;
		for (int i = 0; i < count; i++) {
			final Rect box = bounds[i];
			visibility[i] |= ( Rect.intersects(v1, box) || Rect.intersects(v2, box) );
		}
	}	
}