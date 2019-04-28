package database.helpers;

import events.ErrorEvents;
import org.sqlite.SQLiteJDBCLoader;

import java.sql.*;

public class DbConnector {
    public static Connection connection;

    private static Statement statement = null;

    private static boolean isInitialized = false;

    public static void initializeDb() throws ClassNotFoundException, SQLException {
        if (isInitialized) return;
        try {
            SQLiteJDBCLoader.initialize();
        } catch (Exception e) {
            ErrorEvents.onErrorEvent(new Exception("Failed to initialize with SQLiteJDBCLoader",e));
            return;
        }
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:clipsdata.db");
        isInitialized = true;
    }

    // For getting the data from database
    public static ResultSet executeQuery(String query) {
        try {
            statement = connection.createStatement();
            //if (finishExecute) finishExecute();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
        }
        return null;
    }



    // For insert,update,delete
    public static int executeUpdate(String query) {
        try {
            statement = connection.createStatement();
            return statement.executeUpdate(query);
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
        }

        return -1;
    }



    public static boolean execute(String query) {
        try {
            statement = connection.createStatement();
            return statement.execute(query);
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
        }

        return false;
    }


    public static void finishExecute() {
        if (statement == null) return;
        try {
            statement.close();
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
        } finally {
            statement = null;
        }
    }


    public static Connection getConnection() {
        return connection;
    }
}
