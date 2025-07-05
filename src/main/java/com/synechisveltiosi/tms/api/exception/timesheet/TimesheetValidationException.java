package com.synechisveltiosi.tms.api.exception.timesheet;

import com.synechisveltiosi.tms.api.exception.ResourceValidationException;

public class TimesheetValidationException extends ResourceValidationException {
    public TimesheetValidationException(String message) {
        super(message);
    }
}
