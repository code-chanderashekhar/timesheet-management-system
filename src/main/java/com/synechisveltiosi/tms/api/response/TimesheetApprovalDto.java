package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.entity.TimesheetApproval;
import com.synechisveltiosi.tms.model.enums.TimesheetStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.TimesheetApproval}
 */
public record TimesheetApprovalDto(Long id, UUID employeeApproverId, LocalDate date, TimesheetStatus status,
                                   String comments) implements Serializable {
    public TimesheetApprovalDto(TimesheetApproval approval) {
        this(approval.getId(), getApproval(approval), approval.getDate(), approval.getStatus(), approval.getComments());
    }

    private static UUID getApproval(TimesheetApproval approval) {
        if (approval.getApprover() == null) {
            return null;
        }
        return approval.getApprover().getId();
    }
}