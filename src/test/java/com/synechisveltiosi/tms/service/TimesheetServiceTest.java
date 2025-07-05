package com.synechisveltiosi.tms.service;

import static com.synechisveltiosi.tms.util.DataUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetValidationException;
import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Timesheet;
import com.synechisveltiosi.tms.model.entity.TimesheetApproval;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.repository.TimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {
    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TimesheetMapper timesheetMapper;
    @Mock
    private TimesheetValidator timesheetValidator;
    @InjectMocks
    private TimesheetService timesheetService;

    private UUID employeeId;
    private UUID employeeApproverId;
    private Employee employee;
    private Timesheet timesheet;
    private TimesheetApproval approval;
    private TimesheetRequest timesheetRequest;

    @BeforeEach
    void setUp() {
        initializeTestData();
    }

    @Nested
    @DisplayName("Get Employee Timesheets Tests")
    class GetEmployeeTimesheetsTests {
        @Test
        @DisplayName("Should return timesheet list when employee has timesheets")
        void shouldReturnTimesheetListWhenEmployeeHasTimesheets() {
            // given
            mockTimesheetRepositoryToReturn(List.of(timesheet));

            // when
            List<TimesheetDto> result = timesheetService.getEmployeeTimesheets(employeeId);

            // then
            assertTimesheetListResult(result);
            verifyTimesheetRepositoryWasCalled();
        }

        @Test
        @DisplayName("Should throw NotFoundException when employee has no timesheets")
        void shouldThrowNotFoundExceptionWhenEmployeeHasNoTimesheets() {
            // given
            mockTimesheetRepositoryToReturn(List.of());

            // when/then
            assertThrows(TimesheetNotFoundException.class,
                    () -> timesheetService.getEmployeeTimesheets(employeeId));
        }
    }

    @Nested
    @DisplayName("Create Timesheet Tests")
    class CreateTimesheetTests {
        @Test
        @DisplayName("Should create timesheet when request is valid and status drafted")
        void shouldCreateTimesheetWhenRequestIsValidAndStatusDrafted() {
            // given
            mockDependenciesForSuccessfulCreation(TimesheetStatus.DRAFTED);
            doNothing()
                    .when(timesheetValidator).validateTimesheetCreation(timesheetRequest, TimesheetStatus.DRAFTED);

            // when
            TimesheetDto result = timesheetService.createTimesheet(employeeId, TimesheetStatus.DRAFTED, timesheetRequest);
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals(1, result.entries().size());
            assertEquals(2, result.approvals().size());
            assertEquals(result.status(), TimesheetStatus.DRAFTED);

            // then
            assertNotNull(result);
            verifyTimesheetWasSaved();
        }

        @Test
        @DisplayName("Should create timesheet when request is valid and status submitted")
        void shouldCreateTimesheetWhenRequestIsValidAndStatusSubmitted() {
            // given
            mockDependenciesForSuccessfulCreation(TimesheetStatus.SUBMITTED);
            doNothing()
                    .when(timesheetValidator).validateTimesheetCreation(timesheetRequest, TimesheetStatus.SUBMITTED);
            // when
            TimesheetDto result = timesheetService.createTimesheet(employeeId, TimesheetStatus.SUBMITTED, timesheetRequest);
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals(1, result.entries().size());
            assertEquals(2, result.approvals().size());

            // then
            assertNotNull(result);
            verifyTimesheetWasSaved();
        }

        @Test
        @DisplayName("Should throw ValidationException when date range is invalid")
        void shouldThrowValidationExceptionWhenDateRangeIsInvalid() {
            // given
            doThrow(new TimesheetValidationException("Start date cannot be after end date"))
                    .when(timesheetValidator).validateTimesheetCreation(any(TimesheetRequest.class), any(TimesheetStatus.class));

            // when/then
            TimesheetValidationException timesheetValidationException = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.createTimesheet(employeeId, TimesheetStatus.DRAFTED, createInvalidStartDateTimesheetRequest()));
            assertEquals("Start date cannot be after end date", timesheetValidationException.getMessage());
        }

        @Test
        @DisplayName("Should throw ValidationException when entry is invalid")
        void shouldThrowValidationExceptionWhenEntriesInvalid() {
            // given
            doThrow(new TimesheetValidationException("Timesheet must contain at least one entry"))
                    .when(timesheetValidator).validateTimesheetCreation(any(TimesheetRequest.class), any(TimesheetStatus.class));
            TimesheetRequest invalidRequest = createInvalidEntryTimesheetRequest();

            // when/then
            TimesheetValidationException timesheetValidationException = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.createTimesheet(employeeId, TimesheetStatus.DRAFTED, invalidRequest));
            assertEquals("Timesheet must contain at least one entry", timesheetValidationException.getMessage());
        }

        @Test
        @DisplayName("Should throw ValidationException invalid status")
        void shouldThrowValidationExceptionWhenStatusInvalid() {
            // given
            TimesheetRequest invalidRequest = createTestTimesheetRequest();
            doThrow(new TimesheetValidationException("Cannot create timesheet in status: APPROVED"))
                    .when(timesheetValidator).validateTimesheetCreation(timesheetRequest, TimesheetStatus.APPROVED);
            // when/then
            TimesheetValidationException timesheetValidationException = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.createTimesheet(employeeId, TimesheetStatus.APPROVED, invalidRequest));
            assertEquals("Cannot create timesheet in status: APPROVED", timesheetValidationException.getMessage());
        }
    }

    @Nested
    @DisplayName("Approve Timesheet Tests")
    class ApproveTimesheetTests {
        private UUID timesheetId;
        private TimesheetApprovalRequest approvalRequest;

        @BeforeEach
        void setUp() {
            timesheetId = UUID.randomUUID();
            approvalRequest = new TimesheetApprovalRequest("Approved", TimesheetStatus.APPROVED);
        }

        @Test
        @DisplayName("Should approve timesheet when it exists")
        void shouldApproveTimesheetWhenItExists() {
            // given
            mockDependenciesForSuccessfulApproval();

            // when
            TimesheetDto result = timesheetService.approveTimesheet(timesheetId, employeeApproverId, approvalRequest);
            assertNotNull(result.id());
            assertEquals(1, result.entries().size());
            assertEquals(2, result.approvals().size());
            // then
            assertNotNull(result);
            System.out.println(result);
            verifyTimesheetWasApproved();
        }

        @Test
        @DisplayName("Should throw NotFoundException when timesheet doesn't exist")
        void shouldThrowNotFoundExceptionWhenTimesheetDoesntExist() {
            // given
            when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.empty());

            // when/then
            TimesheetNotFoundException timesheetNotFoundException = assertThrows(TimesheetNotFoundException.class,
                    () -> timesheetService.approveTimesheet(timesheetId, employeeApproverId, approvalRequest));
            assertEquals("Timesheet not found with id: " + timesheetId, timesheetNotFoundException.getMessage());
            verify(timesheetRepository, times(1)).findById(timesheetId);
        }
    }


    private void initializeTestData() {
        employeeId = UUID.randomUUID();
        employeeApproverId = UUID.randomUUID();
        employee = createTestEmployee(UUID.randomUUID());
        approval = createTimesheetApproval(1L, employee);
        timesheet = createTestTimesheet(employee, approval);
        timesheetRequest = createTestTimesheetRequest();
    }

    private void mockTimesheetRepositoryToReturn(List<Timesheet> timesheets) {
        when(timesheetRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(timesheets));
    }

    private void mockDependenciesForSuccessfulCreation(TimesheetStatus status) {
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);
        when(timesheetMapper.toEntity(employee, status, timesheetRequest)).thenReturn(timesheet);
        when(timesheetRepository.save(timesheet)).thenReturn(timesheet);
    }

    private void assertTimesheetListResult(List<TimesheetDto> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    private void verifyTimesheetRepositoryWasCalled() {
        verify(timesheetRepository, times(1)).findByEmployeeId(employeeId);
    }

    private void verifyTimesheetWasSaved() {
        verify(timesheetRepository, times(1)).save(timesheet);
    }

    private void mockDependenciesForSuccessfulApproval() {
        when(timesheetRepository.findById(any(UUID.class))).thenReturn(Optional.of(timesheet));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(timesheet);
    }

    private void verifyTimesheetWasApproved() {
        verify(timesheetRepository, times(1)).findById(any(UUID.class));
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }
}