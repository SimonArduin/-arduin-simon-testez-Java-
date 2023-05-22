package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private long hourInMillis = 60 * 60 * 1000;
    private long minuteInMillis = 60 * 1000;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Mock
    private static Ticket mockTicket;

    @Test
    public void calculateFareCar(){
        //given a ticket for 1 hour for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(new Date(currentTime.getTime() - hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);
        
        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);
        
        //then the price is equal to the car rate per hour
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        //given a ticket for 1 hour for a bike
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(new Date(currentTime.getTime() - hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);
        
        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);
        
        //then the price is equal to the bike rate per hour
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        //given a ticket for 1 hour without a vehicle type
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.UNKNOWN,false);
        ticket.setInTime(new Date(currentTime.getTime() - hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        //then an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareCarWithNoInTime(){
        //given a ticket without outTime for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        //then an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareCarWithNoOutTime(){
        //given a ticket without outTime for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);
        
        //when the price for this ticket is calculated
        //then an IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){

        //given a ticket for a bike with an inTime after the outTime
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(new Date(currentTime.getTime() + hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        //then a IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        //given a ticket for 45 minutes for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(new Date(currentTime.getTime() - 45 * minuteInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);

        //then the price is equal to 3/4 of the car rate per hour
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        //given a ticket for 45 minutes for a bike
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(new Date(currentTime.getTime() - 45 * minuteInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);
        
        //then the price is equal to 3/4 of the bike rate per hour
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        //given a ticket for 24 hours for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(new Date(currentTime.getTime() - 24 * hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);
        
        //then the price is equal to 24 times the car rate per hour
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParking(){
        //given a ticket for 30 minutes for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(new Date(currentTime.getTime() - (long) (fareCalculatorService.freeDuration * 60) * minuteInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);

        //then the price is free
        assertEquals( (0) , ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParking(){
        //given a ticket for 30 minutes for a bike
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(new Date(currentTime.getTime() - (long) (fareCalculatorService.freeDuration * 60) * minuteInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated
        fareCalculatorService.calculateFare(ticket);

        //then the price is free
        assertEquals( (0) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithDiscount(){
        //given a ticket for 1 hour for a car
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(new Date(currentTime.getTime() - hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated with a discount
        fareCalculatorService.calculateFare(ticket, true);
        
        //then the price is equal to the car rate per hour multiplied by the discount rate
        assertEquals(fareCalculatorService.discountRate * Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscount(){
        //given a ticket for 1 hour for a bike
        Date currentTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(new Date(currentTime.getTime() - hourInMillis));
        ticket.setOutTime(currentTime);
        ticket.setParkingSpot(parkingSpot);

        //when the price for this ticket is calculated with a discount
        fareCalculatorService.calculateFare(ticket, true);
        
        //then the price is equal to the bike rate per hour multiplied by the discount rate
        assertEquals(fareCalculatorService.discountRate * Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

}
