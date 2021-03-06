package krusty;

import spark.Request;
import spark.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

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
        final String sql = "SELECT CustomerName AS name, CustomerAddress as address FROM " + database + ".Customer";
        return executeQuery(sql, "customers");
    }

    public String getRawMaterials(Request req, Response res) {
        final String sql = "SELECT IngredientName AS name, StockAmount AS amount, Unit AS unit FROM " + database + ".Storage";
        return executeQuery(sql, "raw-materials");
    }

    public String getCookies(Request req, Response res) {
        final String sql = "SELECT CookieName AS name FROM " + database + ".cookies";
        return executeQuery(sql, "cookies");
    }

    public String getRecipes(Request req, Response res) {
        final String sql = "SELECT CookieName AS cookie, Recipe.IngredientName AS raw_material, Amount AS amount, Storage.Unit AS unit\n"
                + "FROM " + database + ".Recipe\n"
                + "INNER JOIN " + database + ".Storage ON Recipe.IngredientName = Storage.IngredientName";
        return executeQuery(sql, "recipes");
    }

    public String getPallets(Request req, Response res) {

        StringBuilder sql = new StringBuilder(
                "SELECT Pallets.PalletNbr AS id, Pallets.CookieName AS cookie, Pallets.TimeProduced AS production_date, Orders.CustomerName AS customer,  IF(BlockedOrNot, 'yes', 'no') AS blocked\n"
                        + "FROM " + database + ".Pallets\n"
                        + "LEFT JOIN " + database + ".Orders ON Orders.PalletNbr = Pallets.PalletNbr\n"
        );

        Map<String, String> conditions = retrieveConditions(req);

        if (!conditions.isEmpty()) {
            addConditionsToQuery(sql, conditions);
        }

        return executeQuery(sql.toString(), "pallets");
    }

    private void addConditionsToQuery(StringBuilder sql, Map<String, String> conditions) {
        StringJoiner conditionJoiner = new StringJoiner(" AND ");
        conditions.forEach((key, value) -> conditionJoiner.add(key + value));
        sql.append("WHERE ").append(conditionJoiner).append(";");
    }

    private Map<String, String> retrieveConditions(Request req) {
        Map<String, String> conditions = new HashMap<>();

        Map.of("cookie", "Pallets.CookieName = ", "from", "Pallets.TimeProduced >= ", "to", "Pallets.TimeProduced <= ", "blocked", "BlockedOrNot IS ").forEach((key, query) -> {
            var param = req.queryParams(key);

            if (null != param) {
                param = ("BlockedOrNot IS ".equals(query)) ? ("yes".equals(param) ? "TRUE" : "NOT TRUE")
                        : ("'" + param + "'");

                conditions.put(query, param);
            }
        });

        return conditions;
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

    public String reset(Request req, Response res) throws SQLException {
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
            connection.createStatement().executeUpdate("TRUNCATE TABLE " + database + "." + table + ";");
        }
    }

    private void insertDataIntoTables() throws SQLException {
        for (String table : Arrays.asList("Customer", "Cookies", "Storage", "Recipe")) {
            connection.createStatement().execute(readFile("reset-" + table + ".sql"));
        }
    }

    private String readFile(String file) {
        try {

            String path = /*"krusty-skeleton/" + */  file;   /*??ndra path beroende p?? var du l??gger reset.sql filerna */
            return new String(Files.readAllBytes(Paths.get(path)));
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
        PreparedStatement ps = connection.prepareStatement("SELECT CookieName FROM " + database + ".Cookies WHERE CookieName = ?");
        ps.setString(1, CookieName);
        ResultSet rs = ps.executeQuery();

        return rs.next();
    }

    private String createPallet(String CookieName) throws SQLException {

        int palletId = 0;

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO " + database + ".Pallets(TimeProduced,CookieName) VALUES(NOW(), ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, CookieName);
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                palletId = generatedKeys.getInt(1);
            }
        }

        updateStorage(CookieName);

        return "{\"status\": \"ok\", \"id\": " + palletId + "}";
    }

    private void updateStorage(String CookieName) throws SQLException {
        Map<String, Integer> ingredientAmount = retrieveIngredients(CookieName);

        try (PreparedStatement ps = connection.prepareStatement("UPDATE " + database + ".Storage SET StockAmount = StockAmount - 54*? WHERE IngredientName = ?")) {

            for (Map.Entry<String, Integer> entry : ingredientAmount.entrySet()) {
                ps.setInt(1, entry.getValue());
                ps.setString(2, entry.getKey());
                ps.executeUpdate();
            }
        }

    }

    private Map<String, Integer> retrieveIngredients(String CookieName) throws SQLException {
        Map<String, Integer> ingredientAmount = new HashMap<>();

        try (PreparedStatement ps = connection.prepareStatement("SELECT IngredientName, Amount FROM " + database + ".Recipe WHERE CookieName = ?")) {
            ps.setString(1, CookieName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredientAmount.put(rs.getString("IngredientName"), rs.getInt("Amount"));
            }

        }

        return ingredientAmount;
    }
}
