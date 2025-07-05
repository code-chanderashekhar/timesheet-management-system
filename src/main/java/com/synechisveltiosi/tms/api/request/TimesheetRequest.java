package com.synechisveltiosi.tms.api.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record TimesheetRequest(
        LocalDate startDate,
        LocalDate endDate,
        List<TimesheetEntryRequest> entries
) {
    public record TimesheetEntryRequest(
            @NotNull(message = "Task ID cannot be null")
            Long taskId,
            @NotNull(message = "Hours cannot be null")
            LocalDate date,
            @NotNull(message = "Hours cannot be null")
            @DecimalMin(value = "0", message = "Hours cannot be negative")
            @DecimalMax(value = "24", message = "Hours cannot be greater than 24")
            double hours) {
    }
}
