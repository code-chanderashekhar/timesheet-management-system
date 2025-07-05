package com.synechisveltiosi.tms.api.exception.timesheet;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;

public class TimesheetNotFoundException extends ResourceNotFoundException {

    public TimesheetNotFoundException(String message) {
        super(message);
    }
}
