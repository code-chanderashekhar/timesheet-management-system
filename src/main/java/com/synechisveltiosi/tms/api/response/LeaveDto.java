package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.enums.LeaveStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Leave}
 */
public record LeaveDto(UUID id, LocalDate startDate, LocalDate endDate, LeaveStatus status, String reason, double hours,
                       String comments) implements Serializable {
}