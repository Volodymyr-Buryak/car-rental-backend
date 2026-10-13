CREATE SCHEMA IF NOT EXISTS user_module;

CREATE TYPE user_module.user_status AS ENUM ('ACTIVE', 'BLOCKED');
CREATE TYPE user_module.identity_document_type AS ENUM ('ID_CARD','BOOKLET');

CREATE TABLE user_module.users
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identity_out_ref UUID NOT NULL UNIQUE,

    email            VARCHAR(320) UNIQUE,
    email_verified   BOOLEAN NOT NULL DEFAULT FALSE,

    first_name       VARCHAR(150),
    last_name        VARCHAR(150),
    middle_name      VARCHAR(150),
    phone_number     VARCHAR(16) UNIQUE,
    avatar_key       VARCHAR(255),

    status user_module.user_status NOT NULL DEFAULT 'ACTIVE',

    created_at       TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_first_name
        CHECK (first_name IS NULL OR length(btrim(first_name)) >= 2),

    CONSTRAINT chk_users_last_name
        CHECK (last_name IS NULL OR length(btrim(last_name)) >= 2),

    CONSTRAINT chk_users_middle_name
        CHECK (middle_name IS NULL OR length(btrim(middle_name)) >= 2),

    CONSTRAINT chk_users_phone_number
        CHECK (phone_number IS NULL  OR phone_number ~ '^\+[1-9][0-9]{4,14}$' ),

    CONSTRAINT chk_users_updated_at
        CHECK (updated_at >= created_at)
);

CREATE TABLE user_module.client_profiles
(
    user_id    UUID PRIMARY KEY,
    birth_date DATE,

    CONSTRAINT fk_client_profiles_user FOREIGN KEY (user_id)
        REFERENCES user_module.users (id) ON DELETE CASCADE
);

CREATE TABLE user_module.driver_licenses
(
    client_id            UUID PRIMARY KEY,

    series               VARCHAR(3) NOT NULL,
    number               VARCHAR(6) NOT NULL,

    issued_at            DATE NOT NULL,
    expires_at           DATE,
    category_b_opened_at DATE NOT NULL,

    CONSTRAINT fk_driver_licenses_client FOREIGN KEY (client_id)
        REFERENCES user_module.client_profiles (user_id) ON DELETE CASCADE,

    CONSTRAINT uq_driver_licenses_identity UNIQUE (series, number),
    CONSTRAINT chk_driver_licenses_series CHECK (series ~ '^[A-ZА-ЯІЇЄҐ]{3}$'),
    CONSTRAINT chk_driver_licenses_number CHECK (number ~ '^[0-9]{6}$'),

    CONSTRAINT chk_driver_licenses_dates
        CHECK (expires_at IS NULL OR (expires_at >= issued_at AND expires_at >= category_b_opened_at))
);

CREATE TABLE user_module.identity_documents
(
    client_id  UUID PRIMARY KEY,
    type user_module.identity_document_type NOT NULL,
    series     VARCHAR(2),
    number     VARCHAR(9) NOT NULL,
    expires_at DATE,

    CONSTRAINT fk_identity_documents_client FOREIGN KEY (client_id)
            REFERENCES user_module.client_profiles (user_id)ON DELETE CASCADE,

    CONSTRAINT chk_identity_documents_type
        CHECK (type IN ('ID_CARD', 'BOOKLET')),

    CONSTRAINT chk_identity_documents_format
        CHECK (
            (
                type = 'ID_CARD'
                    AND series IS NULL
                    AND number ~ '^[0-9]{9}$'
                )
                OR
            (
                type = 'BOOKLET'
                    AND series IS NOT NULL
                    AND series ~ '^[А-ЯІЇЄҐ]{2}$'
                    AND number ~ '^[0-9]{6}$'
                    AND expires_at IS NULL
                )
            )
);