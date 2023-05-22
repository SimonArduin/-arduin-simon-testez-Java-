package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public double freeDuration = 0.5;
    public double discountRate = 0.95;

    private long hourInMillis = 60 * 60 * 1000;

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getInTime() == null)){
            throw new IllegalArgumentException("In time provided is incorrect: NULL");
        }
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:" + ((ticket.getOutTime() == null) ? "NULL" : ticket.getOutTime().toString()));
        }

        //calculate parking duration
        double inTime = ticket.getInTime().getTime();
        double outTime = ticket.getOutTime().getTime();
        double duration = (outTime - inTime) / (hourInMillis);

        //calculate ticket price
        if(duration <= freeDuration){
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
        if(discount){
            ticket.setPrice(discountRate * ticket.getPrice());
        }

    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}