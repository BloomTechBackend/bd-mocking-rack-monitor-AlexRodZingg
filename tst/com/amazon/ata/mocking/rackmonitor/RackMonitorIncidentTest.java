package com.amazon.ata.mocking.rackmonitor;

import com.amazon.ata.mocking.rackmonitor.clients.warranty.Warranty;
import com.amazon.ata.mocking.rackmonitor.clients.warranty.WarrantyClient;
import com.amazon.ata.mocking.rackmonitor.clients.warranty.WarrantyNotFoundException;
import com.amazon.ata.mocking.rackmonitor.clients.wingnut.WingnutClient;
import com.amazon.ata.mocking.rackmonitor.exceptions.RackMonitorException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;  // initMocks is deprecated, hence the strikeout
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Mockito is framework for mocking in tests
// A framework is a set of classes that make common programming requirements easier
// Mockito is popular because it is easy to use and free

public class RackMonitorIncidentTest {
    // Instantiate objects used in the test
    RackMonitor rackMonitor;        // The class with the methods we are testing

// BEFORE Mocking we need to define references to any external class objects
//    WingnutClient wingnutClient;    // Reference Object that may be used in a test
//    WarrantyClient warrantyClient;  // Reference Object that may be used in a test
//    Rack rack1;                     // Reference Object that may be used in a test

// USING Mocks we define external class objects for Mocking
// Mockito will manage use of the Mocked objects in the tests
@Mock
    WingnutClient wingnutClient;    // Reference Object that may be used in a test managed by Mockito
@Mock
    WarrantyClient warrantyClient;  // Reference Object that may be used in a test managed by Mockito
@Mock
    Rack rack1;                     // Reference Object that may be used in a test managed by Mockito

    // Since we are using Mocking we no longer need multiple instances of test object
//    Server unhealthyServer = new Server("TEST0001");    // Unhealthy server object (healthfactor < .8)
//    Server shakyServer = new Server("TEST0067");        // Shaky server object (healthfactor between .8 and .9)

    // When using Mocking we usually only need one instance of a test object
    // Set attribute the test object needs just before we use it
    Server aServer = new Server("TEST001");

// Define any return values from any Mock'd method calls
// We plan on Mock'ing the Rack.getHealth() method call - it returns a Map<Server, Double>

    Map<Server, Double> serverHealth;                          // Hold the return value from Mock'd getHealth() call
    Map<Server, Integer> rack1ServerUnits;                     // Hold the servers we are testing

    @BeforeEach // Do this before each test is run
    void setUp() {
        // BEFORE Mocking - instantiate objects used in the test and assign them to their reference
//        warrantyClient = new WarrantyClient();    // These are @Mock'd, so we don't instantiate them
//        wingnutClient = new WingnutClient();
//        rack1 = new Rack("RACK01", rack1ServerUnits);

        initMocks(this);    // Tells Mockito to prepare the @Mock objects for use
                                    // initMocks() is deprecated, hence the strikeout; it's going to go away sometime
        // MockitoAnnotations.openMocks(this); // replacement for deprecated initMocks(this)

        serverHealth = new HashMap<>();             // Instantiate the Map to be returned from Mock'd getHealth()

        rack1ServerUnits = new HashMap<>();         // Map of servers in our Rack for testing

        rack1ServerUnits.put(aServer, 1);   // place our unhealthy server in server map with id of 1


        // Define our RackMonitor with the Rack Object, WingnutClient Object, WarrantyClient Object, health thresholds
        rackMonitor = new RackMonitor(new HashSet<>(Arrays.asList(rack1)),
            wingnutClient, warrantyClient, 0.9D, 0.8D);
        //                                          shaky,      unhealthy
        //                                      threshold       threshold
    }

