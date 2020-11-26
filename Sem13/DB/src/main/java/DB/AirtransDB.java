package DB;

import DB.Tables.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AirtransDB {
    private final static String CSV_PATH = "./csv_data/";
    private final static String QUERIES_PATH = "./src/main/sqlQueries/";
    private final static String DATA_SOURCE = "https://storage.yandexcloud.net/airtrans-small/";
    private static ArrayList<BaseTable> tables;

    public static String getQueriesPath(){
        return QUERIES_PATH;
    }

    static {
        try {
            tables = new ArrayList<>(Arrays.asList(new Aircrafts(), new Airports(), new Boarding_passes(),
                    new Bookings(), new Flights(), new Seats(), new Ticket_flights(), new Tickets()));

            downloadCsv();
            dropAllTables();
            createTables();
            loadTablesFromCsv();
            addConstraints(); // loads faster without constraints
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            AirtransDBConnection.closeConnection();
        }
    }

    public static void dropAllTables() {
        AirtransDBConnection.executeSqlUpdateQueryFromFile(new File(QUERIES_PATH + "DropAllTables.sql"));
    }

    public static void createTables() {
        for (BaseTable table : tables) {
            table.createTable();
        }
    }

    private static void addConstraints() {
        AirtransDBConnection.executeSqlUpdateQueryFromFile(new File(QUERIES_PATH + "AddConstraints.sql"));
    }

    private static void loadTablesFromCsv() {
        for (BaseTable table : tables) {
            table.insertDataFromCsv(CSV_PATH + table.getTableName() + ".csv");
        }
    }

    private static void downloadFile(String file_url, String placeToWrite, String fileName) throws IOException {
        URL url = new URL(file_url);
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        Path path = new File(placeToWrite + fileName).toPath();
        Files.copy(inputStream, path);
    }

    public static void downloadCsv() throws IOException {
        File csvFolder = new File(CSV_PATH);
        if (csvFolder.exists()) {
            deleteFile(csvFolder);
        }
        assert csvFolder.mkdir();
        for (BaseTable table : tables) {
            String tableName = table.getTableName();
            String url = String.format(DATA_SOURCE + "%s.csv", tableName);
            downloadFile(url, CSV_PATH, tableName + ".csv");
        }
    }

    public static boolean deleteFile(File fileToDelete) {
        File[] allContents = fileToDelete.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteFile(file);
            }
        }
        return fileToDelete.delete();
    }

    public static ResultSet executeSelectQuery(String sqlFileName) {
        return AirtransDBConnection.executeSqlSelectQueryFromFile(new File(QUERIES_PATH + sqlFileName));
    }

    public static String getCityFromJson(String jsonStr) {
        jsonStr = jsonStr.substring(1, jsonStr.length() - 1).replaceAll("\\\\", "");
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonElement = jsonParser.parse(jsonStr).getAsJsonObject();
        return jsonElement.get("ru").toString().replaceAll("\"", "");
    }
}