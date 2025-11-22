-- Spring PetClinic Modulith - Unified MySQL Database Schema
-- Contains all tables from: Customers, Vets, Visits, and Modulith Event Store

CREATE DATABASE IF NOT EXISTS petclinic;

USE petclinic;

-- ==========================================
-- Customers Module Tables
-- ==========================================

CREATE TABLE IF NOT EXISTS types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS owners (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS pets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30),
  birth_date DATE,
  type_id INT(4) UNSIGNED NOT NULL,
  owner_id INT(4) UNSIGNED NOT NULL,
  INDEX(name),
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id)
) engine=InnoDB;

-- ==========================================
-- Vets Module Tables
-- ==========================================

CREATE TABLE IF NOT EXISTS vets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id INT(4) UNSIGNED NOT NULL,
  specialty_id INT(4) UNSIGNED NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  UNIQUE (vet_id,specialty_id)
) engine=InnoDB;

-- ==========================================
-- Visits Module Tables
-- ==========================================

CREATE TABLE IF NOT EXISTS visits (
    id INT(4) UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
    pet_id INT(4) UNSIGNED NOT NULL,
    vet_id INT(4) UNSIGNED NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(8192),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    FOREIGN KEY (pet_id) REFERENCES pets(id),
    FOREIGN KEY (vet_id) REFERENCES vets(id),
    INDEX idx_visits_pet_id (pet_id),
    INDEX idx_visits_vet_id (vet_id),
    INDEX idx_visits_status (status)
) engine=InnoDB;

-- ==========================================
-- Spring Modulith Event Store
-- ==========================================

CREATE TABLE IF NOT EXISTS event_publication (
  id CHAR(36) NOT NULL PRIMARY KEY COMMENT 'UUID',
  event_type VARCHAR(255) NOT NULL COMMENT 'Type of the published domain event',
  serialized_event LONGBLOB NOT NULL COMMENT 'The serialized form of the domain event',
  listener_id VARCHAR(255) NOT NULL COMMENT 'The listener id or listener class name',
  publication_date TIMESTAMP NOT NULL COMMENT 'When the event was published',
  completion_date TIMESTAMP NULL COMMENT 'When the publication was completed',
  UNIQUE KEY event_type_listener_id (event_type, listener_id, publication_date),
  KEY idx_completion_date (completion_date),
  KEY idx_event_type (event_type)
) engine=InnoDB;
