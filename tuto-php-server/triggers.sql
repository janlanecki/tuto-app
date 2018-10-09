CREATE OR REPLACE FUNCTION tags_dag_modify () RETURNS TRIGGER AS $$
DECLARE
  counter INTEGER := 0;
BEGIN
  WITH RECURSIVE cte AS (
    SELECT parent, child
    FROM tags_dag
    WHERE child = NEW.parent
    UNION  ALL
    SELECT t.parent, t.child
    FROM cte c
    JOIN tags_dag t ON t.child = c.parent
    WHERE t.child <> NEW.parent)
  SELECT COUNT(*) INTO counter
  FROM cte
  WHERE parent = NEW.child;

  IF (counter > 0) THEN
    RAISE EXCEPTION 'Attempt to create a cyclic relation detected.';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tags_dag_modify_trigger AFTER INSERT OR UPDATE
  ON tags_dag FOR EACH ROW
  EXECUTE PROCEDURE tags_dag_modify();


CREATE OR REPLACE FUNCTION delete_expired_inactive_users() RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM inactive_users WHERE start_date < NOW() - INTERVAL '1 hour';
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expired_inactive_users_trigger BEFORE INSERT OR UPDATE
  ON inactive_users
  EXECUTE PROCEDURE delete_expired_inactive_users();


CREATE OR REPLACE FUNCTION delete_previous_code() RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM reset_codes WHERE email = NEW.email;
  DELETE FROM reset_codes WHERE sent_date < NOW() - INTERVAL '1 hour';
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expired_reset_codes_trigger BEFORE INSERT
  ON reset_codes FOR EACH ROW
  EXECUTE PROCEDURE delete_previous_code();