-- Spring PetClinic Modulith - Unified Database Schema
-- Contains all tables from: Customers, Vets, Visits, and Modulith Event Store

-- ==========================================
-- Customers Module Tables
-- ==========================================

DROP TABLE event_publication;
DROP TABLE visits;
DROP TABLE vet_specialties;
DROP TABLE vets;
DROP TABLE specialties;
DROP TABLE pets;
DROP TABLE types;
DROP TABLE owners;

CREATE TABLE types (
  id   INTEGER IDENTITY PRIMARY KEY,
  name VARCHAR(80)
);
CREATE INDEX types_name ON types (name);

CREATE TABLE owners (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(30),
  last_name  VARCHAR(30),
  address    VARCHAR(255),
  city       VARCHAR(80),
  telephone  VARCHAR(12)
);
CREATE INDEX owners_last_name ON owners (last_name);

CREATE TABLE pets (
  id         INTEGER IDENTITY PRIMARY KEY,
  name       VARCHAR(30),
  birth_date DATE,
  type_id    INTEGER NOT NULL,
  owner_id   INTEGER NOT NULL
);
ALTER TABLE pets ADD CONSTRAINT fk_pets_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE pets ADD CONSTRAINT fk_pets_types FOREIGN KEY (type_id) REFERENCES types (id);
CREATE INDEX pets_name ON pets (name);

-- ==========================================
-- Vets Module Tables
-- ==========================================

CREATE TABLE vets (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(30),
  last_name  VARCHAR(30)
);
CREATE INDEX vets_last_name ON vets (last_name);

CREATE TABLE specialties (
  id   INTEGER IDENTITY PRIMARY KEY,
  name VARCHAR(80)
);
CREATE INDEX specialties_name ON specialties (name);

CREATE TABLE vet_specialties (
  vet_id       INTEGER NOT NULL,
  specialty_id INTEGER NOT NULL
);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties (id);

-- ==========================================
-- Visits Module Tables
-- ==========================================

CREATE TABLE visits (
    id INTEGER IDENTITY NOT NULL PRIMARY KEY,
    pet_id INTEGER NOT NULL,
    vet_id INTEGER NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(8192),
    status VARCHAR(20) DEFAULT 'SCHEDULED'
);
ALTER TABLE visits ADD CONSTRAINT fk_visits_pets FOREIGN KEY (pet_id) REFERENCES pets(id);
ALTER TABLE visits ADD CONSTRAINT fk_visits_vets FOREIGN KEY (vet_id) REFERENCES vets(id);
CREATE INDEX idx_visits_pet_id ON visits(pet_id);
CREATE INDEX idx_visits_vet_id ON visits(vet_id);
CREATE INDEX idx_visits_status ON visits(status);

-- ==========================================
-- Spring Modulith Event Store
-- ==========================================

CREATE TABLE event_publication (
  id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
  event_type VARCHAR(255) NOT NULL,
  serialized_event LONGVARBINARY NOT NULL,
  listener_id VARCHAR(255) NOT NULL,
  publication_date TIMESTAMP NOT NULL,
  completion_date TIMESTAMP,
  UNIQUE (event_type, listener_id, publication_date)
);
CREATE INDEX idx_event_pub_completion ON event_publication(completion_date);
CREATE INDEX idx_event_pub_type ON event_publication(event_type);
