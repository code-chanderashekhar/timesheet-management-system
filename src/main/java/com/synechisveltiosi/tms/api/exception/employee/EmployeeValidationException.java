package com.synechisveltiosi.tms.api.exception.employee;

import com.synechisveltiosi.tms.api.exception.ResourceValidationException;

public class EmployeeValidationException extends ResourceValidationException {
    public EmployeeValidationException(String message) {
        super(message);
    }
}
