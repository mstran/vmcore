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
package com.microstran.core.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class DrawingPanel extends JPanel
{
  private static final long serialVersionUID = 2182963812442525739L;

  Image image = null; 

  public DrawingPanel() 
  { 
  } 

  public void paintComponent(Graphics g)
  { 
   // Paint background unless you want to implement custom painting
   super.paintComponent(g); 

   if (image == null)
   		return;
   
   //create a scaled instance
   int sizePanelXOriginal  = getSize().width;
   int sizePanelYOriginal  = getSize().height;
   int imageWidth  = image.getWidth(this);
   int imageHeight = image.getHeight(this);
   
   int sizePanelX = sizePanelXOriginal;
   int sizePanelY = sizePanelYOriginal;
   
   double targetRatio = (double)sizePanelXOriginal / (double)sizePanelYOriginal;
   
   double imageRatio = (double)imageWidth / (double)imageHeight;
   if (targetRatio < imageRatio) 
   {
       sizePanelY = (int)(sizePanelXOriginal / imageRatio);
       if (sizePanelY <= 0)
           sizePanelY = 1;
   } 
   else 
   {
       sizePanelX = (int)(sizePanelYOriginal * imageRatio);
       if (sizePanelX <= 0)
           sizePanelX = 1;
   }

   // draw original image to scaled image 
   BufferedImage panelImage = new BufferedImage(sizePanelX, sizePanelY, BufferedImage.TYPE_INT_RGB);
   Graphics2D smallImg = panelImage.createGraphics();
   smallImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
   smallImg.drawImage(image, 0, 0, sizePanelX, sizePanelY, null);
   
   // Use the image width & height to find the starting point
   int imgX = (sizePanelXOriginal/2) - (sizePanelX/2); 
   int imgY = (sizePanelYOriginal/2) - (sizePanelY/2);

   if (imgX < 0)
   		imgX *= -1;
   if (imgY < 0)
   		imgY *= -1;
   
   //Draw image at center of the panel    
   g.drawImage(panelImage, imgX, imgY, this);
  } 
  
  /**
   * @param image The image to set.
   */
  public void setImage(java.awt.Image image) 
  {
  	this.image = image;
  }
  
} 