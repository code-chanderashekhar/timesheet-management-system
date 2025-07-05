package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.Timesheet;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Timesheet}
 */
public record TimesheetDto(UUID id, TimesheetStatus status,
                           LocalDate startDate,
                           LocalDate endDate,
                           Collection<TimesheetEntryDto> entries, EmployeeDto employee,
                           Collection<TimesheetApprovalDto> approvals) implements Serializable {
    public TimesheetDto(Timesheet t) {
        this(t.getId(),
                t.getStatus(),
                t.getStartDate(),
                t.getEndDate(),
                t.getEntries().stream().map(TimesheetEntryDto::new).toList(),
                new EmployeeDto(t.getEmployee()), t.getApprovals().stream().map(TimesheetApprovalDto::new).toList());
    }
}