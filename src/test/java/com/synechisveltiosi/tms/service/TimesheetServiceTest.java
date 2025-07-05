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
    private static final TimesheetStatus DEFAULT_STATUS = TimesheetStatus.DRAFTED;

    @Mock
    private TimesheetRepository timesheetRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TimesheetService.TimesheetMapper timesheetMapper;
    @InjectMocks
    private TimesheetService timesheetService;

    private UUID employeeId;
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
        @DisplayName("Should create timesheet when request is valid")
        void shouldCreateTimesheetWhenRequestIsValid() {
            // given
            mockDependenciesForSuccessfulCreation();

            // when
            TimesheetDto result = timesheetService.createTimesheet(employeeId, DEFAULT_STATUS, timesheetRequest);

            // then
            assertNotNull(result);
            verifyTimesheetWasSaved();
        }

        @Test
        @DisplayName("Should throw ValidationException when date range is invalid")
        void shouldThrowValidationExceptionWhenDateRangeIsInvalid() {
            // given
            TimesheetRequest invalidRequest = createInvalidStartDateTimesheetRequest();

            // when/then
            TimesheetValidationException timesheetValidationException = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.createTimesheet(employeeId, DEFAULT_STATUS, invalidRequest));
            assertEquals("Start date cannot be after end date", timesheetValidationException.getMessage());
        }

        @Test
        @DisplayName("Should throw ValidationException when date range is invalid")
        void shouldThrowValidationExceptionWhenEntriesIInvalid() {
            // given
            TimesheetRequest invalidRequest = createInvalidEntryTimesheetRequest();

            // when/then
            TimesheetValidationException timesheetValidationException = assertThrows(TimesheetValidationException.class,
                    () -> timesheetService.createTimesheet(employeeId, DEFAULT_STATUS, invalidRequest));
            assertEquals("Timesheet must contain at least one entry", timesheetValidationException.getMessage());
        }
    }

    private void initializeTestData() {
        employeeId = UUID.randomUUID();
        employee = createTestEmployee(UUID.randomUUID());
        timesheet = createTestTimesheet(employee);
        timesheetRequest = createTestTimesheetRequest();
    }

    private void mockTimesheetRepositoryToReturn(List<Timesheet> timesheets) {
        when(timesheetRepository.findByEmployeeId(employeeId))
                .thenReturn(Optional.of(timesheets));
    }

    private void mockDependenciesForSuccessfulCreation() {
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);
        when(timesheetMapper.toEntity(employee, DEFAULT_STATUS, timesheetRequest)).thenReturn(timesheet);
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
}