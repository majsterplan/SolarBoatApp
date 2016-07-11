package com.example.marcin.solarboatapp;

/**
 * Service created by Marcin on 2016-04-03.
 */

public abstract class Service {
    public enum StartUpStatus {
        NO_SUPPORT, NEED_USER_INTERACTION, ON
    }

    protected boolean status;

    public abstract StartUpStatus start();

    public void setStatus(boolean newStatus) {
        status = newStatus;
    }

    public boolean getStatus() {
        return status;
    }
}
