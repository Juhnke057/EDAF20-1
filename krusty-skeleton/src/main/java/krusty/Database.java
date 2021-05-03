package krusty;

import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


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
        String sql = "SELECT PalletNbr AS id, CookieName AS cookie,TimeProduced AS production_date,CustomerName AS customer, IF(BlockedOrNot, 'yes', 'no') AS blocked\n" +
                "FROM krusty.Pallets\n" +
                "LEFT JOIN krusty.Orders ON Pallets.PalletNbr = Orders.PalletNbr\n" +
                "LEFT JOIN Customer ON Orders.CustomerName = Customer.CustomerName";

        ArrayList<String> list = new ArrayList<>();
        String json = "";
        String from = req.queryParams("from");
        String cookie = req.queryParams("cookie");
        String to = req.queryParams("to");
        String blocked = req.queryParams("blocked");

        if (to != null || from != null || cookie != null || blocked != null) {
            sql += "WHERE ";
        }
        if (to != null) {
            if (list.size() > 0) {
                sql += "AND ";
                sql += "production_date <= ? ";
                list.add(to);
            }
        }
        if (from != null) {
            sql = "production_date >= ? ";
            list.add(from);
        }
        if (cookie != null) {
            if (list.size() > 0) {
                sql += "AND ";
                sql += "cookie = ? ";
                list.add(cookie);
            }
        }
        if (blocked != null) {
            if (list.size() > 0)
                sql += "AND ";
            sql += "blocked = ? ";
            list.add(blocked);
        }
       //  return executeQuery(sql, "pallets");
        //}
      try {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 0; i < list.size(); i++) {
                ps.setString(i + 1, list.get(i));
            }
            ResultSet rs = ps.executeQuery();
            return Jsonizer.toJson(rs, "pallets");
        } catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
            return  Jsonizer.anythingToJson("error", "status");
        }

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
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException exception) {
            return exception.getMessage();
        }
    }

    public String createPallet(Request req, Response res) throws SQLException {
        String CookieName = req.queryParams("cookie");
        return CookieName != null ? checkIfCookieExist(CookieName) : Jsonizer.anythingToJson("error", "status");
    }

    private String checkIfCookieExist(String CookieName) throws SQLException {
        return cookieExist(CookieName) ? createPallet(CookieName) : Jsonizer.anythingToJson("unknown cookie", "status");
    }

    private boolean cookieExist(String CookieName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT CookieName FROM krusty.Cookies WHERE CookieName = ?");
        preparedStatement.setString(1, CookieName);
        ResultSet rs = preparedStatement.executeQuery();
        return rs.next();
    }

    private String createPallet(String CookieName) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO krusty.Pallets(TimeProduced,CookieName) VALUES(NOW(), ?)"  , Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, CookieName);
        ps.executeUpdate();
        int palletId = 0;
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            palletId = generatedKeys.getInt(1);
        }
        ps.close();
        updateStorage(CookieName);
        return "{\"status\": \"ok\", \"id\": " + palletId + "}";
    }

    private void updateStorage(String CookieName) throws SQLException {
        Map<String, Integer> ingredientAmount = retrieveIngredients(CookieName);

        PreparedStatement prepareStatement = connection.prepareStatement(" UPDATE krusty.Storage SET StockAmount = StockAmount - 54*? WHERE IngredientName = ?");
        for (Map.Entry<String, Integer> entry : ingredientAmount.entrySet()) {
            prepareStatement.setInt(1, entry.getValue());
            prepareStatement.setString(2, entry.getKey());
            prepareStatement.executeUpdate();
        }

    }

    private Map<String, Integer> retrieveIngredients(String CookieName) throws SQLException {
        Map<String, Integer> ingredientAmount = new HashMap<>();
        PreparedStatement ps = connection.prepareStatement("SELECT IngredientName, Amount FROM krusty.Recipe WHERE CookieName = ?");
        ps.setString(1, CookieName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ingredientAmount.put(rs.getString("IngredientName"), rs.getInt("Amount"));
        }
        return ingredientAmount;
    }
}
