package com.synechisveltiosi.tms.model.entity;


import com.synechisveltiosi.tms.model.enums.LeaveStatus;
import com.synechisveltiosi.tms.model.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Leave implements Serializable {
    @Id
    @UuidGenerator
    private UUID id;

    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private LeaveType type;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    private String reason;
    private double hours;
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
