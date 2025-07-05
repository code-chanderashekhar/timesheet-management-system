package com.synechisveltiosi.tms.api.response;

import java.io.Serializable;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Holiday}
 */
public record HolidayDto(Long id, String name, String description, String date) implements Serializable {
}