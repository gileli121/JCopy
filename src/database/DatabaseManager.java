package database;

import database.classes.ClipInfoList;
import database.helpers.DbConnector;

import java.sql.SQLException;

public class DatabaseManager extends DbConnector {

    private ClipInfoList clipInfos;

    public DatabaseManager() throws SQLException, ClassNotFoundException {
        initializeDb();
        clipInfos = new ClipInfoList();
    }

    public ClipInfoList getClipInfos() {
        return clipInfos;
    }


}
