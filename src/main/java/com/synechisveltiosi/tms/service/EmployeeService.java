package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.employee.EmployeeNotFoundException;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Employee getEmployeeById(UUID uuid) {
        return employeeRepository.findById(uuid)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + uuid));
    }

    public List<Employee> getAllEmployee() {
        return employeeRepository.findAll();
    }
}
