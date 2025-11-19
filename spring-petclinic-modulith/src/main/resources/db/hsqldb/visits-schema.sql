-- HSQLDB schema for Visits module

CREATE TABLE visits (
    id INTEGER IDENTITY NOT NULL PRIMARY KEY,
    pet_id INTEGER NOT NULL,
    vet_id INTEGER NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(8192),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    FOREIGN KEY (pet_id) REFERENCES pets(id),
    FOREIGN KEY (vet_id) REFERENCES vets(id)
);

CREATE INDEX idx_visits_pet_id ON visits(pet_id);
CREATE INDEX idx_visits_vet_id ON visits(vet_id);
CREATE INDEX idx_visits_status ON visits(status);
