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

import java.time.LocalDate;
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

    /**
     * Creates a new timesheet for the specified employee with the given status.
     * The timesheet can be created in either a drafted or submitted state, based on the provided status.
     *
     * @param empId            The unique identifier of the employee for whom the timesheet is being created.
     * @param status           The status of the timesheet, indicating whether it is drafted or submitted.
     * @param timesheetRequest The request payload containing details of the timesheet to be created.
     * @return A {@link ResponseEntity} containing the created {@link TimesheetDto} and the HTTP status code.
     */
    @Operation(
            summary = "Create timesheet",
            description = "Creates a new timesheet with drafted or submitted for the specified employee"
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
            @PathVariable("empId") @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID empId,
            @PathVariable("status") @Parameter(description = TIMESHEET_STATUS_DESC) TimesheetStatus status,
            @Valid @RequestBody TimesheetRequest timesheetRequest) {
        TimesheetDto timesheet = timesheetService.draftOrSubmitTimesheet(empId, status, timesheetRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(timesheet);
    }

    /**
     * Generates a list of timesheets with "Created" status for all employees within the specified date range.
     *
     * @param startDate The start date of the timesheet generation period.
     * @param endDate   The end date of the timesheet generation period.
     * @return A {@link ResponseEntity} containing a success message as a string.
     */
    @Operation(
            summary = "Generate timesheet",
            description = "Generate list of timesheet with created status for the all employee"
    )
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @ApiResponse(responseCode = "200", description = "Timesheet successfully created",
            content = @Content(schema = @Schema(implementation = String.class)))
    @PostMapping("/generate")
    public ResponseEntity<String> generateTimesheets(@RequestParam @Parameter(description = "startDate") LocalDate startDate,
                                                     @RequestParam @Parameter(description = "lastDate") LocalDate endDate) {
        timesheetService.generateTimesheets(startDate, endDate);
        return ResponseEntity.ok("timesheet Generated Successfully");
    }

    /**
     * Retrieves all timesheets for the specified employee.
     *
     * @param empId The unique identifier of the employee whose timesheets are to be retrieved.
     * @return A {@link ResponseEntity} containing a list of {@link TimesheetDto} representing the employee's timesheets.
     */
    @Operation(
            summary = "Get All timesheets by employee id",
            description = "Retrieves all timesheets for the specified employee"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Timesheet successfully retrieved",
            content = @Content(schema = @Schema(
                    implementation = TimesheetDto.class
            ))
    )
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @GetMapping(URLConstants.TimesheetEndpoint.BY_EMP_ID)
    public ResponseEntity<List<TimesheetDto>> getAllTimesheetsByEmployeeId(
            @PathVariable("empId") @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID empId) {
        List<TimesheetDto> timesheetDos = timesheetService.getAllTimesheetByEmployeeId(empId);
        return ResponseEntity.ok(timesheetDos);
    }

    /**
     * Retrieves the timesheet based on the provided timesheet ID.
     *
     * @param tmsId The unique identifier of the timesheet to be retrieved.
     * @return A {@link ResponseEntity} containing the {@link TimesheetDto} representing the timesheet details.
     */
    @Operation(
            summary = "Get timesheet By ID",
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
    @GetMapping(URLConstants.TimesheetEndpoint.BY_TMS_ID)
    public ResponseEntity<TimesheetDto> getAllTimesheetsByTimesheetById(
            @PathVariable("tmsId") @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID tmsId) {
        TimesheetDto timesheetDto = timesheetService.getTimesheetByTimesheetById(tmsId);
        return ResponseEntity.ok(timesheetDto);
    }


    /**
     * Approves the timesheet for the specified timesheet ID and employee approver ID.
     *
     * @param tmsId                    The unique identifier of the timesheet to be approved.
     * @param empId                    The unique identifier of the employee who is approving the timesheet.
     * @param timesheetApprovalRequest The request payload containing approval-related details for the timesheet.
     * @return A {@link ResponseEntity} containing the updated {@link TimesheetDto} representing the approved timesheet details.
     */
    @Operation(
            summary = "Approve timesheet",
            description = "Approve timesheet for timesheetId and employeeApproverId"
    )
    @ApiResponse(responseCode = "200", description = "Timesheet approved status updated",
            content = @Content(schema = @Schema(implementation = TimesheetDto.class)))
    @ApiResponse(responseCode = "404", description = RESOURCE_NOT_FOUND)
    @ApiResponse(responseCode = "400", description = RESOURCE_INVALID_DATA)
    @PostMapping(URLConstants.TimesheetEndpoint.BY_TMS_ID_EMP_ID)
    public ResponseEntity<TimesheetDto> approveTimesheet(
            @PathVariable("tmsId") @Parameter(description = TIMESHEET_ID_DESC, required = true) UUID tmsId,
            @PathVariable("empId") @Parameter(description = EMPLOYEE_ID_DESC, required = true) UUID empId,
            @Valid @RequestBody TimesheetApprovalRequest timesheetApprovalRequest) {
        TimesheetDto timesheetDto = timesheetService.approveTimesheet(tmsId, empId, timesheetApprovalRequest);
        return ResponseEntity.ok(timesheetDto);
    }
}
