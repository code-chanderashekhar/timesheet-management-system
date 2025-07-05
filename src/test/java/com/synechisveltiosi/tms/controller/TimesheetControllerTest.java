package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.util.DataUtils;
import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.model.entity.Employee;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.repository.EmployeeRepository;
import com.synechisveltiosi.tms.service.TimesheetService;
import com.synechisveltiosi.tms.util.RestRequestSpecification;
import com.synechisveltiosi.tms.util.RestResponseSpecification;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimesheetControllerTest {
    private static final String BASE_URI = "http://localhost/api/v1/timesheets";
    private static final String POSTGRES_IMAGE = "postgres:latest";
    private static final String ERROR_TITLE = "Timesheet Validation Failed";
    private static final String ERROR_START_DATE = "Start date cannot be after end date";
    private static final String ERROR_EMPTY_ENTRIES = "Timesheet must contain at least one entry";
    private static final String ERROR_METHOD = "POST";
    public static final String TITLE = "title";
    public static final String DETAIL = "detail";
    public static final String METHOD = "method";

    @LocalServerPort
    private int serverPort;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TimesheetService timesheetService;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse(POSTGRES_IMAGE)
    );

    private RestRequestSpecification requestSpec;
    private RestResponseSpecification responseSpec;

    @BeforeEach
    void setup() {
        initializeRestAssured();
        initializeSpecifications();
        postgreSQLContainer.start();
    }

    // Test Cases - Successful Scenarios
    @Test
    void shouldCreateDraftTimesheet_WhenValidEmployeeAndData() {
        Employee testEmployee = getEmployeeOrElseThrow();

        createValidTimesheetRequest(testEmployee.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue());
    }

    @Test
    void shouldRetrieveTimesheets_WhenValidEmployee() {
        Employee testEmployee = getEmployeeOrElseThrow();
        createTestTimesheet(testEmployee);

        getEmployeeTimesheetsRequest(testEmployee.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(1))
                .body("id", notNullValue());
    }


    // Test Cases - Error Scenarios
    @Test
    void shouldReturnNotFound_WhenEmployeeDoesNotExist() {
        getEmployeeTimesheetsRequest(UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnNotFound_WhenEmployeeHasNoTimesheets() {
        getEmployeeTimesheetsRequest(UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidStartDate() {
        Employee testEmployee = getEmployeeOrElseThrow();

        createInvalidStartDateTimesheetRequest(testEmployee.getId())
                .then()
                .spec(responseSpec.getErrorResponseSpec())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(TITLE, equalTo(ERROR_TITLE),
                        DETAIL, equalTo(ERROR_START_DATE),
                        METHOD, equalTo(ERROR_METHOD));
    }

    @Test
    void shouldReturnBadRequest_WhenEmptyEntries() {
        Employee testEmployee = getEmployeeOrElseThrow();

        createInvalidEntryTimesheetRequest(testEmployee.getId())
                .then()
                .spec(responseSpec.getErrorResponseSpec())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(TITLE, equalTo(ERROR_TITLE),
                        DETAIL, equalTo(ERROR_EMPTY_ENTRIES),
                        METHOD, equalTo(ERROR_METHOD));
    }

    // Helper Methods
    private void initializeRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.baseURI = BASE_URI;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private void initializeSpecifications() {
        requestSpec = new RestRequestSpecification();
        responseSpec = new RestResponseSpecification();
    }

    private TimesheetDto createTestTimesheet(Employee employee) {
        return timesheetService.createTimesheet(
                employee.getId(),
                TimesheetStatus.DRAFTED,
                DataUtils.createTestTimesheetRequest()
        );
    }

    private Response createTimesheetRequest(UUID employeeId, TimesheetRequest requestBody) {
        return RestAssured
                .given()
                .spec(requestSpec.getBasicRequestSpec())
                .body(requestBody)
                .when()
                .post(URLConstants.TimesheetEndpoint.BY_EMP_ID_TMS_STATUS, employeeId, TimesheetStatus.DRAFTED);
    }

    private Response getEmployeeTimesheetsRequest(UUID employeeId) {
        return RestAssured
                .given()
                .spec(requestSpec.getBasicRequestSpec())
                .when()
                .get(URLConstants.TimesheetEndpoint.BY_EMP_ID, employeeId);
    }

    public Response getEmployeeTimesheetApprovalRequest(UUID timesheetId, UUID empApproverId, TimesheetApprovalRequest timesheetApprovalRequest) {
        return RestAssured
                .given()
                .spec(requestSpec.getBasicRequestSpec())
                .body(timesheetApprovalRequest)
                .when()
                .post(URLConstants.TimesheetEndpoint.BY_TMS_ID_EMP_ID, timesheetId, empApproverId);
    }

    private Response createValidTimesheetRequest(UUID employeeId) {
        return createTimesheetRequest(employeeId, DataUtils.createTestTimesheetRequest());
    }

    private Response createInvalidStartDateTimesheetRequest(UUID employeeId) {
        return createTimesheetRequest(employeeId, DataUtils.createInvalidStartDateTimesheetRequest());
    }

    private Response createInvalidEntryTimesheetRequest(UUID employeeId) {
        return createTimesheetRequest(employeeId, DataUtils.createInvalidEntryTimesheetRequest());
    }

    private Employee getEmployeeOrElseThrow() {
        return employeeRepository.findAll()
                .stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No test employees found"));
    }
}