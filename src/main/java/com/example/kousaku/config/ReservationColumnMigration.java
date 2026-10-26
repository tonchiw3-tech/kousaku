package com.example.kousaku.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Adds columns introduced after the initial reservations table without relying on
 * MySQL 8.0.29's ADD COLUMN IF NOT EXISTS syntax.
 */
@Component
@DependsOn("dataSourceScriptDatabaseInitializer")
public class ReservationColumnMigration {

	private final JdbcTemplate jdbcTemplate;

	public ReservationColumnMigration(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	void addMissingReservationColumns() {
		addColumnIfMissing("source", "ALTER TABLE reservations ADD COLUMN source VARCHAR(100) NULL AFTER visit_type");
		addColumnIfMissing("note", "ALTER TABLE reservations ADD COLUMN note VARCHAR(1000) NULL AFTER source");
	}

	private void addColumnIfMissing(String columnName, String alterStatement) {
		if (columnExists(columnName)) {
			return;
		}

		try {
			jdbcTemplate.execute(alterStatement);
		} catch (DataAccessException exception) {
			// A simultaneous startup may have added it after the preceding check.
			if (!columnExists(columnName)) {
				throw exception;
			}
		}
	}

	private boolean columnExists(String columnName) {
		return !jdbcTemplate.queryForList("""
				SELECT 1
				FROM information_schema.columns
				WHERE UPPER(table_name) = 'RESERVATIONS'
				  AND UPPER(column_name) = UPPER(?)
				""", Integer.class, columnName).isEmpty();
	}
}
