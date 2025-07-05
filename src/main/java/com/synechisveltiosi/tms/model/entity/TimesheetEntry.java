package com.synechisveltiosi.tms.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "timesheet_entry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEntry implements Serializable {

    @Id
    @UuidGenerator
    private UUID id;

    private LocalDate date;
    private double hours;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    public void setTask(Task task) {
        this.task = task;
        task.getTimesheetEntries().add(this);
    }
}