    @Test
    public void getIncidents_withOneUnhealthyServer_createsOneReplaceIncident() throws Exception {
        // GIVEN
        // Since we are using Mock'd objects we need to tell Mockito what the calls should return
        // Since we no longer have real objects with real methods, there are no real methods to call
        // We tell Mockito when you see a method call with certain parameters return a specific result

        // Need to define the value to be returned from the Mock'd method
        // The rack is set up with a single unhealthy server - unhealthy = healthfactor < .8
        serverHealth.put(aServer, .5);  // return data from Mock'd getHealth() call

        // What you are telling Mockito is....
        // When you see this method call.return-this-value
        when(rack1.getHealth()).thenReturn(serverHealth);  // Tell Mockito to return our serverHealth object when getHealth is called

        when(rack1.getUnitForServer(aServer)).thenReturn(1);    // Tell Mockito to return our test server id (1)
                                                                        // when a Rack is used to call getUnitForServer

        // Tell Mockito return a Warranty when the warrantyClient.getWarrantyForServer() is called with a server
        when(warrantyClient.getWarrantyForServer(aServer)).thenReturn(Warranty.nullWarranty());

        // We've reported the unhealthy server to Wingnut
        rackMonitor.monitorRacks();

        // WHEN
        Set<HealthIncident> actualIncidents = rackMonitor.getIncidents();

        // THEN
        HealthIncident expected =
            new HealthIncident(aServer, rack1, 1, RequestAction.REPLACE);
        assertTrue(actualIncidents.contains(expected),
            "Monitoring an unhealthy server should record a REPLACE incident!");
    }

    @Test
    public void getIncidents_withOneShakyServer_createsOneInspectIncident() throws Exception {
        // GIVEN
        // The rack is set up with a single shaky server

// Because we are using Mock'd objects, there is no need to define real data
//        rack1ServerUnits = new HashMap<>();
//        rack1ServerUnits.put(aServer, 1);
//        rack1 = new Rack("RACK01", rack1ServerUnits);
//  no need to define a RackMonitor object as it is already done in the SetUp() in the @BeforeEach
//        rackMonitor = new RackMonitor(new HashSet<>(Arrays.asList(rack1)),
//            wingnutClient, warrantyClient, 0.9D, 0.8D);
        // We've reported the shaky server to Wingnut
        // Need to define the value to be returned from the Mock'd method
        // The rack is set up with a single shaky server - unhealthy = healthfactor between .81 and .9
        serverHealth.put(aServer, .85);  // return data from Mock'd getHealth() call

        // What you are telling Mockito is....
        // When you see this method call.return-this-value
        when(rack1.getHealth()).thenReturn(serverHealth);  // Tell Mockito to return our serverHealth object when getHealth is called

        when(rack1.getUnitForServer(aServer)).thenReturn(1);    // Tell Mockito to return our test server id (1)
        // when a Rack is used to call getUnitForServer

        // Tell Mockito return a Warranty when the warrantyClient.getWarrantyForServer() is called with a server
        when(warrantyClient.getWarrantyForServer(aServer)).thenReturn(Warranty.nullWarranty());


        rackMonitor.monitorRacks();

        // WHEN
        Set<HealthIncident> actualIncidents = rackMonitor.getIncidents();

        // THEN
        HealthIncident expected =
            new HealthIncident(aServer, rack1, 1, RequestAction.INSPECT);
        assertTrue(actualIncidents.contains(expected),
            "Monitoring a shaky server should record an INSPECT incident!");
    }

