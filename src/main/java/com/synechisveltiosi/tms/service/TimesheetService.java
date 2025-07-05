package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetCreationException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetUpdateException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetValidationException;
import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.model.entity.*;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.repository.TimesheetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetService {
    private final TimesheetRepository timesheetRepository;
    private final EmployeeService employeeService;
    private final TimesheetMapper timesheetMapper;
    private final TimesheetValidator timesheetValidator;

    @Transactional
    public List<TimesheetDto> getEmployeeTimesheets(UUID employeeId) {
        log.info("Getting timesheets for employee with id: {}", employeeId);
        List<Timesheet> timesheets = getTimesheetsByEmployeeIdOrElseThrow(employeeId);
        return timesheets.stream().map(TimesheetDto::new).toList();
    }

    @Transactional
    public TimesheetDto createTimesheet(UUID employeeId, TimesheetStatus status, TimesheetRequest timesheetRequest) {
        log.info("Creating timesheet for employee with id: {}", employeeId);
        timesheetValidator.validateTimesheetCreation(timesheetRequest, status);

        Employee employee = employeeService.getEmployeeById(employeeId);
        Timesheet timesheet = timesheetMapper.toEntity(employee, status, timesheetRequest);
        timesheet.addApproval(createInitialApproval(employee.getManager(), timesheet.getStatus(), ""));

        return Optional.of(timesheet)
                .map(timesheetRepository::save)
                .map(TimesheetDto::new)
                .orElseThrow(() -> new TimesheetCreationException("Failed to create timesheet"));
    }

    @Transactional
    public TimesheetDto approveTimesheet(UUID timesheetId, UUID empApproverId, TimesheetApprovalRequest approvalRequest) {
        log.info("Approving timesheet with timesheetId {} and employeeApproverId {}", timesheetId, empApproverId);
        Employee empApprover = employeeService.getEmployeeById(empApproverId);
        Timesheet timesheet = getTimesheetByID(timesheetId);

        timesheet.setStatus(approvalRequest.status());
        timesheet.addApproval(createInitialApproval(empApprover, approvalRequest.status(), approvalRequest.comments()));

        return Optional.of(timesheet)
                .map(timesheetRepository::save)
                .map(TimesheetDto::new)
                .orElseThrow(() -> new TimesheetUpdateException("Failed to approve timesheet"));
    }

    private TimesheetApproval createInitialApproval(Employee approver, TimesheetStatus status, String comments) {
        return TimesheetApproval.builder()
                .comments(comments)
                .status(status)
                .approver(approver)
                .build();
    }

    private Timesheet getTimesheetByID(UUID timesheetId) {
        return timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new TimesheetNotFoundException("Timesheet not found with id: " + timesheetId));
    }

    private List<Timesheet> getTimesheetsByEmployeeIdOrElseThrow(UUID employeeId) {
        return timesheetRepository.findByEmployeeId(employeeId)
                .filter(timesheetList -> !timesheetList.isEmpty())
                .orElseThrow(() -> new TimesheetNotFoundException("Timesheet not found for employee with id: " + employeeId));
    }
}


@Component
@RequiredArgsConstructor
class TimesheetMapper {
    private final TaskService taskService;

    public Timesheet toEntity(Employee employee, TimesheetStatus status, TimesheetRequest request) {
        Timesheet timesheet = createTimesheetBase(employee, status, request);
        addEntriesToTimesheet(timesheet, request.entries());
        return timesheet;
    }

    public Timesheet createTimesheetBase(Employee employee, TimesheetStatus status, TimesheetRequest request) {
        return Timesheet.builder()
                .startDate(request.startDate())
                .endDate(request.endDate())
                .employee(employee)
                .status(status)
                .build();
    }

    public void addEntriesToTimesheet(Timesheet timesheet, List<TimesheetRequest.TimesheetEntryRequest> entries) {
        entries.stream()
                .map(timesheetEntryRequest -> createTimesheetEntry(timesheetEntryRequest, taskService.getTaskById(timesheetEntryRequest.taskId())))
                .forEach(timesheet::addEntry);
    }

    public TimesheetEntry createTimesheetEntry(TimesheetRequest.TimesheetEntryRequest request, Task rTask) {
        return TimesheetEntry.builder()
                .date(request.date())
                .hours(request.hours())
                .task(rTask)
                .entryType(request.entryType())
                .build();
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class TimesheetValidator {
    private static final String INVALID_STATUS_MESSAGE = "Cannot create timesheet in status: %s";
    private static final String INVALID_DATES_MESSAGE = "Start date cannot be after end date";
    private static final String NO_ENTRIES_MESSAGE = "Timesheet must contain at least one entry";

    public void validateTimesheetCreation(TimesheetRequest request, TimesheetStatus status) {
        log.info("Validating timesheet creation request");
        validateStatus(status);
        validateDateRange(request.startDate(), request.endDate());
        validateEntries(request.entries());
    }

    private void validateStatus(TimesheetStatus status) {
        if (!isValidInitialStatus(status)) {
            throw new TimesheetValidationException(String.format(INVALID_STATUS_MESSAGE, status));
        }
    }

    private boolean isValidInitialStatus(TimesheetStatus status) {
        return status == TimesheetStatus.SUBMITTED || status == TimesheetStatus.DRAFTED;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new TimesheetValidationException(INVALID_DATES_MESSAGE);
        }
    }

    private void validateEntries(List<TimesheetRequest.TimesheetEntryRequest> entries) {
        if (entries.isEmpty()) {
            throw new TimesheetValidationException(NO_ENTRIES_MESSAGE);
        }
    }
}
