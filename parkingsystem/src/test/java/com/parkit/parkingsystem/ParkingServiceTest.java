package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            lenient().when(ticketDAO.getNbTicket(anyString())).thenReturn(0);

            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try{
            verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to verify mock inputReaderUtil.readVehicleRegistrationNumber");
        }
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testProcessIncomingVehicleIfReturningCustomer(){
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to verify mock inputReaderUtil.readVehicleRegistrationNumber");
        }
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testProcessIncomingVehicleIfBike(){
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to verify mock inputReaderUtil.readVehicleRegistrationNumber");
        }
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testProcessIncomingVehicleIfUnableProcess(){
        assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());
    }

    @Test
    public void processExitingVehicleTest(){
        parkingService.processExitingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }
    
    @Test
    public void processExitingVehicleTestIfReturningCustomer(){
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        parkingService.processExitingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }
    
    @Test
    public void processExitingVehicleTestUnableUpdate(){
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        try{
            parkingService.getNextParkingNumberIfAvailable();
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to run parkingService.getNextParkingNumberIfAvailable");
        }
        
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());

        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
    }

}
