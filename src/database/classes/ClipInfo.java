package database.classes;

import classes.ClipboardDataFormats;
import database.helpers.DbConnector;
import events.ErrorEvents;
import settings.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClipInfo {

    public ClipboardDataFormats clipboardDataFormats = null;
    public ClipboardDataFormats decryptedDataFormats = null;
    private String decryptPassword = null;
    private long timestamp = 0;
    private List<String> clipTags = null;
    private String clipDisplayName = null;
    private int clipId = -1;
    private boolean saveToDb = true;
    private int dbClipId = -1;



    public ClipInfo() {
        clipboardDataFormats = new ClipboardDataFormats();
    }

    public ClipInfo(ClipInfo clipInfo) {
        this(clipInfo.clipboardDataFormats,clipInfo.clipId,clipInfo.saveToDb,clipInfo.dbClipId,clipInfo.clipDisplayName,clipInfo.clipTags);
    }

    public ClipInfo(ClipboardDataFormats clipboardDataFormats) {
        this(clipboardDataFormats,-1,Settings.getAutoSaveDb(),-1,null,null);
    }


    public ClipInfo(ClipboardDataFormats clipboardDataFormats, int clipId,boolean saveToDb, int dbClipId, String clipDisplayName, List<String> clipTags) {
        this.clipboardDataFormats = clipboardDataFormats;
        this.clipId = clipId;
        this.saveToDb = saveToDb;
        this.dbClipId = dbClipId;
        this.clipDisplayName = clipDisplayName;
        this.clipTags = clipTags;
        this.timestamp = (new Timestamp(System.currentTimeMillis())).getTime();
    }

    // Getters
    public boolean getSaveToDb() {return saveToDb;}
    public String getClipDisplayName() {return clipDisplayName;}
    public String getTagsAsString() {return getTagsAsString(clipTags);}
    public String getTagsAsString(List<String> clipTags) {
        if (clipTags == null || clipTags.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < clipTags.size()-1; i++)
            sb.append(clipTags.get(i)).append(",");
        sb.append(clipTags.get(i));
        return sb.toString();
    }
    public List<String> getClipTags() {return clipTags; }
    public long getTimestamp() {return timestamp;}
    public int getClipId() {return clipId;}
    public int getDbClipId() {return dbClipId;}
    public boolean getIsEncrypted() {return clipboardDataFormats.getIsEncrypted();}
    public String getDecryptPassword() {
        return decryptPassword;
    }

    //Setters
    public void setClipId(int clipId) {this.clipId = clipId; }
    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
    public void setClipTags(List<String> clipTags) {this.clipTags = clipTags;}
    public void setClipDisplayName(String clipDisplayName) {
        if (saveToDb && dbClipId != -1 && !sqlSaveDisplayName(clipDisplayName)) return;
        this.clipDisplayName = clipDisplayName;
    }
    public void setDbClipId(int dbClipId) {this.dbClipId = dbClipId;}
    public void setSaveToDb(boolean saveToDb) {
        this.saveToDb = saveToDb;
    }
    public void setEncryption(String password) {
        clipboardDataFormats.setEncryption(password);
        if (saveToDb) sqlUpdateClipboardDataFormats();
    }
    public void setDecryptPassword(String decryptPassword) {
        this.decryptPassword = decryptPassword;
    }

    public void removeEncryption(String password) {
        clipboardDataFormats.removeEncryption(password);
        if (saveToDb) sqlUpdateClipboardDataFormats();
    }


    // Database add
    public boolean sqlAdd() {
        try {

            PreparedStatement preparedStatement = DbConnector.getConnection().prepareStatement(
                    "INSERT INTO CLIPS (TIME_STAMP,IS_ENCRYPTED,TAGS,CLIP_DISPLAY,PLAIN_TEXT,HTML,RTF,URL) VALUES (?,?,?,?,?,?,?,?)"
            );

            preparedStatement.setLong(1,timestamp);
            preparedStatement.setBoolean(2,getIsEncrypted());
            preparedStatement.setString(3, getTagsAsString());
            preparedStatement.setString(4,clipDisplayName);
            preparedStatement.setString(5,clipboardDataFormats.getPlainText());
            preparedStatement.setString(6,clipboardDataFormats.getHtml());
            preparedStatement.setString(7,clipboardDataFormats.getRtf());
            preparedStatement.setString(8,clipboardDataFormats.getUrl());

            if (preparedStatement.executeUpdate() >= 0) {
                try {
                    ResultSet resultSet = DbConnector.executeQuery("SELECT last_insert_rowid() from CLIPS");
                    if (resultSet != null) {
                        dbClipId = resultSet.getInt(1);
                        return true;
                    }
                } catch (SQLException e) {
                    ErrorEvents.onErrorEvent(e);
                } finally {
                    DbConnector.finishExecute();
                }
            }
            preparedStatement.close();
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
        }
        return false;
    }


    public boolean sqlRemove() {
        if (DbConnector.executeUpdate("DELETE FROM CLIPS WHERE id = " + dbClipId) <= 0) {
            DbConnector.finishExecute();
            ErrorEvents.onErrorEvent(new Exception("Failed to delete the selected clip from database"));
            return false;
        }
        dbClipId = -1;
        return true;
    }


    public boolean sqlSaveTags() {return sqlSaveTags(clipTags);}
    public boolean sqlSaveTags(List<String> tagList) { return sqlUpdateKey("TAGS",getTagsAsString(tagList));}
    public boolean sqlSaveDisplayName(String clipDisplayName) { return sqlUpdateKey("CLIP_DISPLAY",clipDisplayName);}
    public void sqlUpdateClipboardDataFormats() {
        sqlUpdateKey("IS_ENCRYPTED",clipboardDataFormats.getIsEncrypted() ? 1 : 0);
        sqlUpdateKey("PLAIN_TEXT", clipboardDataFormats.getPlainText());
        sqlUpdateKey("HTML", clipboardDataFormats.getHtml());
        sqlUpdateKey("RTF", clipboardDataFormats.getRtf());
        sqlUpdateKey("URL", clipboardDataFormats.getUrl());

    }

    public boolean sqlUpdateKey(String key, Object value) {

        PreparedStatement preparedStatement;
        try {
            preparedStatement = DbConnector.getConnection().prepareStatement(
                    "UPDATE CLIPS SET "+key+"=? WHERE ID=?"
            );
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
            return false;
        }

        try {
            preparedStatement.setObject(1,value);
            preparedStatement.setInt(2, dbClipId);

            if (preparedStatement.executeUpdate() < 0) {
                preparedStatement.close();
                return false;
            }

            preparedStatement.close();
            return true;
        } catch (SQLException e) {
            ErrorEvents.onErrorEvent(e);
        }

        return false;
    }



    // Other
    public void updateTimestamp() {
        timestamp = (new Timestamp(System.currentTimeMillis())).getTime();
    }


    public void addTag(String tag) {
        if (clipTags == null) clipTags = new ArrayList<>();
        if (clipTags.contains(tag)) return;
        if (saveToDb && dbClipId != -1) {
            List<String> newTagList = new ArrayList<>(clipTags);
            newTagList.add(tag);
            if (!sqlSaveTags(newTagList)) return;
        }
        clipTags.add(tag);
    }

    public void removeTag(String tag) {
        if (clipTags == null) return;
        if (!clipTags.contains(tag)) return;
        if (saveToDb && dbClipId != -1) {
            List<String> newTagList = new ArrayList<>(clipTags);
            newTagList.remove(tag);
            if (!sqlSaveTags(newTagList)) return;
        }
        clipTags.remove(tag);
    }

    public boolean equals(ClipInfo clipInfo) {
        return clipId == clipInfo.clipId;
    }

    public boolean updateHtmlText(String newHtmlText) {
        if (clipboardDataFormats.getRtf() != null) {
            clipboardDataFormats.setRtf(null);
            if (saveToDb) sqlUpdateKey("RTF",null);

        }
        if (clipboardDataFormats.getUrl() != null) {
            clipboardDataFormats.setUrl(null);
            if (saveToDb) sqlUpdateKey("URL",null);
        }

        clipboardDataFormats.setHtml(newHtmlText);
        if (saveToDb) sqlUpdateKey("HTML",clipboardDataFormats.getHtml());

        return true;
    }

    public boolean updatePlainText(String newPlainText) {
        clipboardDataFormats.setPlainText(newPlainText);
        if (saveToDb) sqlUpdateKey("PLAIN_TEXT",newPlainText);
        return true;
    }

}
