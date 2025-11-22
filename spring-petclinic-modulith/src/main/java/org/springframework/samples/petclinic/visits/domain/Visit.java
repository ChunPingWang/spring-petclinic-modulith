/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.visits.domain;

import java.util.Date;
import java.util.Objects;

/**
 * Pure Java domain model representing a pet visit to a veterinarian.
 *
 * This is a framework-agnostic POJO containing only business logic.
 * No JPA, Spring, or validation annotations.
 *
 * @author PetClinic Team
 */
public class Visit {

    private Integer id;
    private Integer petId;
    private Integer vetId;
    private Date visitDate;
    private String description;
    private VisitStatus status;

    public Visit() {
        this.visitDate = new Date();
        this.status = VisitStatus.SCHEDULED;
    }

    public Visit(Integer petId, Integer vetId) {
        this();
        this.petId = petId;
        this.vetId = vetId;
    }

    // Business logic methods

    /**
     * Schedule a visit.
     */
    public void schedule() {
        validateForScheduling();
        this.status = VisitStatus.SCHEDULED;
        if (this.visitDate == null) {
            this.visitDate = new Date();
        }
    }

    /**
     * Complete a visit.
     */
    public void complete() {
        if (this.status != VisitStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled visits can be completed");
        }
        this.status = VisitStatus.COMPLETED;
    }

    /**
     * Cancel a visit.
     */
    public void cancel() {
        if (this.status == VisitStatus.COMPLETED) {
            throw new IllegalStateException("Completed visits cannot be cancelled");
        }
        this.status = VisitStatus.CANCELLED;
    }

    /**
     * Validate visit data for scheduling.
     */
    public void validateForScheduling() {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID is required and must be positive");
        }
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Vet ID is required and must be positive");
        }
        if (description != null && description.length() > 8192) {
            throw new IllegalArgumentException("Description must not exceed 8192 characters");
        }
    }

    /**
     * Check if visit is scheduled.
     */
    public boolean isScheduled() {
        return this.status == VisitStatus.SCHEDULED;
    }

    /**
     * Check if visit is completed.
     */
    public boolean isCompleted() {
        return this.status == VisitStatus.COMPLETED;
    }

    /**
     * Check if visit is cancelled.
     */
    public boolean isCancelled() {
        return this.status == VisitStatus.CANCELLED;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public Integer getVetId() {
        return vetId;
    }

    public void setVetId(Integer vetId) {
        this.vetId = vetId;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visit visit = (Visit) o;
        return Objects.equals(id, visit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id=" + id +
                ", petId=" + petId +
                ", vetId=" + vetId +
                ", visitDate=" + visitDate +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
