package database.classes;

import classes.ClipboardDataFormats;
import database.helpers.DbConnector;
import events.ErrorEvents;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClipInfoList extends ArrayList<ClipInfo> {

    private List<String> clipTags;

    public ClipInfoList() throws SQLException {
        clipTags = new ArrayList<>();
        createSqlTableIfNotExist();
        loadListFromDatabase();
    }


    // Getters
    public List<String> getClipTags() {return clipTags;}


    // overrides
    @Override
    public boolean add(ClipInfo clipInfo) {
        createSqlTableIfNotExist();
        addClipTagsIfNotExist(clipInfo.getClipTags());
        clipInfo.setClipId(getUniqueId());
        if (clipInfo.getSaveToDb() && !clipInfo.sqlAdd()) return false;
        return super.add(clipInfo);
    }

    private int getUniqueId() {
        if (size() == 0) return 1;
        int id = 2;
        boolean found;
        do {
            found = true;
            for (ClipInfo clipInfo : this) {
                if (clipInfo.getClipId() != id) continue;
                found = false;
                id++;
                break;
            }
        } while (!found);
        return id;
    }

    @Override
    public ClipInfo remove(int index) {
        ClipInfo clipInfo = get(index);
        if (clipInfo.getDbClipId() != -1) clipInfo.sqlRemove();
        return super.remove(index);
    }

    // Other
    private void loadListFromDatabase() throws SQLException {
        ResultSet rs = DbConnector.executeQuery("SELECT * FROM CLIPS");
        if (rs == null) return;


        while (rs.next()) {

            List<String> clipTags = null;

            boolean isEncrypted = rs.getInt("IS_ENCRYPTED") == 1;

            String clipTagsStr = rs.getString("TAGS");
            if (clipTagsStr != null && !clipTagsStr.isEmpty())
                clipTags = new ArrayList<>(Arrays.asList(clipTagsStr.split("\\s*,\\s*")));

            addClipTagsIfNotExist(clipTags);


            String clipPlainText = rs.getString("PLAIN_TEXT");
            if (clipPlainText != null && clipPlainText.isEmpty()) clipPlainText = null;

            String clipHtml = rs.getString("HTML");
            if (clipHtml != null && clipHtml.isEmpty()) clipHtml = null;

            String clipRtf = rs.getString("RTF");
            if (clipRtf != null && clipRtf.isEmpty()) clipRtf = null;

            String clipUrl = rs.getString("URL");
            if (clipUrl != null && clipUrl.isEmpty()) clipUrl = null;

            String clipDisplay = rs.getString("CLIP_DISPLAY");
            if (clipDisplay != null && clipDisplay.isEmpty()) clipDisplay = null;

            super.add(new ClipInfo(
                    new ClipboardDataFormats(isEncrypted, clipPlainText, clipHtml, clipRtf,clipUrl,null),
                    size()+1,true,rs.getInt("ID"),clipDisplay,clipTags));

        }
    }

    private void addClipTagsIfNotExist(List<String> tagList) {
        if (tagList == null) return;
        for (String tag : tagList) {
            if (!clipTags.contains(tag))
                clipTags.add(tag);
        }
    }

    private void createSqlTableIfNotExist() {
        DbConnector.executeUpdate("CREATE TABLE IF NOT EXISTS CLIPS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TIME_STAMP INTEGER NOT NULL," +
                "IS_ENCRYPTED INTEGER DEFAULT 0," +
                "TAGS TEXT," +
                "CLIP_DISPLAY TEXT," +
                "PLAIN_TEXT TEXT," +
                "HTML TEXT," +
                "RTF TEXT," +
                "URL TEXT," +
                "IMAGE TEXT" +
                ");");

        DbConnector.finishExecute();
    }

    public boolean isClipExist(ClipboardDataFormats clipboardDataFormats) {
        for (ClipInfo clipInfo : this) {
            if (clipInfo.clipboardDataFormats.getPlainText().equals(clipboardDataFormats.getPlainText()))
                return true;
        }
        return false;
    }

    public int findIndex(ClipInfo clipInfo) {return findIndex(clipInfo,true);}
    public int findIndex(ClipInfo clipInfo, boolean dir) {
        if (dir) {
            for (int i = 0; i < size(); i++) {
                if (clipInfo.getClipId() == get(i).getClipId()) return i;
            }
        } else {
            for (int i = size()-1; i >= 0; i--) {
                if (clipInfo.getClipId() == get(i).getClipId()) return i;
            }
        }
        return -1;
    }

    public int findIndex(ClipboardDataFormats clipboardDataFormats) {
        return findIndex(clipboardDataFormats, true);
    }
    public int findIndex(ClipboardDataFormats clipboardDataFormats, boolean dir) {
        if (clipboardDataFormats.getPlainText() == null) {
            ErrorEvents.onErrorEvent(new Exception("Found invalid copy"));
            return -1;
        }
        if (dir) {
            for (int i = 0; i < size(); i++) {
                if (get(i).clipboardDataFormats.getPlainText() == null) {
                    ErrorEvents.onErrorEvent(new Exception("Found invalid copy"));
                    continue;
                }
                if (get(i).clipboardDataFormats.getPlainText().equals(clipboardDataFormats.getPlainText()))
                    return i;
            }
        } else {
            for (int i = size()-1; i >= 0; i--) {
                if (get(i).clipboardDataFormats.getPlainText() == null) {
                    ErrorEvents.onErrorEvent(new Exception("Found invalid copy"));
                    continue;
                }
                if (get(i).clipboardDataFormats.getPlainText().equals(clipboardDataFormats.getPlainText()))
                    return i;
            }
        }
        return -1;
    }


    public void moveClipToLast(int index) {
        ClipInfo clipInfo = new ClipInfo(get(index));
        remove(index);
        clipInfo.updateTimestamp();
        add(clipInfo);
    }

}
