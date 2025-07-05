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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceTest {
    private static final int EXPECTED_ENTRIES_SIZE = 1;
    private static final int EXPECTED_APPROVALS_SIZE = 2;

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
            mockTimesheetRepositoryToReturn(List.of(timesheet));

            List<TimesheetDto> result = timesheetService.getEmployeeTimesheets(employeeId);

            assertTimesheetListResult(result);
            verifyTimesheetRepositoryWasCalled();
        }

        @Test
        @DisplayName("Should throw NotFoundException when employee has no timesheets")
        void shouldThrowNotFoundExceptionWhenEmployeeHasNoTimesheets() {
            mockTimesheetRepositoryToReturn(List.of());

            assertThrows(TimesheetNotFoundException.class,
                    () -> timesheetService.getEmployeeTimesheets(employeeId));
        }
    }

    @Nested
    @DisplayName("Create Timesheet Tests")
    class CreateTimesheetTests {
        @ParameterizedTest
        @EnumSource(value = TimesheetStatus.class, names = {"DRAFTED", "SUBMITTED"})
        @DisplayName("Should create timesheet when request is valid")
        void shouldCreateTimesheetWhenRequestIsValid(TimesheetStatus status) {
            mockDependenciesForSuccessfulCreation(status);
            doNothing().when(timesheetValidator)
                    .validateTimesheetCreation(timesheetRequest, status);

            TimesheetDto result = timesheetService.createTimesheet(employeeId, status, timesheetRequest);

            assertTimesheetResult(result);
            verifyTimesheetWasSaved();
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void shouldThrowValidationException() {
            String errorMessage = "Validation error";
            doThrow(new TimesheetValidationException(errorMessage))
                    .when(timesheetValidator)
                    .validateTimesheetCreation(any(), any());

            TimesheetValidationException exception = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.createTimesheet(employeeId, TimesheetStatus.DRAFTED, timesheetRequest));

            assertEquals(errorMessage, exception.getMessage());
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
            mockDependenciesForSuccessfulApproval();

            TimesheetDto result = timesheetService.approveTimesheet(timesheetId, employeeApproverId, approvalRequest);

            assertTimesheetResult(result);
            verifyTimesheetWasApproved();
        }

        @Test
        @DisplayName("Should throw NotFoundException when timesheet doesn't exist")
        void shouldThrowNotFoundExceptionWhenTimesheetDoesntExist() {
            when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.empty());

            TimesheetNotFoundException exception = assertThrows(TimesheetNotFoundException.class,
                    () -> timesheetService.approveTimesheet(timesheetId, employeeApproverId, approvalRequest));

            assertEquals("Timesheet not found with id: " + timesheetId, exception.getMessage());
            verify(timesheetRepository).findById(timesheetId);
        }
    }

    private void assertTimesheetResult(TimesheetDto result) {
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(EXPECTED_ENTRIES_SIZE, result.entries().size());
        assertEquals(EXPECTED_APPROVALS_SIZE, result.approvals().size());
    }

    private void initializeTestData() {
        employeeId = UUID.randomUUID();
        employeeApproverId = UUID.randomUUID();
        employee = createTestEmployee(UUID.randomUUID());
        timesheet = createTestTimesheet(employee, createTimesheetApproval(1L, employee));
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
    }}