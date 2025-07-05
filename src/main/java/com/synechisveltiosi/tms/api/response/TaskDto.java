package com.synechisveltiosi.tms.api.response;

import java.io.Serializable;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Task}
 */
public record TaskDto(Long id, String name, String description) implements Serializable {
}