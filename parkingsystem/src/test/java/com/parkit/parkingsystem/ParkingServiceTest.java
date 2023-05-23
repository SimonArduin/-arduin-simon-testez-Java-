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

    private long hourInMillis = 60 * 60 * 1000;
    private String vehicleRegNumber = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            //setting up standard conditions for testing
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
            lenient().when(inputReaderUtil.readSelection()).thenReturn(1); //when asked a vehicle type, user chooses "car"

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (hourInMillis)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegNumber);
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            lenient().when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
            
            lenient().when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle(){
        //given standard conditions

        //when an incoming vehicle is processed
        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //then the following methods are called
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
        /*given standard conditions
        except there is a previous ticket for this vehicle */

        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        //when an incoming vehicle is processed
        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //then the following methods are called
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
        /*given standard conditions
        except that the incoming vehicle is a bike */

        when(inputReaderUtil.readSelection()).thenReturn(2);

        //when an incoming vehicle is processed
        try{
            parkingService.processIncomingVehicle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //then the following methods are called
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
        /*given standard conditions
        except that there is no available parking spot */

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

        //when an incoming vehicle is processed
        //then an exception is thrown
        assertThrows(Exception.class, () -> parkingService.processIncomingVehicle());
    }

    @Test
    public void processExitingVehicleTest(){
        //given standard conditions
        //when an exiting vehicle is processed
        parkingService.processExitingVehicle();
        
        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }

    @Test
    public void processExitingVehicleTestIfBike(){
        /*given standard conditions
        except that the vehicel is a bike*/
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (hourInMillis)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegNumber);

        //when an exiting vehicle is processed
        parkingService.processExitingVehicle();
        
        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }
    
    @Test
    public void processExitingVehicleTestIfReturningCustomer(){
        /*given standard conditions
        except that there is a previous ticket for this vehicle  */
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        //when an exiting vehicle is processed
        parkingService.processExitingVehicle();
        
        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }
    
    @Test
    public void processExitingVehicleTestUnableUpdate(){
        /*given standard conditions
        except that the ticket cannot be updated  */
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        //when an exiting vehicle is processed
        parkingService.processExitingVehicle();
        
        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }

    @Test
    public void processExitingVehicleTestUnableProcess(){
        /*given standard conditions
        except that the ticket has an inTime in the future  */
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() + (hourInMillis)));
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        //when an exiting vehicle is processed
        parkingService.processExitingVehicle();
        
        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){
        //given standard conditions
        //when the next available parking number is asked
        try{
            parkingService.getNextParkingNumberIfAvailable();
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to run parkingService.getNextParkingNumberIfAvailable");
        }
        
        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        /*given standard conditions
        except that the next available can't be found */
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        //when the next available parking number is asked
        assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());

        //then the following methods are called
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, Mockito.times(1)).readSelection();
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        /*given standard conditions
        except that input from the user does not correspond to a vehicle type */
        lenient().when(inputReaderUtil.readSelection()).thenReturn(0);

        //when the next available parking number is asked
        //then an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
    }

}
