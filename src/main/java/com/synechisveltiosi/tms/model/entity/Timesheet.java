package com.synechisveltiosi.tms.model.entity;

import com.synechisveltiosi.tms.model.enums.TimesheetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "timesheet")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timesheet implements Serializable {

    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TimesheetStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL)
    @OrderBy("date ASC")
    @Builder.Default
    private Collection<TimesheetEntry> entries = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL)
    @OrderBy("date ASC")
    @Builder.Default
    private Collection<TimesheetApproval> approvals = new ArrayList<>();

    public void addEntry(TimesheetEntry entry) {
        entries.add(entry);
        entry.setTimesheet(this);
    }

    public void addApproval(TimesheetApproval approval) {
        approvals.add(approval);
        approval.setTimesheet(this);
    }

}
