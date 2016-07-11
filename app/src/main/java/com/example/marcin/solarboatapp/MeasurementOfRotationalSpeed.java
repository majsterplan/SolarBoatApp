package com.example.marcin.solarboatapp;

/**
 * MeasurementOfRotationalSpeed Class created by Marcin on 2016-07-09.
 */
public class MeasurementOfRotationalSpeed extends Measurement {
    public MeasurementOfRotationalSpeed(int newNumber) {
        super(newNumber);
        unit = "rpm";
    }

    @Override
    public void calculateValue() {

    }
}
