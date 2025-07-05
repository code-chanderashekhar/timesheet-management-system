package com.synechisveltiosi.tms.api.exception.employee;

import com.synechisveltiosi.tms.api.exception.ResourceNotFoundException;

public class EmployeeNotFoundException extends ResourceNotFoundException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
