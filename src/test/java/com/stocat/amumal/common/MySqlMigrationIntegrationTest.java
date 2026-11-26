package com.stocat.amumal.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
class MySqlMigrationIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void migrationsAreAppliedAndHibernateValidatesTheSchema() {
    Integer appliedMigrations =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class);

    assertThat(appliedMigrations).isPositive();
  }
}
