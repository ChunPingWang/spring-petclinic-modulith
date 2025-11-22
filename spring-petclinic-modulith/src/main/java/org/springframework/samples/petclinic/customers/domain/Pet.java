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
package org.springframework.samples.petclinic.customers.domain;

import java.util.Date;
import java.util.Objects;

/**
 * Pure Java domain model representing a pet.
 *
 * This is a framework-agnostic POJO containing only business logic.
 * No JPA, Spring, or validation annotations.
 *
 * @author PetClinic Team
 */
public class Pet {

    private Integer id;
    private String name;
    private Date birthDate;
    private PetType type;
    private Customer owner;

    public Pet() {
    }

    public Pet(Integer id, String name, Date birthDate, PetType type) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.type = type;
    }

    // Business logic methods

    public void validateForCreation() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name is required");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date is required");
        }
        if (birthDate.after(new Date())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        if (type == null) {
            throw new IllegalArgumentException("Pet type is required");
        }
    }

    public int getAgeInYears() {
        if (birthDate == null) {
            return 0;
        }
        long ageInMillis = System.currentTimeMillis() - birthDate.getTime();
        return (int) (ageInMillis / (1000L * 60 * 60 * 24 * 365));
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(id, pet.id) &&
               Objects.equals(name, pet.name) &&
               Objects.equals(birthDate, pet.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, birthDate);
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + (type != null ? type.getName() : "null") +
                '}';
    }
}
