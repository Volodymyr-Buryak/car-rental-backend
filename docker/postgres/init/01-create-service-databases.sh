set -eu

psql \
  --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --set=app_db_name="$APP_DB_NAME" \
  --set=app_db_user="$APP_DB_USER" \
  --set=app_db_password="$APP_DB_PASSWORD" \
  --set=keycloak_db_name="$KEYCLOAK_DB_NAME" \
  --set=keycloak_db_user="$KEYCLOAK_DB_USER" \
  --set=keycloak_db_password="$KEYCLOAK_DB_PASSWORD" <<-'EOSQL'
SELECT format(
  'CREATE ROLE %I LOGIN PASSWORD %L',
  :'app_db_user',
  :'app_db_password'
)
WHERE NOT EXISTS (
  SELECT 1 FROM pg_roles WHERE rolname = :'app_db_user'
) \gexec

SELECT format(
  'CREATE DATABASE %I OWNER %I',
  :'app_db_name',
  :'app_db_user'
)
WHERE NOT EXISTS (
  SELECT 1 FROM pg_database WHERE datname = :'app_db_name'
) \gexec

SELECT format(
  'CREATE ROLE %I LOGIN PASSWORD %L',
  :'keycloak_db_user',
  :'keycloak_db_password'
)
WHERE NOT EXISTS (
  SELECT 1 FROM pg_roles WHERE rolname = :'keycloak_db_user'
) \gexec

SELECT format(
  'CREATE DATABASE %I OWNER %I',
  :'keycloak_db_name',
  :'keycloak_db_user'
)
WHERE NOT EXISTS (
  SELECT 1 FROM pg_database WHERE datname = :'keycloak_db_name'
) \gexec
EOSQL
