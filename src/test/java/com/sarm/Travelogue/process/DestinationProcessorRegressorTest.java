/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sarm.Travelogue.process;

import com.sarm.Travelogue.common.LonelyConstants;
import com.sarm.Travelogue.model.Destination;
import com.sarm.Travelogue.model.Destinations;
import static com.sarm.Travelogue.process.DestinationProcessorTest.logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is the test class for regressively test the Destination Processor's StAX
 * marshaller. Reason of having the regression in a separate class is because it
 * will be easy to ignore it when a regression is not needed as it takes times.
 * Regression will only be needed when there is a significant change to the StAX
 * marshalling functionality of Destinations.
 *
 * @author sarm
 */
public class DestinationProcessorRegressorTest {

    private static String taxonomyFileName;
    private static String regressDestinationfile;

    @BeforeClass
    public static void setUpClass() {
        logger.info("DestinationProcessorRegressorTest  Commencing loading test properties ...");
        Properties prop = new Properties();

        String propFileName = LonelyConstants.testPropertyFile;

        try (InputStream input = new FileInputStream(propFileName)) {

            if (input == null) {
                logger.debug("input Stream for test.properties file : is Null  ");
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            logger.debug("Loading properties file   " + propFileName);

            prop.load(input);
        } catch (FileNotFoundException ex) {
            logger.debug("FileNotFoundException  " + propFileName);
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.debug("IOException  " + propFileName);
            ex.printStackTrace();
        }

        regressDestinationfile = prop.getProperty(LonelyConstants.regressDestinationfile);

        try {
            testMarshall(".//destinations.xml");
        } catch (JAXBException | FileNotFoundException | XMLStreamException | UnsupportedEncodingException ex) {
            ex.printStackTrace();

        }

    }

    /**
     * Test of processDestinationByStAX method, of class DestinationProcessor.
     * The destinationFile is a test file with approximately 30000 destination
     * This file is created by the normal destinations file and added them over
     * again to make up 30000 destinations.
     */
    @Test
    public void testProcessDestinationByStAXfor30000() throws Exception {
        logger.info("Testing processDestinationByStAX30000");

        String destinationFileName = regressDestinationfile;
        DestinationProcessor destinationProcessor = new DestinationProcessor();
        int expResult = 36000;
        List<Destination> result = destinationProcessor.processDestinationByStAX(regressDestinationfile);

        assertEquals(expResult, result.size());
        logger.info("Successfully Parsed  : " + result.size() + " destinations ...");
    }

    /**
     * This method is not a JUnit test method. This is used to create an XML
     * file of 30000 destinations and to regressively test the UnMarshalling
     * technique used in the DestinationProcessor. This is called by the test
     * method : testProcessDestinationByStAXfor30000 to test the 30000
     * destinations.
     *
     * @param destinationFileName
     * @throws JAXBException
     * @throws FileNotFoundException
     * @throws XMLStreamException
     * @throws UnsupportedEncodingException
     */
    public static void testMarshall(String destinationFileName) throws JAXBException, FileNotFoundException, XMLStreamException, UnsupportedEncodingException {
        DestinationProcessor destinationProcessor = new DestinationProcessor();
        List<Destination> destinations = destinationProcessor.processDestinationByStAX(destinationFileName);
        List<Destination> tempDestinations = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            for (Destination destination : destinations) {
                Destination temp = new Destination();
                temp = destination.clone();
                temp.setTitle(temp.getTitle() + i);
                tempDestinations.add(temp);
            }
        }
        Destinations destinationList = new Destinations();
        destinationList.setDestinations(tempDestinations);
        logger.info(" destinationList " + destinationList.getDestinations().size());

        JAXBContext jc = JAXBContext.newInstance(Destinations.class);
        logger.debug(jc.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(destinationList, new File(regressDestinationfile));
        logger.info("Successfully created regressDestinationfile " + regressDestinationfile);

    }
    
     @AfterClass
    public static void tearDownClass() {
       
            logger.info("Tearing down DestinationProcessorTest and deleting the 30000dests.xml file");
            File file = new File(regressDestinationfile);
            file.delete();
          
          
    }
}
