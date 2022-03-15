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


import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * A class to wrap DOM handling and ease setting/getting elements, attributes, text,
 * 
 * @author Mike Stran
 */
public class DOMXMLParserHelper 
{

    private DOMXMLParser domXMLParser = null;
    private Document document = null;

    public DOMXMLParserHelper() 
    {
    }

    /**
     * Class constructor
     * @param document The document object to manipulate
     */
    public DOMXMLParserHelper(Document document) 
    {
        this.document = document;
    }

    /**
     * Class constructor
     * @param document document object to manipulate.
     * @param domXMLParser The parser
     */
    public DOMXMLParserHelper(Document document, DOMXMLParser domXMLParser) 
    {
        this.document = document;
        this.domXMLParser = domXMLParser;
    }

    /**
     * Sets the Document
     * @param document document object to use.
     */
    public void setDocument(Document document) 
    {
        this.document = document;
    }

    /**
     * Gets the document
     * @return Document
     */
    public Document getDocument()
    {
        return(document);
    }

    /**
     * Write the document out
     * @return true on success, false on failure
     */
    public boolean writeDocument()
    {
        return(this.domXMLParser.writeXmlFile(this.document));
    }
    
    /**
     * Gets the parser if it exists, else creates new
     * @return DOMXMLParser
     */
    public DOMXMLParser getDOMXMLParser() 
    {
        if (domXMLParser == null)
            this.domXMLParser = new DOMXMLParser();
        return(domXMLParser);
    }

    /**
     * Set the parser
     * @param domXMLParser The parser object to use.
     */
    public void setDOMXMLParser(DOMXMLParser domXMLParser) 
    {
        this.domXMLParser = domXMLParser;
    }

    /**
     * Creates a Document.
     * @return Document The generated document object.
     */
    public Document createDocument(String rootElementName, String publicID, String systemID)
    {
        try {
            DOMXMLParser domXMLParser = getDOMXMLParser();
            
            DOMImplementation domImpl = domXMLParser.getDocumentBuilder().getDOMImplementation(); 
            DocumentType docType =  domImpl.createDocumentType(rootElementName, publicID, systemID);
            Document doc = domImpl.createDocument("", rootElementName, docType);
            //setDocument(domXMLParser.getDocumentBuilder().newDocument());
            setDocument(doc);
            return(doc);
        }
        catch (Exception e) 
		{}
        return(null);
    }


    /**
     * Sets the DocumentType
     * @param documentType
     * @return DocumentType
     */
    public DocumentType setDocumentType(DocumentType documentType) 
    {
        if ((getDocument() == null) || (documentType == null))
            return null;

        try 
		{
            return((DocumentType)getDocument().appendChild(documentType));
        }
        catch (DOMException e) 
		{
        }
        return(null);
    }

    /**
     * Gets the DocumentType
     * @return DocumentType
     */
    public DocumentType getDocumentType()
    {
        if (getDocument() == null)
            return(null);
        try 
		{
            return(getDocument().getDoctype());
        }
        catch (Exception e) 
		{}
        return(null);
    }


