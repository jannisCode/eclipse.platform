//
// EnclosingTargetSearchingHandlerTest.java
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ui.externaltools.internal.ant.editor.EnclosingTargetSearchingHandler;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Tests the parsing using the EnclosingTargetSearchingHandler.
 * 
 * @version 19.11.2002
 * @author Alf Schiefelbein
 */
public class EnclosingTargetSearchingHandlerTest extends TestCase {

    /**
     * Constructor for EnclosingTargetSearchingHandlerTest.
     * @param name
     */
    public EnclosingTargetSearchingHandlerTest(String name) {
        super(name);
    }


	/**
	 * The test suite.
	 */
    public static Test suite() {
        TestSuite suite = new TestSuite("EnclosingTargetSearchingHandlerTest");
        
        suite.addTest(new EnclosingTargetSearchingHandlerTest("testParsingOfBuildFileWithTargetElement"));
        suite.addTest(new EnclosingTargetSearchingHandlerTest("testParsingOfBuildFileWithoutTargetElement"));
        suite.addTest(new EnclosingTargetSearchingHandlerTest("testParsingOfEmptyBuildFile"));
        return suite;
    }
	
    
    /**
     * Tests parsing an XML file with the use of our PlantySaxDefaultHandler.
     */
    public void testParsingOfBuildFileWithoutTargetElement() throws SAXException, ParserConfigurationException, IOException {
        SAXParser tempParser = SAXParserFactory.newInstance().newSAXParser();
		URL url= getClass().getResource("test1.xml");
		File file= new File(url.getFile());
        EnclosingTargetSearchingHandler tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 4, 8);
        InputStream tempStream = getClass().getResourceAsStream("test1.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        Element tempElement = tempHandler.getParentElement(false);
        assertNull(tempElement);
        tempElement = tempHandler.getParentElement(true);
        assertNull(tempElement);

		tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 2, 0);
        tempStream = getClass().getResourceAsStream("test1.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        tempElement = tempHandler.getParentElement(false);
        assertNull(tempElement);
        tempElement = tempHandler.getParentElement(true);
        assertNull(tempElement);

        tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        tempStream = getClass().getResourceAsStream("test1.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        tempElement = tempHandler.getParentElement(false);
        assertNull(tempElement);
        tempElement = tempHandler.getParentElement(true);
        assertNull(tempElement);
    }
    
    
    /**
     * Tests parsing an XML file with the use of our PlantySaxDefaultHandler.
     */
    public void testParsingOfBuildFileWithTargetElement() throws SAXException, ParserConfigurationException, IOException {
        SAXParser tempParser = SAXParserFactory.newInstance().newSAXParser();
		URL url= getClass().getResource("russianbuild.xml");
		File file= new File(url.getFile());
        EnclosingTargetSearchingHandler tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 5, 5);
        InputStream tempStream = getClass().getResourceAsStream("russianbuild.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        Element tempElement = tempHandler.getParentElement(false);
        assertNotNull(tempElement);
		assertEquals("target", tempElement.getTagName());
        tempElement = tempHandler.getParentElement(true);
        assertNotNull(tempElement);
		assertEquals("target", tempElement.getTagName());
		assertEquals("init", tempElement.getAttribute("name"));

        tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 7, 0);
        tempStream = getClass().getResourceAsStream("russianbuild.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        tempElement = tempHandler.getParentElement(false);
        assertNull(tempElement);
        tempElement = tempHandler.getParentElement(true);
        assertNull(tempElement);

        tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        tempStream = getClass().getResourceAsStream("russianbuild.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        tempElement = tempHandler.getParentElement(false);
        assertNull(tempElement);
        tempElement = tempHandler.getParentElement(true);
        assertNull(tempElement);
    }
    
    /**
     * Tests parsing an XML file with the use of our PlantySaxDefaultHandler.
     */
    public void testParsingOfEmptyBuildFile() throws SAXException, ParserConfigurationException, IOException {
        SAXParser tempParser = SAXParserFactory.newInstance().newSAXParser();
		URL url= getClass().getResource("emptyfile.xml");
		File file= new File(url.getFile());
        EnclosingTargetSearchingHandler tempHandler = new EnclosingTargetSearchingHandler(file.getParentFile(), 0, 0);
        InputStream tempStream = getClass().getResourceAsStream("emptyfile.xml");
        try {
            tempParser.parse(tempStream, tempHandler);
        } catch(SAXParseException e) {
        }
        Element tempElement = tempHandler.getParentElement(false);
        assertNull(tempElement);
        tempElement = tempHandler.getParentElement(true);
        assertNull(tempElement);
    }
}
