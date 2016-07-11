package com.example.marcin.solarboatapp;

/**
 * Measurement Class created by Marcin on 2016-04-08.
 */
public abstract class Measurement {
    protected int number;
    protected String unit;
    protected double value;

    public Measurement(int newNumber) {
        number = newNumber;
    }

    public int getNumber() {
        return number;
    }

    public String getUnit() {
        return unit;
    }

    public void setValue(double newValue) {
        value = newValue;
    }

    public abstract void calculateValue();
}
