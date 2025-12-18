package com.qa.framework.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public final class DatabaseUtil {

    private DatabaseUtil() {
    }

    public static List<Map<String, Object>> query(String sql, Object... params) {
        try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> result = new ArrayList<>();

            var meta = rs.getMetaData();
            int columns = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columns; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("❌ Query failed: " + sql, e);
        }
    }
}
