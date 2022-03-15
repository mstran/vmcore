/*
 * Copyright(c) 2022 Microstran Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microstran.core.clock;

import java.awt.Rectangle;

import com.microstran.core.viewer.SVG_SWTViewerFrame;

/**
 * A class to encapsulate individual clocks, for use generally with the
 * screen save programs this class contains key classes and info needed to allow
 * a grid of clocks to be displayed and managed, these are the elements 
 * of the grid
 *
 * @author Mike Stran
 *
 */
public class ClockDisplayElement
{

	/**
	 * total rectangle the clock is consuming
	 */
	private Rectangle rectangle;
	/**
	 * clock for display
	 */
	private Clock clock;
	
	/**
	 * The viewer frame
	 */
	private SVG_SWTViewerFrame frame;
	
	/**
	 * Horizontal index into a two dimensional array that represents a display grid of clocks
	 */
	private int startingHorizontalIndex;
	/**
	 * Vertical index into a two dimensional array that represents a display grid of clocks
	 */
	private int startingVerticalIndex;
	
	/**
	 * if the clock spans more than one index (i.e. hint wise it favors horizontal or vertical
	 * display, based on the display program it may span up to 2 squares
	 */
	private int endingHorizontalIndex = -1;
	private int endingVerticalIndex = -1;
	
	/**
	 * quick check flags
	 */
	private boolean singleSpace;
	private boolean primaryCell;
	
	/**
	 * if empty
	 */
	private boolean empty = false;
	
	/**
	 * flag for visibility
	 */
	private boolean visible = false;
	
	/**
	 * hint using the Clock constants for how this element prefers to render
	 */
	private int renderHint;
	
	
	private int previousMoveFrom;
	
	/**
	 * basic no arguments constructor
	 */
	public ClockDisplayElement()
	{
	}

	/**
	 * constructor for all arugments
	 * @param rect
	 * @param clock
	 * @param startHorIndex
	 * @param startVertIndex
	 */
	public ClockDisplayElement(Rectangle rect, Clock clock, int startHorIndex, int startVertIndex, boolean singleSpace, boolean primaryCell)
	{
	    this.rectangle = rect;
	    this.clock = clock;
	    this.startingHorizontalIndex = startHorIndex;
	    this.startingVerticalIndex = startVertIndex;
	    this.singleSpace = singleSpace;
	    this.primaryCell = primaryCell;
	    
	    if (clock != null)
	        this.renderHint = this.clock.getRenderingHint();
	    else
	        this.renderHint = Clock.SQUARE;
	}
	
    /**
     * @return Returns the clock.
     */
    public Clock getClock()
    {
        return clock;
    }
    /**
     * @param clock The clock to set.
     */
    public void setClock(Clock clock)
    {
        this.clock = clock;
    }
    /**
     * @return Returns the rectangle.
     */
    public Rectangle getRectangle()
    {
        return rectangle;
    }
    /**
     * @param rectangle The rectangle to set.
     */
    public void setRectangle(Rectangle rectangle)
    {
        this.rectangle = rectangle;
    }
    /**
     * @return Returns the startingHorizontalIndex.
     */
    public int getStartingHorizontalIndex()
    {
        return startingHorizontalIndex;
    }
    /**
     * @param startingHorizontalIndex The startingHorizontalIndex to set.
     */
    public void setStartingHorizontalIndex(int startingHorizontalIndex)
    {
        this.startingHorizontalIndex = startingHorizontalIndex;
    }
    /**
     * @return Returns the startingVerticalIndex.
     */
    public int getStartingVerticalIndex()
    {
        return startingVerticalIndex;
    }
    /**
     * @param startingVerticalIndex The startingVerticalIndex to set.
     */
    public void setStartingVerticalIndex(int startingVerticalIndex)
    {
        this.startingVerticalIndex = startingVerticalIndex;
    }
    /**
     * @return Returns the endingHorizontalIndex.
     */
    public int getEndingHorizontalIndex()
    {
        return endingHorizontalIndex;
    }
    /**
     * @param endingHorizontalIndex The endingHorizontalIndex to set.
     */
    public void setEndingHorizontalIndex(int endingHorizontalIndex)
    {
        this.endingHorizontalIndex = endingHorizontalIndex;
    }
    /**
     * @return Returns the endingVerticalIndex.
     */
    public int getEndingVerticalIndex()
    {
        return endingVerticalIndex;
    }
    /**
     * @param endingVerticalIndex The endingVerticalIndex to set.
     */
    public void setEndingVerticalIndex(int endingVerticalIndex)
    {
        this.endingVerticalIndex = endingVerticalIndex;
    }
    /**
     * @return Returns the singleSpace.
     */
    public boolean isSingleSpace()
    {
        return singleSpace;
    }
    /**
     * @param singleSpace The singleSpace to set.
     */
    public void setSingleSpace(boolean singleSpace)
    {
        this.singleSpace = singleSpace;
    }
    /**
     * @return Returns the renderHint.
     */
    public int getRenderHint()
    {
        return renderHint;
    }
    /**
     * @return Returns the primaryCell.
     */
    public boolean isPrimaryCell()
    {
        return primaryCell;
    }
    /**
     * @param primaryCell The primaryCell to set.
     */
    public void setPrimaryCell(boolean primaryCell)
    {
        this.primaryCell = primaryCell;
    }
    /**
     * @return Returns the frame.
     */
    public SVG_SWTViewerFrame getFrame()
    {
        return frame;
    }
    /**
     * @param frame The frame to set.
     */
    public void setFrame(SVG_SWTViewerFrame frame)
    {
        this.frame = frame;
    }
    /**
     * @return Returns the isVisible.
     */
    public boolean isVisible()
    {
        return visible;
    }
    /**
     * @param isVisible The isVisible to set.
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
    /**
     * @return Returns the isEmpty.
     */
    public boolean isEmpty()
    {
        return empty;
    }
    /**
     * @param isEmpty The isEmpty to set.
     */
    public void setEmpty(boolean empty)
    {
        this.empty = empty;
    }
    /**
     * @return Returns the previousMoveFrom.
     */
    public int getPreviousMoveFrom()
    {
        return previousMoveFrom;
    }
    /**
     * @param previousMoveFrom The previousMoveFrom to set.
     */
    public void setPreviousMoveFrom(int previousMoveFrom)
    {
        this.previousMoveFrom = previousMoveFrom;
    }
}