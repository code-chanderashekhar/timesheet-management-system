package com.synechisveltiosi.tms.service;

import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetNotFoundException;
import com.synechisveltiosi.tms.api.exception.timesheet.TimesheetValidationException;
import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.entity.Timesheet;
import com.synechisveltiosi.tms.model.entity.TimesheetApproval;
import com.synechisveltiosi.tms.model.enums.TimesheetEntryType;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.repository.TimesheetRepository;
import com.synechisveltiosi.tms.util.DataUtils;
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

import java.time.LocalDate;
import java.util.*;

import static com.synechisveltiosi.tms.util.DataUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private final LocalDate startDate = LocalDate.of(2025, 1, 1);
    private final LocalDate endDate = LocalDate.of(2025, 1, 7);
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
            //given
            mockTimesheetRepositoryToReturn(List.of(timesheet));
            //when
            List<TimesheetDto> result = timesheetService.getAllTimesheetByEmployeeId(employeeId);
            //then
            assertTimesheetListResult(result);
            verifyTimesheetRepositoryWasCalled();
        }

        @Test
        @DisplayName("Should throw NotFoundException when employee has no timesheets")
        void shouldThrowNotFoundExceptionWhenEmployeeHasNoTimesheets() {
            //given
            mockTimesheetRepositoryToReturn(List.of());
            //when
            TimesheetNotFoundException timesheetNotFoundException = assertThrows(TimesheetNotFoundException.class,
                    () -> timesheetService.getAllTimesheetByEmployeeId(employeeId));
            //then
            assertEquals("Timesheet not found for employee with id: " + employeeId, timesheetNotFoundException.getMessage());
            verifyTimesheetRepositoryWasCalled();
        }
    }

    @Nested
    @DisplayName("Create Timesheet Tests")
    class CreateTimesheetTests {
        @ParameterizedTest
        @EnumSource(value = TimesheetStatus.class, names = {"DRAFTED", "SUBMITTED"})
        @DisplayName("Should create timesheet when request is valid")
        void shouldCreateTimesheetWhenRequestIsValid(TimesheetStatus status) {
            //given
            timesheet = createTestTimesheet(status, employee, createTimesheetApproval(1L, employee));
            mockDependenciesForSuccessfulCreation(status);
            doNothing().when(timesheetValidator)
                    .validateTimesheetCreation(timesheetRequest, status);
            //when
            TimesheetDto result = timesheetService.draftOrSubmitTimesheet(employeeId, status, timesheetRequest);
            //then
            assertTimesheetResult(status, result);
            verifyTimesheetWasSaved();
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void shouldThrowValidationException() {
            String errorMessage = "Validation error";
            //given
            doThrow(new TimesheetValidationException(errorMessage))
                    .when(timesheetValidator)
                    .validateTimesheetCreation(any(), any());
            //when
            TimesheetValidationException exception = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.draftOrSubmitTimesheet(employeeId, TimesheetStatus.DRAFTED, timesheetRequest));
            //then
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
            //given
            mockDependenciesForSuccessfulApproval();
            //when
            TimesheetDto result = timesheetService.approveTimesheet(timesheetId, employeeApproverId, approvalRequest);
            //then
            assertTimesheetResult(TimesheetStatus.APPROVED, result);
            verifyTimesheetWasApproved();
        }

        @Test
        @DisplayName("Should throw NotFoundException when timesheet doesn't exist")
        void shouldThrowNotFoundExceptionWhenTimesheetDoesntExist() {
            //given
            when(timesheetRepository.findById(timesheetId)).thenReturn(Optional.empty());
            //when
            TimesheetNotFoundException exception = assertThrows(TimesheetNotFoundException.class,
                    () -> timesheetService.approveTimesheet(timesheetId, employeeApproverId, approvalRequest));
            //then
            assertEquals("Timesheet not found with id: " + timesheetId, exception.getMessage());
            verify(timesheetRepository).findById(timesheetId);
        }
    }

    private void assertTimesheetResult(TimesheetStatus status, TimesheetDto result) {
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(EXPECTED_ENTRIES_SIZE, result.entries().size());
        assertEquals(EXPECTED_APPROVALS_SIZE, result.approvals().size());
        assertEquals(status, result.status());
    }

    private void initializeTestData() {
        employeeId = UUID.randomUUID();
        employeeApproverId = UUID.randomUUID();
        employee = createTestEmployee(UUID.randomUUID());
        timesheet = createTestTimesheet(TimesheetStatus.DRAFTED, employee, createTimesheetApproval(1L, employee));
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