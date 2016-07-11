package com.example.marcin.solarboatapp;

/**
 * Boat Monitor Class created by Marcin on 2016-07-10.
 */
public class UnknownMeasurement extends Measurement {
    public UnknownMeasurement(int newNumber) {
        super(newNumber);
        unit = "-";
    }

    @Override
    public void calculateValue() {

    }
}