    @Test
    public void getIncidents_withOneHealthyServer_createsNoIncidents() throws Exception {
        // GIVEN
        // monitorRacks() will find only healthy servers
        // Need to define the value to be returned from the Mock'd method
        // The rack is set up with a single healthy server - healthy = healthfactor > .9
        serverHealth.put(aServer, .91);  // return data from Mock'd getHealth() call

        // What you are telling Mockito is....
        // When you see this method call.return-this-value
        when(rack1.getHealth()).thenReturn(serverHealth);  // Tell Mockito to return our serverHealth object when getHealth is called

        when(rack1.getUnitForServer(aServer)).thenReturn(1);    // Tell Mockito to return our test server id (1)
        // when a Rack is used to call getUnitForServer

        // Tell Mockito return a Warranty when the warrantyClient.getWarrantyForServer() is called with a server
        when(warrantyClient.getWarrantyForServer(aServer)).thenReturn(Warranty.nullWarranty());

        // monitorRacks() will find only healthy servers
        rackMonitor.monitorRacks();

        // WHEN
        Set<HealthIncident> actualIncidents = rackMonitor.getIncidents();

        // THEN
        assertEquals(0, actualIncidents.size(),
            "Monitoring a healthy server should record no incidents!");
    }

    @Test
    public void monitorRacks_withOneUnhealthyServer_replacesServer() throws Exception {
        // GIVEN
        // Need to define the value to be returned from the Mock'd method
        // The rack is set up with a single unhealthy server - unhealthy = healthfactor < .8
        serverHealth.put(aServer, .63);  // return data from Mock'd getHealth() call

        // What you are telling Mockito is....
        // When you see this method call.return-this-value
        when(rack1.getHealth()).thenReturn(serverHealth);  // Tell Mockito to return our serverHealth object when getHealth is called

        when(rack1.getUnitForServer(aServer)).thenReturn(1);    // Tell Mockito to return our test server id (1)
        // when a Rack is used to call getUnitForServer

        // Tell Mockito return a Warranty when the warrantyClient.getWarrantyForServer() is called with a server
        when(warrantyClient.getWarrantyForServer(aServer)).thenReturn(Warranty.nullWarranty());

        // WHEN
        rackMonitor.monitorRacks();

        // THEN
        // There were no exceptions
        // No way to tell we called the warrantyClient for the server's Warranty
        // UNLESS you use Mockito's verify() method
        verify(warrantyClient).getWarrantyForServer(aServer);   // verify the getWarrantyForServer() was called at least once

        // No way to tell we called Wingnut to replace the server
        // UNLESS we use Mockito's verify() method
        verify(wingnutClient).requestReplacement(rack1, 1, Warranty.nullWarranty()); // verify requestReplacement() was called at least once
    }

    @Test
    public void monitorRacks_withUnwarrantiedServer_throwsServerException() throws Exception {
        // GIVEN
// No need to define real data since we are using Mock'd objects
//        Server noWarrantyServer = new Server("TEST0052");
//        rack1ServerUnits = new HashMap<>();
//        rack1ServerUnits.put(noWarrantyServer, 1);
//        rack1 = new Rack("RACK01", rack1ServerUnits);
//        rackMonitor = new RackMonitor(new HashSet<>(Arrays.asList(rack1)),
//            wingnutClient, warrantyClient, 0.9D, 0.8D);

        // Need to define the value to be returned from the Mock'd method
        // The rack is set up with a single healthy server - healthy = healthfactor > .9
        serverHealth.put(aServer, .63);  // return data from Mock'd getHealth() call

        // What you are telling Mockito is....
        // When you see this method call.return-this-value
        when(rack1.getHealth()).thenReturn(serverHealth);  // Tell Mockito to return our serverHealth object when getHealth is called

        when(rack1.getUnitForServer(aServer)).thenReturn(1);    // Tell Mockito to return our test server id (1)
                                                                    // when a Rack is used to call getUnitForServer

        // Tell Mockito to throw a WarrantyNotFoundException custom exceptions
        //      when the warrantyClient.getWarrantyForServer() is called with a server
        when(warrantyClient.getWarrantyForServer(aServer)).thenThrow(WarrantyNotFoundException.class);

        // WHEN and THEN
        assertThrows(RackMonitorException.class,
            () -> rackMonitor.monitorRacks(),    // This uses a Java Lambda expression
            "Monitoring a server with no warranty should throw exception!");
    }
}
