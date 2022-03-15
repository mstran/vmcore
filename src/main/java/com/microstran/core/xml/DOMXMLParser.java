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

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


/**
 * Derived class for parsing XML using DOM.
 *
 * @author Mike Stran
 */
public class DOMXMLParser extends AbstractXMLParser 
{
    
    public DOMXMLParser() 
    {
        super();
    }
    
    /**
     * Parses document and returns a helper object
     * @return DomXMLParserHelper
     */
    public DOMXMLParserHelper parseIntoDOMXMLParserHelper() 
    {
        Document document = parse();
        return(new DOMXMLParserHelper(document, this));
    }
    
    /**
	 * Write the file
	 * 
	 * @return true on success, false on failure
	 */
    public boolean writeXmlFile(Document document) 
    {
        try 
        {
            // Prepare the DOM document for writing
            Source source = new DOMSource(document);
    
            /**
             *  Prepare the output file
             *  Saves the last version in case of write failure - 
             *  Removed 2.9.2022 so don't have to grant permission to write to the install folder under windows!
             *  Only granting write permission to just the configuration.xml file in the install folder
             *  For now the system will no longer create a configuration.xml.bak file
             */
            File fileNew = new File(filePath);
            /*
            File fileOld = new File(filePath + ".bak");
            if (fileOld.exists())
                fileOld.delete();
            if (fileNew.exists())
                fileNew.renameTo(fileOld);
            */
            File file 	  = new File(filePath);
            /**
             * CRITICAL NOTE: You must have file.getPath() in the stream result or 
             * it will FAIL under windows to find anything in a folder that has
             * a space in the name!!! like, for example, "c:\Program Files (x86)\vmcc"
             */
            Result result = new StreamResult(file.getPath());
    
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            return(true);
        } 
        catch (TransformerConfigurationException e)
        {
            System.out.println(e.getMessage());
        } 
        catch (TransformerException e) 
        {
            System.out.println(e.getMessage());
        } 
        return(false);
    }
    
    /**
     * Implementation of parse from abstract class 
     * @return Document
     */
    protected Document parse()
    {
        Document document = null;
        
        if ( (input != null) && (domBuilder != null) ) 
        {
        	try 
			{
                document = domBuilder.parse(input);
            }
            catch (Exception e) 
			{
            }
        }
        return(document);
    }
    
    
}