    /**
     * Sets the root element.
     * @param rootElement 
     * @return Element reference to the new root
     */
    public Element setRootElement(Element rootElement) 
    {
        if ((getDocument() == null) || (rootElement == null))
            return(null);
        try 
		{
            return((Element)getDocument().appendChild(rootElement));
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Gets the root element in the document.
     * @return Element Reference to the root element 
     */
    public Element getRootElement()
    {
        if (getDocument() == null)
            return(null);
        try 
		{
            return(getDocument().getDocumentElement());
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Adds a child element to the root element.
     * @param elementName The name of the element to create.
     * @return Element A reference to the created element.
     */
    public Element addChildToRoot(String elementName)
    {
        if ((getDocument() == null) || elementName == null) 
            return(null);
        try 
		{
            Document document = getDocument();
            return(addChildToParent(document.getDocumentElement(), elementName));
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Adds a child element to the root element
     * @param childElement The child to be added to the root
     * @return Element
     */
    public Element addChildToRoot(Element childElement)
    {
        if ((getDocument() == null) || (childElement == null))
            return(null);
        return(addChildToParent(document.getDocumentElement(), childElement));
    }

    /** 
     * Adss a child element to parent
     * @param parent The parent element to add to.
     * @param elementName The name of the new child element
     * @return Element A reference to the created element.
     */
    public Element addChildToParent(Element parent, String elementName) 
	{
        if ((parent == null) || (elementName == null) || (getDocument() == null))
            return null;

        try 
		{
            return((Element)parent.appendChild(getDocument().createElement(elementName)));
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Adds a child element to specified parent. Child element will have 
     * the specified element name, method will also set the text of the
     * new element to the supplied value.
     * @param parent The parent of the element to create.
     * @param elementName The name of the element to create.
     * @param value The text to be added to the new element.
     * @return Element A reference to the created element.
     */
    public Element addChildToParent(Element parent, String elementName, String value) 
    {
    	Text textNode = null;

        if ((parent == null) || (elementName == null) || (value == null)) 
            return(null);
        
        try 
		{
            Element newChild = addChildToParent(parent, elementName);

            // If the child was created, add the supplied text
            // value to it.
            if (newChild != null)
                textNode = setElementText(newChild, value);

            // Verify that the text node was actually created.
            if (textNode == null)
                newChild = null;

            return(newChild);
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Adds the supplied Element to the parent.
     * @param childElement The <code>Element</code> to add to the paraent
     * @param paraentElement The parent <code>Element</code>
     * @return Element
     */
    public Element addChildToParent(Element childElement, Element parentElement)
    {
        if ((childElement == null) || (parentElement == null))
            return(null);
        try 
		{
            return((Element)parentElement.appendChild(childElement));
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Removes a child element from the parent
     * @param parentElement
     * @param childElement
     * @return the removed child element
     */
    public Element removeChildFromParent(Element parentElement, Element childElement)
    {
        if ((childElement == null) || (parentElement == null))
            return(null);
        try 
		{
            return((Element)parentElement.removeChild(childElement));
        }
        catch (Exception e) 
		{}
        return(null);
    }
    
    /**
     * Sets the text of the element
     * @param element The element to set the text value on.
     * @param value The value of the text.
     * @return Element The modified element.
     */
    public Text setElementText(Element element, String value)
    {
        if ((element == null) || (value == null) || (getDocument() == null))
            return(null);

        try 
		{
            Text textElement = getDocument().createTextNode(value);

            if ((element.getFirstChild() == null) || (element.getFirstChild().getNodeType() != Node.TEXT_NODE))
                element.appendChild(textElement);
            else
                element.replaceChild(textElement, element.getFirstChild());

            return(textElement);
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Gets the text from the element.
     * @param element The element to get the text from.
     * @return String The element text content
     */
    public String getElementText(Node element) 
	{
        String text = "";

        if (element == null)
            return(null);
        try 
		{
            if (element.getNodeType() == Node.TEXT_NODE)
                text = element.getNodeValue();
            else if (element.getNodeType() == Node.ATTRIBUTE_NODE)
                text = element.getNodeValue();
            else 
            {
                Node child = element.getFirstChild();
                do 
                {
                    if ((child != null) && (child.getNodeType() == Node.TEXT_NODE))
                        text += child.getNodeValue();
                }
                while ((child != null) && ((child = child.getNextSibling()) != null));
            }

            return(text);
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Gets all of the attributes for the Element
     * @param element The Element to retrieve attributes for.
     * @return NamedNodeMap 
     */
    public NamedNodeMap getElementAttributes(Element element) 
    {
        if (element == null)
            return(null);
        return(element.getAttributes());
    }

    /**
     * Gets the specified attribute
     * @param element The Element to get the attribute from.
     * @param attributeName The name of the attribute to retrieve.
     * @return String The attribute value
     */
    public String getElementAttributeValue(Element element, String attributeName) 
    {
        if ((element == null) || (attributeName == null))
            return(null);
        return(element.getAttribute(attributeName));
    }

    /**
     * Gets the specified attribute from the element.
     * @param element The Element to get the attribute from.
     * @return Attr The attribute element.
     */
    public Attr getElementAttribute(Element element, String attributeName) 
    {
        if ((element == null) || (attributeName == null))
            return(null);
        return(element.getAttributeNode(attributeName));
    }

    /**
     * Adds an attribute to the element
     * @param element The element to add to
     * @param attributeName The name of the new attribute.
     * @param attributeValue The value of the attribute.
     * @return Attr The created attribute.
     */
    public boolean addAttributeToElement(Element element, String attributeName, String attributeValue) 
    {
        if ((element == null) || (attributeName == null))
            return(false);
        try 
		{
            element.setAttribute(attributeName, attributeValue);
            return(true);
        }
        catch (Exception e) 
		{}
        return(false);
    }

    /**
     * Gets the elements with the supplied name related to the element.
     * A supplied element name of "*" will return all elements under the supplied element.
     * @param element The element to search from for the target elements.
     * @param elementName The name of the element to search for.
     * @return NodeList A list of nodes found to match the supplied name.
     */
    public NodeList getElementsByTagName(Element element, String elementName)
    {
        if ((element == null) || (elementName == null))
            return(null);
        try 
		{
            return(element.getElementsByTagName(elementName));
        }
        catch (Exception e) 
		{}
        return(null);
    }

    /**
     * Retrieves the elements with the supplied name that is a descendant of the supplied element.
     * Alternative form to retrieve elements from document
     * a supplied element name of "*" will return all elements under the element.
     * @param elementName The name of the element to search for.
     * @return NodeList A list of nodes found to match the supplied name.
     */
    public NodeList getElementsByTagName(String elementName) 
    {
        if (getDocument() == null)
            return(null);
        Element rootElement = getRootElement();
        return(getElementsByTagName(rootElement, elementName));
    }

    /**
     * Gets the element with the supplied name that is a descendant of the supplied element.
     * If an element name of "*" is supplied,then the first element will be returned.
     * @param element The element used to search from.
     * @param elementName The name of the element to search for.
     * @return Element The found element.
     */
    public Element getElementByTagName(Element element, String elementName) 
    {
        NodeList nodes = getElementsByTagName(element, elementName);

        if (nodes != null)
            return((Element)nodes.item(0));
        else
            return(null);
    }

    /**
     * Gets the element with the supplied name that is a 
     * descendant of the supplied element.
     * If an element name of "*" is supplied, then the first element will be returned.
     * @param elementName The name of the element to retrieve.
     * @return Element The retrieved element.
     */
    public Element getElementByTagName(String elementName) 
    {
        if (getDocument() == null)
            return(null);
        Element rootElement = getRootElement();
        return(getElementByTagName(rootElement, elementName));
    }
  
  }