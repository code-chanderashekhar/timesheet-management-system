package com.synechisveltiosi.tms.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "holiday", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String date;
}
