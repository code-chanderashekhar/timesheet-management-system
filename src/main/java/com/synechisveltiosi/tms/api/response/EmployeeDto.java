package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.Employee;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Employee}
 */
public record EmployeeDto(UUID id, PersonDetailsDto personDetails) implements Serializable {
  public EmployeeDto(Employee employee) {
    this(employee.getId(), new PersonDetailsDto(employee.getPersonDetails()));
  }
}