# Flyway migrations

This folder is intentionally empty right now. That is expected — read on before adding anything.

## Why it's empty (bootstrap step you must do once)

Your production schema was created by Hibernate's old `ddl-auto=update` setting, not by a
migration file. `application-prod.yml` now sets:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
```

`baseline-on-migrate: true` tells Flyway: "if you connect to a database that already has
tables but no Flyway history table, don't panic — just record the current state as
version 0 and move on." Since there are no migration files above version 0 yet, Flyway
does nothing else, and Hibernate's `validate` mode simply checks your entities still match
the tables that already exist. This is the safe way to adopt Flyway on a database that's
already live, without rewriting its schema.

**I could not verify this against a real database in this sandbox** (no network/Maven
here), so please confirm on a staging/test copy of your Render Postgres database before
relying on it in production:

1. Take a snapshot/backup of your current Render Postgres database.
2. Deploy this branch to a staging environment pointed at a **copy** of that database.
3. Check the startup logs for lines like `Successfully validated ... schema` and
   `Successfully baselined schema`. If Hibernate instead throws a
   `SchemaManagementException` mismatch, an entity has drifted from the real table
   (e.g. a column renamed in Java but never migrated) — fix that mismatch before
   deploying to real production.

### If you are deploying to a brand-new, empty database (no existing tables)

`baseline-on-migrate` only baselines a database that already has tables. Against an
empty database it will look for migration files starting at version 1 and run them —
and since none exist yet, **no tables will be created and the app will fail to start.**
For a fresh environment, do one of:

- Temporarily set `ddl-auto: update` for that one first boot to let Hibernate create the
  tables, then switch back to `validate` for all subsequent boots (matches how your
  existing production database was created), or
- Generate a proper `V1__baseline.sql` from your entities using Hibernate's schema
  export tool locally (`mvn spring-boot:run` against a scratch Postgres with
  `spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create`
  and `...create-target=V1__baseline.sql`), then place that file here before the first
  deploy.

## Going forward

Every future schema change (new column, new table, renamed field, etc.) ships as a new
file here, named `V{next-number}__short_description.sql`, e.g.
`V1__add_booking_notes_column.sql`. Do not edit already-applied migration files —
Flyway checksums them and will refuse to start if an applied file changes. Add a new
migration instead.
