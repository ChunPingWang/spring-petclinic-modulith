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
package org.springframework.samples.petclinic.vets.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pure Java domain model representing a veterinarian.
 *
 * This is a framework-agnostic POJO containing only business logic.
 * No JPA, Spring, or validation annotations.
 *
 * @author PetClinic Team
 */
public class Vet {

    private Integer id;
    private String firstName;
    private String lastName;
    private List<Specialty> specialties;

    public Vet() {
        this.specialties = new ArrayList<>();
    }

    public Vet(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialties = new ArrayList<>();
    }

    // Business logic methods

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public void addSpecialty(Specialty specialty) {
        if (specialty == null) {
            throw new IllegalArgumentException("Specialty cannot be null");
        }
        if (!this.specialties.contains(specialty)) {
            this.specialties.add(specialty);
        }
    }

    public void removeSpecialty(Specialty specialty) {
        if (specialty != null) {
            this.specialties.remove(specialty);
        }
    }

    public int getNrOfSpecialties() {
        return this.specialties.size();
    }

    public List<Specialty> getSpecialties() {
        return Collections.unmodifiableList(this.specialties);
    }

    public void validateForCreation() {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSpecialties(List<Specialty> specialties) {
        this.specialties = specialties != null ? new ArrayList<>(specialties) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vet vet = (Vet) o;
        return Objects.equals(id, vet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vet{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", specialties=" + specialties.size() +
                '}';
    }
}
