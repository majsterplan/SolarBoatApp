package com.example.marcin.solarboatapp;

/**
 * MeasurementOfVoltage Class created by Marcin on 2016-04-08.
 */
public class MeasurementOfVoltage extends Measurement {
    public MeasurementOfVoltage(int newNumber) {
        super(newNumber);
        unit = "V";
    }

    @Override
    public void calculateValue() {

    }
}
