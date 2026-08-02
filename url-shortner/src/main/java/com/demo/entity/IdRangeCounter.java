package com.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ID_RANGE_COUNTER")
public class IdRangeCounter {

    @Id
    private Long id; // fixed = 1L, single row

    @Column(name = "next_available_id", nullable = false)
    private Long nextAvailableId;

    @Version // optimistic locking - safe under concurrent allocation requests
    private Long version;

    public IdRangeCounter(Long id, Long nextAvailableId) {
        this.id = id;
        this.nextAvailableId = nextAvailableId;
    }
}
