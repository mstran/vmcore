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

package com.microstran.core.xml;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base class for DOM Parsing.
 * 
 * @author Mike Stran
 */
public abstract class AbstractXMLParser 
{
    
    protected DocumentBuilderFactory domFactory;
    protected DocumentBuilder domBuilder;
    
    protected InputSource input;
    protected String filePath;
    
    
    public AbstractXMLParser() 
    {
        createParser();
    }
    
    
    /**
     * Creates and sets up the parser objects.
     */
    private void createParser() 
    {
        try {
            // Create the factory
            this.domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            
            domFactory.setIgnoringComments(true);
            domFactory.setExpandEntityReferences(false);
    
            this.domBuilder = domFactory.newDocumentBuilder();
            
            domBuilder.setErrorHandler(new DefaultHandler());
        }
        catch(Exception e) 
		{
            System.out.println("cannot create parser");
        }
    }
    
    /**
     * Sets the ErrorHandler to be used. 
     * @param errorHandler 
     */
    public void setErrorHandler(ErrorHandler errorHandler) 
    {
        if ( (errorHandler != null) && (domBuilder != null) )
            domBuilder.setErrorHandler(errorHandler);
    }
    
    /**
     * Sets the input source for the XML Parser
     * @param input InputStream The source of the XML Document to
     */
    public void setXMLInputSource(InputStream input) 
    {
        if (input != null)
            this.input = new InputSource(input);
    }
    
    /**
     * Sets the input source for the XML Parser
     * @param reader Reader The source of the XML Document
     */
    public void setXMLInputSource(Reader reader) 
    {
        if (reader != null)
            this.input = new InputSource(reader);
    }
    
    /**
     * Sets the input source for the XML Parser
     * @param systemId String The source of the XML Document 
     */
    public void setXMLSource(String filePath) 
    {
        if (filePath != null)
        {
            this.input = new InputSource(filePath);
            this.filePath = filePath;
        }
        
        
    }
    
    /**
     * Sets the input source for the XML Parser
     * @param systemId String The source of the XML Document 
     */
    public void setXMLSourceURL(URL url)
    {
        if (url != null)
        {
            this.input = new InputSource(url.toString());
            this.filePath = url.toExternalForm();
        }
    }
    
    /**
     * Returns the DocumentBuilder (was built internally for parse).
     * @return DocumentBuilder
     */
    public DocumentBuilder getDocumentBuilder() 
    { 
    	return(domBuilder); 
    }
    
    /**
     * Abstract method, will be implemented in derived class 
     * @return Document
     */
    protected abstract Document parse();
    
    /**
     * Abstract method, will be implemented in derived class 
     * @return Document
     */
    protected abstract boolean writeXmlFile(Document doc);
    
    /**
     * @return Returns the filePath.
     */
    public String getFilePath()
    {
        return filePath;
    }
}