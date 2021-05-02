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
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException exception) {
            return exception.getMessage();
        }
    }

    public String createPallet(Request req, Response res) throws SQLException {
        if (req.queryParams("cookie") != null) {
            String cookie = req.queryParams("cookie");
            return createPallet(cookie);
        } else{
            return null;
        }
    }
    protected String createPallet(String cookie) throws SQLException {
        String sql2 = "SELECT CookieName AS Cookie FROM krusty.Pallets";
        String sql = "INSERT INTO krusty.Pallets(CookieName) VALUES(?)";
        PreparedStatement st = connection.prepareStatement(sql);
        st.setString(1,cookie);
        st.executeUpdate();
        updateFlourAmount(cookie);
        updateButterAmount(cookie);
        updateIcingSugar(cookie);
        updateRoastedChoppedNuts(cookie);
        updateFineGroundNuts(cookie);
        updateGroundRoastedNuts(cookie);
        updateBreadCrumbs(cookie);
        updateSugarAmount(cookie);
        updateEggWhitesAmount(cookie);
        updateChocolateAmount(cookie);
        updateMarzipanAmount(cookie);
        updateEggsAmount(cookie);
        updatePotatoStarchAmount(cookie);
        updateWheatFlourAmount(cookie);
        updateSodiumBicarbonateAmount(cookie);
        updateVanillaAmount(cookie);
        updateChoppedAlmonds(cookie);
        updateCinnamonAmount(cookie);
        updateVanillaSugarAmount(cookie);
        return executeQuery(sql2,"pallets");
    }

    private void updateVanillaSugarAmount(String cookie) throws SQLException {
        if(cookie.equals("Berliner")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 5*54 \n" +
                    "WHERE IngredientName = 'Vanilla sugar'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateCinnamonAmount(String cookie) throws SQLException {
        if(cookie.equals("Almond delight")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 10*54 \n" +
                    "WHERE IngredientName = 'Cinnamon'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateChoppedAlmonds(String cookie) throws SQLException {
        if(cookie.equals("Almond delight")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 279*54 \n" +
                    "WHERE IngredientName = 'Chopped almonds'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateVanillaAmount(String cookie) throws SQLException {
        if(cookie.equals("Tango")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 2*54 \n" +
                    "WHERE IngredientName = 'Vanilla'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateSodiumBicarbonateAmount(String cookie) throws SQLException {
        if(cookie.equals("Tango")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 4*54 \n" +
                    "WHERE IngredientName = 'Sodium bicarbonate'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateWheatFlourAmount(String cookie) throws SQLException {
        if(cookie.equals("Amneris")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 25*54 \n" +
                    "WHERE IngredientName = 'Wheat flour'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updatePotatoStarchAmount(String cookie) throws SQLException {
        if(cookie.equals("Amneris")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 25*54 \n" +
                    "WHERE IngredientName = 'Potato starch'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateEggsAmount(String cookie) throws SQLException {
        if(cookie.equals("Amneris")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 250*54 \n" +
                    "WHERE IngredientName = 'Eggs'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Berliner")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 50*54 \n" +
                    "WHERE IngredientName = 'Eggs'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateMarzipanAmount(String cookie) throws SQLException {
        if(cookie.equals("Amneris")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 750*54 \n" +
                    "WHERE IngredientName = 'Marzipan'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateChocolateAmount(String cookie) throws SQLException {
        if(cookie.equals("Nut cookie")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 50*54 \n" +
                    "WHERE IngredientName = 'Chocolate'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Berliner")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 50*54 \n" +
                    "WHERE IngredientName = 'Chocolate'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateEggWhitesAmount(String cookie) throws SQLException {
        if(cookie.equals("Nut cookie")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 3,5*54 \n" +
                    "WHERE IngredientName = 'Egg Whites'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateSugarAmount(String cookie) throws SQLException {
        if(cookie.equals("Nut cookie")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 375*54 \n" +
                    "WHERE IngredientName = 'Sugar'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Tango")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 250*54 \n" +
                    "WHERE IngredientName = 'Sugar'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Almond delight")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 270*54 \n" +
                    "WHERE IngredientName = 'Sugar'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateBreadCrumbs(String cookie) throws SQLException {
        if(cookie.equals("Nut cookie")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 125*54 \n" +
                    "WHERE IngredientName = 'Bread crumbs'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateGroundRoastedNuts(String cookie) throws SQLException {
        if(cookie.equals("Nut cookie")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 625*54 \n" +
                    "WHERE IngredientName = 'Ground, roasted nuts'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateFineGroundNuts(String cookie) throws SQLException {
        if(cookie.equals("Nut cookie")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 750*54 \n" +
                    "WHERE IngredientName = 'Fine-ground nuts'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateRoastedChoppedNuts(String cookie) throws SQLException {
        if(cookie.equals("Nut ring")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 225*54 \n" +
                    "WHERE IngredientName = 'Roasted, chopped nuts'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateIcingSugar(String cookie) throws SQLException {
        if(cookie.equals("Nut ring")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 190*54 \n" +
                    "WHERE IngredientName = 'Icing sugar'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Berliner")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 100*54 \n" +
                    "WHERE IngredientName = 'Icing sugar'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateButterAmount(String cookie) throws SQLException {
        if(cookie.equals("Nut ring")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 450*54 \n" +
                    "WHERE IngredientName = 'Butter'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        else if(cookie.equals("Amneris")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 250*54 \n" +
                    "WHERE IngredientName = 'Butter'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        else if(cookie.equals("Tango")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 200*54 \n" +
                    "WHERE IngredientName = 'Butter'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        else if(cookie.equals("Almond delight")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 400*54 \n" +
                    "WHERE IngredientName = 'Butter'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        else if(cookie.equals("Berliner")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 250*54 \n" +
                    "WHERE IngredientName = 'Butter'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }

    private void updateFlourAmount(String cookie) throws SQLException {
        if(cookie.equals("Nut ring")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 450*54 \n" +
                    "WHERE IngredientName = 'Flour'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Almond delight")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 400*54 \n" +
                    "WHERE IngredientName = 'Flour'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Tango")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 300*54 \n" +
                    "WHERE IngredientName = 'Flour'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
        if(cookie.equals("Berliner")){
            String sql = "UPDATE krusty.Storage\n" +
                    "SET StockAmount = StockAmount - 350*54 \n" +
                    "WHERE IngredientName = 'Flour'";
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        }
    }
}
