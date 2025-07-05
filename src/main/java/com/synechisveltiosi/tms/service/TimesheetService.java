package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetCreationException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetUpdateException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetValidationException;
import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Timesheet;
import com.synechisveltiosi.tms.model.entity.TimesheetApproval;
import com.synechisveltiosi.tms.model.entity.TimesheetEntry;
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

    private static final String INVALID_STATUS_MESSAGE = "Cannot create timesheet in status: %s";
    private static final String INVALID_DATES_MESSAGE = "Start date cannot be after end date";
    private static final String NO_ENTRIES_MESSAGE = "Timesheet must contain at least one entry";

    /**
     * Retrieves the list of timesheets associated with a specific employee.
     *
     * @param employeeId the unique identifier of the employee whose timesheets are being retrieved
     * @return a list of {@link TimesheetDto} objects representing the timesheets of the specified employee
     * @throws TimesheetNotFoundException if no timesheets are found for the given employee ID
     */
    public List<TimesheetDto> getEmployeeTimesheets(UUID employeeId) {
        log.info("Getting timesheets for employee with id: {}", employeeId);
        List<Timesheet> timesheets = getTimesheetsByEmployeeIdOrElseThrow(employeeId);
        return timesheets.stream().map(TimesheetDto::new).toList();
    }

    /**
     * Creates a new timesheet for a specified employee with the given status and request details.
     *
     * @param employeeId       the unique identifier of the employee for whom the timesheet is being created
     * @param status           the status to assign to the timesheet, such as DRAFTED or SUBMITTED
     * @param timesheetRequest the request object containing details like start date, end date, and entries for the timesheet
     * @return a {@link TimesheetDto} representing the newly created timesheet
     * @throws TimesheetCreationException if the timesheet creation fails for any reason
     */
    @Transactional
    public TimesheetDto createTimesheet(UUID employeeId, TimesheetStatus status, TimesheetRequest timesheetRequest) {
        log.info("Creating timesheet for employee with id: {}", employeeId);
        validateTimesheetCreation(timesheetRequest, status);

        Employee employee = employeeService.getEmployeeById(employeeId);
        Timesheet timesheet = timesheetMapper.toEntity(employee, status, timesheetRequest);
        timesheet.addApproval(createInitialApproval(employee.getManager(), timesheet.getStatus(),""));
        return Optional.of(timesheet)
                .map(timesheetRepository::save)
                .map(TimesheetDto::new)
                .orElseThrow(() -> new TimesheetCreationException("Failed to create timesheet"));
    }


    private void validateEmployeeManager(Employee employee) {
        if (employee.getManager() == null) {
            throw new TimesheetValidationException("Cannot create timesheet for employee without a manager");
        }
    }

    private TimesheetApproval createInitialApproval(Employee manager, TimesheetStatus status, String comments) {
        return TimesheetApproval.builder()
                .comments(comments)
                .status(status)
                .approver(manager)
                .build();
    }


    /**
     * Validates timesheet request data before creation.
     * Checks status validity, date ranges, and entries presence.
     *
     * @param request Timesheet request to validate
     * @param status  Target timesheet status
     * @throws TimesheetValidationException if validation fails
     */
    private void validateTimesheetCreation(TimesheetRequest request, TimesheetStatus status) {

        log.info("Validating timesheet creation request");

        validateStatus(status);
        validateDateRange(request.startDate(), request.endDate());
        validateEntries(request.entries());
    }

    /**
     * Approves the specified timesheet by updating its status and adding an approval record.
     *
     * @param timesheetId     the unique identifier of the timesheet to be approved
     * @param empApproverId   the unique identifier of the employee approving the timesheet
     * @param approvalRequest the request object containing approval details such as comments and status
     * @return a {@link TimesheetDto} representing the approved timesheet
     * @throws TimesheetNotFoundException if the timesheet with the given ID is not found
     * @throws TimesheetCreationException if there is a failure during the approval process
     */
    public TimesheetDto approveTimesheet(UUID timesheetId, UUID empApproverId, TimesheetApprovalRequest approvalRequest) {
        log.info("approving timesheet with timesheetId {} and employeeApproverId {}", timesheetId, empApproverId);
        Employee empApprover = employeeService.getEmployeeById(empApproverId);
        Timesheet timesheet = getTimesheetByID(timesheetId);
        timesheet.setStatus(approvalRequest.status());
        timesheet.addApproval(createInitialApproval(empApprover, approvalRequest.status(), approvalRequest.comments()));
        return Optional.of(timesheet)
                .map(timesheetRepository::save)
                .map(TimesheetDto::new)
                .orElseThrow(() -> new TimesheetUpdateException("Failed to approve timesheet"));
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

    private void validateStatus(TimesheetStatus status) {
        if (isTimesheetNotSubmittedAndDrafted(status)) {
            throw new TimesheetValidationException(String.format(INVALID_STATUS_MESSAGE, status));
        }
    }

    public boolean isTimesheetNotSubmittedAndDrafted(TimesheetStatus status) {
        return !status.equals(TimesheetStatus.SUBMITTED) && !status.equals(TimesheetStatus.DRAFTED);
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


    @Component
    @RequiredArgsConstructor
    public static class TimesheetMapper {
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
                    .map(this::createTimesheetEntry)
                    .forEach(timesheet::addEntry);
        }

        public TimesheetEntry createTimesheetEntry(TimesheetRequest.TimesheetEntryRequest request) {
            return TimesheetEntry.builder()
                    .date(request.date())
                    .hours(request.hours())
                    .task(taskService.getTaskById(request.taskId()))
                    .build();
        }
    }
}
