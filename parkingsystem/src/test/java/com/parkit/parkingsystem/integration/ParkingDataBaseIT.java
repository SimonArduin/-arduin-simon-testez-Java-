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
import static org.mockito.Mockito.*;

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
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        //setting up standard conditions for testing
        lenient().when(inputReaderUtil.readSelection()).thenReturn(1); //when asked a vehicle type, user chooses "car"
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        //given standard conditions
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //when an incoming vehicle is processed
        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //then there is exactly 1 ticket with this vehicleRegNumber in the database
        assertEquals(ticketDAO.getNbTicket(vehicleRegNumber), 1);

        //then the parking spot of the ticket is not available
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit(){
        //given standard conditions
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //given there is an open ticket for the vehicle in the database
        Ticket ticket = new Ticket();
        Date inTime = new Date();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR,false));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setInTime(new Date(inTime.getTime() - hourInMillis));
        ticketDAO.saveTicket(ticket);

        //when an exiting vehicle is processed
        parkingService.processExitingVehicle();

        //then the ticket recorded in the database has the correct outTime
        ticket = ticketDAO.getTicket(vehicleRegNumber);
        Date expectedOutTime = new Date();
        Date ticketOutTime = ticket.getOutTime();
        assertEquals(ticket.getOutTime().getTime(), expectedOutTime.getTime(), 500);
        /*ticket.getOutTime and expectedOutTime are not exactly equal
        because the database rounds time to the nearest second */

        //then the ticket recorded in the database has a price
        assertNotEquals(null, ticket.getPrice());
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        //given standard conditions
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //given there is a previous ticket for the vehicle in the database
        Date currentTime = new Date();
        FareCalculatorService fareCalculatorService =  new FareCalculatorService();
        Ticket firstTicket = new Ticket();
        firstTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR,false));
        firstTicket.setVehicleRegNumber(vehicleRegNumber);
        firstTicket.setPrice(0);
        firstTicket.setInTime(new Date(currentTime.getTime() - 5 * hourInMillis));
        firstTicket.setOutTime(new Date(currentTime.getTime() - 3 * hourInMillis));
        ticketDAO.saveTicket(firstTicket);
        fareCalculatorService.calculateFare(firstTicket);
        
        //given the vehicle has been parked for 2 hours
        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Ticket secondTicket = ticketDAO.getTicket(vehicleRegNumber);
        currentTime.setTime(System.currentTimeMillis());
        secondTicket.setInTime(new Date(currentTime.getTime() - 2 * hourInMillis));
        secondTicket.setOutTime(currentTime);
        ticketDAO.updateTicket(secondTicket);

        //when the exiting vehicle is processed
        parkingService.processExitingVehicle();

        //then the price of the second ticket is discounted
        secondTicket = ticketDAO.getTicket(vehicleRegNumber);
        assertEquals(fareCalculatorService.discountRate * firstTicket.getPrice(), secondTicket.getPrice(), 0.005);
        /*ticket prices may not be mathematically equal
        because the database rounds time to nearest second
        which can cause the duration of the two tickets to be slightly different
        they should not be different by more than a cent */
    }

}
