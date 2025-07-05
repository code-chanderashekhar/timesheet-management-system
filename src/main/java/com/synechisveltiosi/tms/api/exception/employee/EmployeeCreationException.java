package com.synechisveltiosi.tms.api.exception.employee;

import com.synechisveltiosi.tms.api.exception.ResourceCreationException;

public class EmployeeCreationException extends ResourceCreationException {
    public EmployeeCreationException(String message) {
        super(message);
    }
}
