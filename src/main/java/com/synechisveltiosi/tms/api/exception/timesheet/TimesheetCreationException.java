package com.synechisveltiosi.tms.api.exception.timesheet;

import com.synechisveltiosi.tms.api.exception.ResourceCreationException;

public class TimesheetCreationException extends ResourceCreationException {
    public TimesheetCreationException(String message) {
        super(message);
    }
}
