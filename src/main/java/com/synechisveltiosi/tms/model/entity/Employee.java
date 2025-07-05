package com.synechisveltiosi.tms.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.synechisveltiosi.tms.model.embed.PersonDetails;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employee")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    @Id
    @UuidGenerator
    private UUID id;

    @Embedded
    private PersonDetails personDetails;

    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
    @CollectionTable(name = "employee_project", joinColumns = @JoinColumn(name = "employee_id"))
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
    @CollectionTable(name = "employee_task", joinColumns = @JoinColumn(name = "employee_id"))
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Leave> leaves = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Timesheet> timesheets = new ArrayList<>();

    @ManyToOne
    @JoinTable(
            name = "employee_manager",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "manager_id")
    )
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    @Builder.Default
    private List<Employee> subordinates = new ArrayList<>();


    public void addProject(Project project) {
        projects.add(project);
        project.getEmployees().add(this);
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.getEmployees().add(this);
    }

    public void addLeave(Leave leave) {
        leaves.add(leave);
        leave.setEmployee(this);
    }

    public void addTimesheet(Timesheet timesheet) {
        timesheets.add(timesheet);
        timesheet.setEmployee(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.getEmployees().remove(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.getEmployees().remove(this);
    }
    public void removeLeave(Leave leave) {
        leaves.remove(leave);
        leave.setEmployee(null);
    }
    public void removeTimesheet(Timesheet timesheet) {
        timesheets.remove(timesheet);
        timesheet.setEmployee(null);
    }
}
