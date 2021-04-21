package krusty;

import spark.Request;
import spark.Response;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static krusty.Jsonizer.toJson;

public class Database {
    /**
     * Modify it to fit your environment and then use this string when connecting to your database!
     */
    private static final String jdbcString = "jdbc:mysql://127.0.0.1:3306/?user=root/krusty?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    // For use with MySQL or PostgreSQL
    private static final String jdbcUsername = "root";
    private static final String jdbcPassword = "localhost";
    private Connection connection;

    public void connect() {
        try {
            connection = DriverManager.getConnection(jdbcString, jdbcUsername, jdbcPassword);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    // TODO: Implement and change output in all methods below!

    public String getCustomers(Request req, Response res) {
        String sql = "SELECT CustomerName AS name, CustomerAddress as address FROM Customer";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, "customers");
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Jsonizer.anythingToJson(exception.getMessage(), "status");
        }
    }

    public String getRawMaterials(Request req, Response res) {
        return "{}";
    }

    public String getCookies(Request req, Response res) {
        return "{\"cookies\":[]}";
    }

    public String getRecipes(Request req, Response res) {
        return "{}";
    }

    public String getPallets(Request req, Response res) {
        return "{\"pallets\":[]}";
    }

    public String reset(Request req, Response res) {
        return "{}";
    }

    public String createPallet(Request req, Response res) {
        return "{}";
    }
}
