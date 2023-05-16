package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private long hourInMillis = 60 * 60 * 1000;
    private String vehicleRegNumber = "ABCDEF";
    
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(ticketDAO.getNbTicket(vehicleRegNumber), 1);
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertFalse(parkingSpot.isAvailable());
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        ticket = ticketDAO.getTicket(vehicleRegNumber);
        //check correct time in database
        Date expectedOutTime = new Date();
        Date ticketOutTime = ticket.getOutTime();
        assertNotEquals(null, ticket.getOutTime());
        assertEquals(ticket.getOutTime().getTime(), expectedOutTime.getTime(), 1000);

        //check correct fare in database
        assertNotEquals(null, ticket.getPrice());
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        FareCalculatorService fareCalculatorService =  new FareCalculatorService();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        try {
            parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        } catch(Exception e){
            e.printStackTrace();
        }
        Date firstInTime = new Date();
        Date firstOutTime = new Date();
        Date secondInTime = new Date();
        Date secondOutTime = new Date();

        //add first ticket to database
        firstInTime.setTime(System.currentTimeMillis() - 5 * hourInMillis);
        firstOutTime.setTime(System.currentTimeMillis() - 3 * hourInMillis);
        Ticket firstTicket = new Ticket();
        firstTicket.setParkingSpot(parkingSpot);
        firstTicket.setVehicleRegNumber(vehicleRegNumber);
        firstTicket.setPrice(0);
        firstTicket.setInTime(firstInTime);
        firstTicket.setOutTime(firstOutTime);
        ticketDAO.saveTicket(firstTicket);
        fareCalculatorService.calculateFare(firstTicket);
        
        //enter vehicle
        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //set second ticket inTime to 2 hours ago
        Ticket secondTicket = ticketDAO.getTicket(vehicleRegNumber);
        secondInTime = secondTicket.getInTime();
        secondInTime.setTime(secondInTime.getTime() - 2 * hourInMillis);
        secondTicket.setInTime(secondInTime);
        secondOutTime.setTime(System.currentTimeMillis());
        secondTicket.setOutTime(secondOutTime);
        ticketDAO.updateTicket(secondTicket);

        //exit vehicle
        parkingService.processExitingVehicle();
        secondTicket = ticketDAO.getTicket(vehicleRegNumber);
        assertEquals(fareCalculatorService.discountRate * firstTicket.getPrice(), secondTicket.getPrice(), 0.01);
    }

}
