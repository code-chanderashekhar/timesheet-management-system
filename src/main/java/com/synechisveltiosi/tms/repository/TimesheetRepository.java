package com.synechisveltiosi.tms.repository;

import com.synechisveltiosi.tms.model.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {
    @Query("SELECT t FROM Timesheet t WHERE t.employee.id = :employeeId")
    Optional<List<Timesheet>> findByEmployeeId(UUID employeeId);

}