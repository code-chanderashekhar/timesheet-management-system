package com.synechisveltiosi.tms.api.exception.timesheet;

import com.synechisveltiosi.tms.api.exception.ResourceUpdateException;

public class TimesheetUpdateException extends ResourceUpdateException {
    public TimesheetUpdateException(String message) {
        super(message);
    }
}
