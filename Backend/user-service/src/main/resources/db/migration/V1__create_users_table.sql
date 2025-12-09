CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    organization_name VARCHAR(255),
    password VARCHAR(255),
    refresh_token_id VARCHAR(255),
    role VARCHAR(255)
);

INSERT INTO users (
    user_id,
    email,
    name,
    organization_name,
    password,
    refresh_token_id,
    role
)
VALUES (
    1,
    'organizer@gmail.com',
    'System Organizer',
    'COLOMBO INTERNATIONAL BOOK FAIR',
    '$2a$12$exetL40BJ9HfStlZWQrToO58oVzbbyfzpS5GKPrnnBR9X54TafQUO', -- Org@1234
    '3210754e-a2e4-4a58-99b6-ae69090e7585',
    'ORGANIZER'
);

