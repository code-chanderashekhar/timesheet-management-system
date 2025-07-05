package com.synechisveltiosi.tms.controller;

import com.synechisveltiosi.tms.api.constants.url.URLConstants;
import com.synechisveltiosi.tms.api.request.TimesheetApprovalRequest;
import com.synechisveltiosi.tms.api.request.TimesheetRequest;
import com.synechisveltiosi.tms.api.response.TimesheetDto;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import com.synechisveltiosi.tms.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.synechisveltiosi.tms.api.constants.swagger.EmployeeConstants.EMPLOYEE_ID_DESC;
import static com.synechisveltiosi.tms.api.constants.swagger.EmployeeConstants.TIMESHEET_STATUS_DESC;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_INVALID_DATA;
import static com.synechisveltiosi.tms.api.constants.swagger.ResourceConstants.RESOURCE_NOT_FOUND;
import static com.synechisveltiosi.tms.api.constants.swagger.TimesheetConstants.TIMESHEET_ID_DESC;


@RestController
@RequestMapping(URLConstants.TimesheetEndpoint.BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Timesheet", description = "Timesheet API")
public class TimesheetController {


    private final TimesheetService timesheetService;

    @Operation(
            summary = "Create draft timesheet",
            description = "Creates a new draft timesheet for the specified employee"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Timesheet successfully created",
            content = @Content(schema = @Schema(implementation = TimesheetDto.class))
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @PostMapping(URLConstants.TimesheetEndpoint.BY_EMP_ID_TMS_STATUS)
    public ResponseEntity<TimesheetDto> createTimesheetForEmployeeWithStatus(
            @PathVariable @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID empId,
            @PathVariable @Parameter(description = TIMESHEET_STATUS_DESC) TimesheetStatus status,
            @Valid @RequestBody TimesheetRequest timesheetRequest) {
        TimesheetDto timesheet = timesheetService.createTimesheet(empId, status, timesheetRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timesheet);
    }

    @Operation(
            summary = "Get employee timesheets",
            description = "Retrieves all timesheets for the specified employee"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Timesheets successfully retrieved",
            content = @Content(schema = @Schema(
                    implementation = TimesheetDto.class,
                    type = "array"
            ))
    )
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping(URLConstants.TimesheetEndpoint.BY_EMP_ID)
    public ResponseEntity<List<TimesheetDto>> getAllTimesheetsForEmployee(
            @PathVariable @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID empId) {
        List<TimesheetDto> timesheets = timesheetService.getEmployeeTimesheets(empId);
        return ResponseEntity.ok(timesheets);
    }

    @ApiResponse(responseCode = "200", description = "Timesheet approved status updated")
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @PostMapping(URLConstants.TimesheetEndpoint.BY_TMS_ID_EMP_ID)
    public ResponseEntity<TimesheetDto> approveTimesheet(
            @PathVariable @Parameter(description = TIMESHEET_ID_DESC, required = true) UUID tmsId,
            @PathVariable @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID empId,
            @Valid @RequestBody TimesheetApprovalRequest timesheetApprovalRequest) {
        TimesheetDto timesheetDto = timesheetService.approveTimesheet(tmsId, empId, timesheetApprovalRequest);
        return ResponseEntity.ok(timesheetDto);
    }
}
