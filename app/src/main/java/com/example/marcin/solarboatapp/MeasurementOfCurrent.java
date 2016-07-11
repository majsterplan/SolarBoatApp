package com.example.marcin.solarboatapp;

/**
 * MeasurementOfCurrent Class created by Marcin on 2016-04-08.
 */
public class MeasurementOfCurrent extends Measurement {
    public MeasurementOfCurrent(int newNumber) {
        super(newNumber);
        unit = "A";
    }

    @Override
    public void calculateValue() {

    }
}
