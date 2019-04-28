package settings;

import events.ErrorEvents;
import settings.enums.SaveCopyMode;

import java.io.*;
import java.util.Properties;

public class Settings {

    private static final String KEY_AUTOSAVE_NEWCLIP = "autoSaveClips";
    private static final String KEY_UI_SAVE_CLIP_MODE = "uiSaveToDb";
    private static final String VALUE_UI_SAVE_CLIP_MODE_NONE = "none";
    private static final String VALUE_UI_SAVE_CLIP_MODE_SAVE = "save";
    private static final String VALUE_UI_SAVE_CLIP_MODE_DBSAVE = "dbSave";
    private static final String VALUE_UI_RECORD_CLIPS = "recordClips";

    private static File settingsFile;
    private static Properties settings;

    public static void initialize() {
        settingsFile = new File("settings");
        settings = new Properties();
        loadSettings();
    }


    public static void loadSettings(){

        InputStream settingsFileInStream;
        try {
            settingsFileInStream = new FileInputStream(settingsFile);
        } catch (FileNotFoundException e) {
            ErrorEvents.onErrorEvent(new Exception("The settings file was not found", e));
            return;
        }

        try {
            settings.load(settingsFileInStream);
        } catch (IOException e) {
            ErrorEvents.onErrorEvent(new Exception("Failed to load settings",e));
            return;
        }
    }

    public static void writeSettings() {
        OutputStream settingsFileOutStream = null;
        try {
            settingsFileOutStream = new FileOutputStream(settingsFile);
        } catch (FileNotFoundException e) {
            ErrorEvents.onErrorEvent(new Exception("The settings file was not found", e));
            return;
        }
        try {
            settings.store(settingsFileOutStream,null);
        } catch (IOException e) {
            ErrorEvents.onErrorEvent(new Exception("Failed to save settings",e));
            return;
        }

        try {
            settingsFileOutStream.close();
        } catch (IOException e) {
            ErrorEvents.onErrorEvent(new Exception("Failed to close the settings file",e));
        }
    }

    public static boolean getAutoSaveDb() {
        return settings.getProperty(KEY_AUTOSAVE_NEWCLIP,"1").equals("1");
    }
    public static void setAutoSaveDb(boolean autoSaveDb) {
        settings.setProperty(KEY_AUTOSAVE_NEWCLIP,autoSaveDb ? "1" : "0");
        writeSettings();
    }

    public static SaveCopyMode getUiSaveCopyMode() {

        switch (settings.getProperty(KEY_UI_SAVE_CLIP_MODE,VALUE_UI_SAVE_CLIP_MODE_SAVE)) {
            case VALUE_UI_SAVE_CLIP_MODE_SAVE:
                return SaveCopyMode.SAVE_ONLY;
            case VALUE_UI_SAVE_CLIP_MODE_DBSAVE:
                return SaveCopyMode.SAVE_AND_DATABASE;
            case VALUE_UI_SAVE_CLIP_MODE_NONE:
                default:
                    return SaveCopyMode.NONE;
        }
    }
    public static void setUiSaveCopyMode(SaveCopyMode saveCopyMode) {
        switch (saveCopyMode) {
            case NONE:
                settings.setProperty(KEY_UI_SAVE_CLIP_MODE,VALUE_UI_SAVE_CLIP_MODE_NONE);
                break;
            case SAVE_ONLY:
                settings.setProperty(KEY_UI_SAVE_CLIP_MODE, VALUE_UI_SAVE_CLIP_MODE_SAVE);
                break;
            case SAVE_AND_DATABASE:
                settings.setProperty(KEY_UI_SAVE_CLIP_MODE, VALUE_UI_SAVE_CLIP_MODE_DBSAVE);
                break;
        }
        writeSettings();
    }

    public static boolean getClipRecord() {

        return settings.getProperty(VALUE_UI_RECORD_CLIPS,"1").equals("1");
    }
    public static void setClipRecord(boolean clipRecord) {
        settings.setProperty(VALUE_UI_RECORD_CLIPS,clipRecord ? "1" : "0");
        writeSettings();
    }

}
