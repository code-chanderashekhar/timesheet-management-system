package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.Task;
import com.synechisveltiosi.tms.model.entity.TimesheetEntry;
import com.synechisveltiosi.tms.model.enums.TimesheetEntryType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.TimesheetEntry}
 */
public record TimesheetEntryDto(UUID id, Long taskId, TimesheetEntryType entryType, LocalDate date,
                                double hours, boolean disable) implements Serializable {
    public TimesheetEntryDto(TimesheetEntry t) {
        this(t.getId(), getTaskId(t.getTask()), t.getEntryType(), t.getDate(), t.getHours(), t.isDisable());
    }

    private static Long getTaskId(Task task) {
        if (task == null) return null;
        return task.getId();
    }
}