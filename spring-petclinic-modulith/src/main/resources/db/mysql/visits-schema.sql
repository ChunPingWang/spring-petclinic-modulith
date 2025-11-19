-- MySQL schema for Visits module

CREATE TABLE visits (
    id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    pet_id INT NOT NULL,
    vet_id INT NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(8192),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    FOREIGN KEY (pet_id) REFERENCES pets(id),
    FOREIGN KEY (vet_id) REFERENCES vets(id),
    INDEX idx_visits_pet_id (pet_id),
    INDEX idx_visits_vet_id (vet_id),
    INDEX idx_visits_status (status)
);
