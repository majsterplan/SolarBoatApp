package com.example.marcin.solarboatapp;

/**
 * MeasurementOfTemperature Class created by Marcin on 2016-04-08.
 */
public class MeasurementOfTemperature extends Measurement {
    public MeasurementOfTemperature(int newNumber) {
        super(newNumber);
        unit = "\u2103";
    }

    @Override
    public void calculateValue() {

    }
}
