/*
Copyright(c) 2022 Microstran Inc..  All rights reserved.
 
This file contains data proprietary to Microstran Inc.
It may contain data proprietary to others, with use granted to Microstran
 under a non-disclosure agreement. Do not release this 
information to any party unless that party has signed all appropriate 
non-disclosure agreements. 
The source code for this software is not published and 
remains protected by trade secret laws, notwithstanding any deposits 
with the U.S. Copyright Office.
*/

package com.microstran.core.engine.util;

import org.apache.batik.util.ParsedURL;

import com.microstran.core.viewer.SVG_SWTViewerFrame;


/**
 * @author Mstran
 *
 * Input handler
 */
public class SVGInputHandler implements FileInputHandler 
{
     /**
     * Input for the SVG_SWTViewerFrame
     */
    public void handleInput(ParsedURL purl, SVG_SWTViewerFrame svgViewerFrame) {
        svgViewerFrame.getJSVGCanvas().loadSVGDocument(purl.toString());
    }
}
