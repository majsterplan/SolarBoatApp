package com.example.marcin.solarboatapp;

import java.util.Vector;

/**
 * Boat Monitor Class created by Marcin on 2016-04-19.
 */
public class FrameParser {
    public boolean parse(String frame, Vector<String> result) {
        int begin = frame.indexOf(Frame.FRAME_OPEN);
        if (!(begin != -1 && frame.endsWith(Frame.FRAME_CLOSE)))
            return false;
        String interestingData = frame.substring(begin + 1, frame.length() - 1);
        String[] parts = interestingData.split(Frame.MAIN_SEPARATOR);
        if (parts.length != 2)
            return false;
        String[] unitData = parts[0].split(Frame.UNIT_NAME_SEPARATOR);
        if (unitData.length != 2)
            return false;
        try {
            int measurementCategory = Integer.parseInt(unitData[0]);
            int measurementNumber = Integer.parseInt(unitData[1]);
            double measurementValue = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        result.add(unitData[0]);
        result.add(unitData[1]);
        result.add(parts[1]);
        return true;
    }
}
