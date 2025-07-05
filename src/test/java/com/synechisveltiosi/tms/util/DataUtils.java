package com.synechisveltiosi.tms.util;

import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.model.embed.PersonDetails;
import com.synechisveltiosi.tms.model.entity.*;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataUtils {

    public static final int DAYS_AGO_START = 27;
    public static final int DAYS_AGO_END = 20;
    public static final double WORK_HOURS = 8.0;
    public static final double OVERTIME_HOURS = 10.0;
    public static final String TEST_TASK_NAME = "Task 1";

    public static Employee createTestEmployee(UUID id) {
        return Employee.builder()
                .id(id)
                .projects(List.of())
                .tasks(List.of())
                .personDetails(createTestPersonDetails())
                .timesheets(List.of())
                .leaves(List.of())
                .build();
    }


    public static Timesheet createTestTimesheet(Employee employee) {
        return Timesheet.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .startDate(LocalDate.now().minus(DAYS_AGO_START, TimeUnit.DAYS.toChronoUnit()))
                .endDate(LocalDate.now().minus(DAYS_AGO_END, TimeUnit.DAYS.toChronoUnit()))
                .entries(createTestTimesheetEntries())
                .status(TimesheetStatus.DRAFTED)
                .approvals(new ArrayList<>(List.of(TimesheetApproval.builder()
                        .approver(employee)
                        .comments("Approved")
                        .status(TimesheetStatus.APPROVED)
                        .date(LocalDate.now())
                        .build())))
                .build();
    }

    public static List<TimesheetEntry> createTestTimesheetEntries() {
        return List.of(TimesheetEntry.builder()
                .date(LocalDate.now().minus(DAYS_AGO_END, TimeUnit.DAYS.toChronoUnit()))
                .hours(OVERTIME_HOURS)
                .task(Task.builder().name(TEST_TASK_NAME).build())
                .build());
    }

    public static TimesheetRequest createTestTimesheetRequest() {
        return new TimesheetRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                List.of(new TimesheetRequest.TimesheetEntryRequest(1L, LocalDate.now(), WORK_HOURS))
        );
    }

    public static TimesheetRequest createInvalidStartDateTimesheetRequest() {
        return new TimesheetRequest(
                LocalDate.now().plusDays(1),
                LocalDate.now(),
                List.of(new TimesheetRequest.TimesheetEntryRequest(1L, LocalDate.now(), WORK_HOURS))
        );
    }

    public static TimesheetRequest createInvalidEntryTimesheetRequest() {
        return new TimesheetRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                List.of()
        );
    }

    public static PersonDetails createTestPersonDetails() {
        return PersonDetails.builder()
                .name(createTestName())
                .contact(createTestContact())
                .address(createTestAddress())
                .build();
    }

    public static PersonDetails.Name createTestName() {
        return PersonDetails.Name.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    public static PersonDetails.Contact createTestContact() {
        return PersonDetails.Contact.builder()
                .contactNumber("1234567890")
                .emailAddress("john.doe@gmail.com")
                .build();
    }

    public static PersonDetails.Address createTestAddress() {
        return PersonDetails.Address.builder()
                .addressLine1("123 Main St")
                .addressLine2("Apt 456")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .country("USA")
                .build();
    }
}
