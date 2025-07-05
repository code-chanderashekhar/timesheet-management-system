package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.TimesheetEntry;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.TimesheetEntry}
 */
public record TimesheetEntryDto(UUID id, LocalDate date, double hours) implements Serializable {
    public TimesheetEntryDto(TimesheetEntry t) {
        this(t.getId(), t.getDate(), t.getHours());
    }
}