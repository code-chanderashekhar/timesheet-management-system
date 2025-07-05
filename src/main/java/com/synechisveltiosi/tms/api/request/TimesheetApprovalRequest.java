package com.synechisveltiosi.tms.api.request;

import com.synechisveltiosi.tms.model.enums.TimesheetStatus;

public record TimesheetApprovalRequest(String comments, TimesheetStatus status) {
}
