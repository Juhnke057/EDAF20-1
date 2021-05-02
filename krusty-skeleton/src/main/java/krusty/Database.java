package krusty;

import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;


public class Database {

    private static final String jdbcUsername = "root";
    private static final String jdbcPassword = "localhost";
    private static final String hostIp = "127.0.0.1";
    private static final String hostPort = "3306";
    private static final String database = "krusty";
    private static final String timezone = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static Connection connection;

    private static final String jdbcString = "jdbc:mysql://" + hostIp + ":" + hostPort + "/?user=" + jdbcUsername + "/" + database + timezone;

    public void connect() {
        try {
            connection = DriverManager.getConnection(jdbcString, jdbcUsername, jdbcPassword);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public String getCustomers(Request req, Response res) {
        String sql = "SELECT CustomerName AS name, CustomerAddress as address FROM krusty.Customer";
        return executeQuery(sql, "customers");
    }

    public String getRawMaterials(Request req, Response res) {
        String sql = "SELECT IngredientName AS name, StockAmount AS amount, Unit AS unit FROM krusty.Storage";
        return executeQuery(sql, "raw-materials");
    }

    public String getCookies(Request req, Response res) {
        String sql = "SELECT CookieName AS name FROM krusty.cookies";
        return executeQuery(sql, "cookies");
    }

    public String getRecipes(Request req, Response res) {
        String sql = "SELECT CookieName AS cookie, Recipe.IngredientName AS raw_material, Amount AS amount, Storage.Unit AS unit\n" +
                "FROM krusty.Recipe\n" +
                "INNER JOIN krusty.Storage ON Recipe.IngredientName = Storage.IngredientName";
        return executeQuery(sql, "recipes");
    }

    private static String executeQuery(String sql, String table) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, table);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Jsonizer.anythingToJson(exception.getMessage(), "status");
        }
    }

    public String getPallets(Request req, Response res) {
        return "{\"pallets\":[]}";
    }

    public String reset(Request req, Response res) throws IOException, SQLException {
        setForeignKeyCheck(false);
        truncateTables();
        insertDataIntoTables();
        setForeignKeyCheck(true);
        return Jsonizer.anythingToJson("status", "ok");
    }

    private void setForeignKeyCheck(boolean on) throws SQLException {
        connection.createStatement().executeQuery(
                "SET FOREIGN_KEY_CHECKS = " + (on ? "1" : "0") + ";"
        );
    }

    private void truncateTables() throws SQLException {
        for (String table : Arrays.asList("Customer", "Cookies", "Storage", "Recipe", "Pallets")) {
            connection.createStatement().executeUpdate("TRUNCATE TABLE krusty." + table + ";");
        }
    }

    private void insertDataIntoTables() throws SQLException {
        for (String table : Arrays.asList("Customer", "Cookies", "Storage", "Recipe")) {
            connection.createStatement().execute(readFile("reset-" + table + ".sql"));
        }
    }

    private String readFile(String file) {
        try {
            String path = "krusty-skeleton/" + file;
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException exception) {
            return exception.getMessage();
        }
    }

    public String createPallet(Request req, Response res) throws SQLException {
        if (req.queryParams("cookie") != null) {
            String cookie = req.queryParams("cookie");
            return createPallet(cookie);
        } else{
            return "{}";
        }
    }
    protected String createPallet(String cookie) throws SQLException {
        String sql2 = "SELECT CookieName AS Cookie FROM krusty.Pallets";
        String sql = "INSERT INTO krusty.Pallets(CookieName) VALUES(?)";
        PreparedStatement st = connection.prepareStatement(sql);
        st.setString(1,cookie);
        st.executeUpdate();
        return executeQuery(sql2,"pallets");
    }

}
