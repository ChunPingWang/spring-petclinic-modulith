-- MySQL sample data for Visits module

INSERT INTO visits (pet_id, vet_id, visit_date, description, status) VALUES
(1, 1, '2024-11-15 10:00:00', 'Routine checkup', 'COMPLETED'),
(1, 2, '2024-11-18 14:30:00', 'Vaccination', 'COMPLETED'),
(2, 1, '2024-11-19 09:00:00', 'Dental cleaning', 'SCHEDULED'),
(3, 3, '2024-11-20 11:00:00', 'Skin condition follow-up', 'SCHEDULED'),
(4, 2, '2024-11-17 15:00:00', 'Post-surgery check', 'COMPLETED'),
(5, 1, '2024-11-19 10:30:00', 'General examination', 'SCHEDULED'),
(6, 4, '2024-11-18 13:00:00', 'Eye examination', 'COMPLETED');
