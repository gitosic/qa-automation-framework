package com.qa.framework.database;

import com.qa.framework.config.ConfigurationManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleDataTest {

    @Test
    void debugConnectionInfo() {
        var info = DatabaseUtil.query(
                "SELECT current_database(), current_schema(), version()"
        );
        info.forEach(System.out::println);
    }

    @Test
    void shouldGetUsersFromRealDatabase() {
        String schema = ConfigurationManager.getDbSchema();

        List<Map<String, Object>> users =
                DatabaseUtil.query("SELECT * FROM " + schema + ".users");

        assertThat(users).isNotEmpty();

        users.forEach(u ->
                System.out.printf(
                        "ID=%s | username=%s | email=%s | active=%s%n",
                        u.get("id"),
                        u.get("username"),
                        u.get("email"),
                        u.get("is_active")
                )
        );
    }
}
