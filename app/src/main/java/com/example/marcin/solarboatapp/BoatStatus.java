package com.example.marcin.solarboatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * BoatStatus Class created by Marcin on 2016-04-01.
 */
public class BoatStatus {
    private enum MeasurementCategory {
        UNKNOWN(-1, "UnknownMeasurement"),
        SPEED(1, "MeasurementOfSpeed"),
        ROTATIONAL_SPEED(2, "MeasurementOfRotationalSpeed"),
        CURRENT(3, "MeasurementOfCurrent"),
        VOLTAGE(4, "MeasurementOfVoltage"),
        TEMPERATURE(5, "MeasurementOfTemperature");

        private int value;
        private String className;
        private static Map measurementCategories = new HashMap();
        private static Map classNames = new HashMap();

        MeasurementCategory(int newValue, String newClassName) {
            value = newValue;
            className = newClassName;
        }

        static {
            for (MeasurementCategory category :
                    MeasurementCategory.values()) {
                measurementCategories.put(category.value, category);
            }
        }

        static {
            for (MeasurementCategory category :
                    MeasurementCategory.values()) {
                classNames.put(category.value, category.className);
            }
        }

        public static MeasurementCategory getCateogryAt(int value) {
            MeasurementCategory category = (MeasurementCategory) measurementCategories.get(value);
            if (null == category) {
                category = (MeasurementCategory) measurementCategories.get(-1);
            }
            return category;
        }

        public static String getClassNameAt(int value) {
            String className = (String) classNames.get(value);
            if (null == className) {
                className = (String) classNames.get(-1);
            }
            return className;
        }

        /*public int getValue() {
            return value;
        }*/
    }

    private BroadcastReceiver frameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String frame = intent.getStringExtra("frame");
            FrameParser frameParser = new FrameParser();
            Vector<String> result = new Vector<String>();
            boolean parsingSuccess = frameParser.parse(frame, result);
            Intent nextIntent = new Intent();
            if (parsingSuccess) {
                int measurementCategory = Integer.parseInt(result.elementAt(0));
                int measurementNumber = Integer.parseInt(result.elementAt(1));
                double measurementValue = Double.parseDouble(result.elementAt(2));
                if (!isMeasurementExist(measurementCategory, measurementNumber)) {
                    addMeasurement(measurementCategory, measurementNumber);
                }
                Measurement measurement = getMeasurement(measurementCategory, measurementNumber);
                measurement.setValue(measurementValue);
                nextIntent.setAction("SHOW_MEASUREMENT");
                nextIntent.putExtra(result.elementAt(0) + "." + result.elementAt(1), String.format(Locale.US, "%.1f", measurementValue) + " " + measurement.getUnit());
                appContext.sendBroadcast(nextIntent);
            } else {
                nextIntent.setAction("SHOW_LOG");
                nextIntent.putExtra("invalidFrame", frame);
            }
            appContext.sendBroadcast(nextIntent);
        }
    };

    private Context appContext;
    private Set<Measurement> measurements = new HashSet<Measurement>();

    public BoatStatus(Context context) {
        appContext = context;
        appContext.registerReceiver(frameReceiver, new IntentFilter("INCOMING_FRAME"));
    }

    public boolean isMeasurementExist(int category, int number) {
        boolean existingMeasurement = false;
        for (Measurement measurement :
                measurements) {
            if (measurement.getClass().getSimpleName().equals(MeasurementCategory.getClassNameAt(category)) && measurement.getNumber() == number) {
                existingMeasurement = true;
                break;
            }
        }
        return existingMeasurement;
    }

    public void addMeasurement(int value, int number) {
        Measurement measurement = null;
        MeasurementCategory category = MeasurementCategory.getCateogryAt(value);
        switch (category) {
            case SPEED:
                measurement = new MeasurementOfSpeed(number);
                break;
            case ROTATIONAL_SPEED:
                measurement = new MeasurementOfRotationalSpeed(number);
                break;
            case CURRENT:
                measurement = new MeasurementOfCurrent(number);
                break;
            case VOLTAGE:
                measurement = new MeasurementOfVoltage(number);
                break;
            case TEMPERATURE:
                measurement = new MeasurementOfTemperature(number);
                break;
            case UNKNOWN:
                measurement = new UnknownMeasurement(number);
                break;
            default:
        }
        if (measurement != null) {
            measurements.add(measurement);
        }
    }

    public Measurement getMeasurement(int category, int number) {
        Measurement interestingMeasurement = null;
        for (Measurement measurement :
                measurements) {
            if (measurement.getClass().getSimpleName().equals(MeasurementCategory.getClassNameAt(category)) && measurement.getNumber() == number) {
                interestingMeasurement = measurement;
                break;
            }
        }
        return interestingMeasurement;
    }
}
