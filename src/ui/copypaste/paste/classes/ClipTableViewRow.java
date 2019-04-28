package ui.copypaste.paste.classes;

import classes.ClipboardDataFormats;
import database.classes.ClipInfo;
import events.ErrorEvents;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import ui.copypaste.paste.exceptions.ClipTableViewRowException;
import ui.copypaste.paste.exceptions.GetClipTextException;

import java.util.Date;

public class ClipTableViewRow {

    private String clipText;

    private String clipTags;

    private String clipDate;

    private SimpleBooleanProperty saveDbChecked;

    public ClipInfo clipInfo;

    private static final String LS = System.lineSeparator();

    public ClipTableViewRow(ClipInfo clipInfo) throws ClipTableViewRowException {

        this.clipInfo = clipInfo;

        updateClipText();
        updateClipInfo();
        updateClipDate();

        saveDbChecked = new SimpleBooleanProperty(clipInfo.getSaveToDb());
    }

    public boolean updateClipText() {
        if (clipInfo.getClipDisplayName() != null)
            clipText = clipInfo.getClipDisplayName();
        else {

            ClipboardDataFormats clipboardDataFormats = clipInfo.clipboardDataFormats;
            if (clipboardDataFormats.getIsEncrypted()) {
                clipboardDataFormats = clipInfo.decryptedDataFormats;
                if (clipboardDataFormats == null) {
                    clipText = "";
                    return true;
                }
            }

            if (clipboardDataFormats.getImage() != null) {
                Image image = clipboardDataFormats.getImage();
                clipText = "[Image (" + (int) image.getWidth() + "x" + (int) image.getHeight() + ") ]";
            } else if (clipboardDataFormats.getPlainText() != null)
                clipText = clipboardDataFormats.getPlainText();
            else if (clipboardDataFormats.getHtml() != null)
                clipText = clipboardDataFormats.getHtml();
            else if (clipboardDataFormats.getRtf() != null)
                clipText = clipboardDataFormats.getRtf();
            else if (clipboardDataFormats.getUrl() != null)
                clipText = clipboardDataFormats.getUrl();
            else {
                ErrorEvents.onErrorEvent(new GetClipTextException());
                return false;
            }

            clipText = clipText.replace(LS, " ");
        }
        return true;
    }

    public void updateClipInfo() {
        if (clipInfo.getClipTags() != null) {

            //clipTags = String.join(",",clipInfo.clipTags); TODO
        } else
            clipTags = "";
    }

    public void updateClipDate() {
        if (clipInfo.getTimestamp() != 0) {
            Date date = new Date(clipInfo.getTimestamp());
            clipDate = date.toString();
        } else
            clipDate = "";
    }

    public String getClipText() {
        return clipText;
    }

    public String getClipTags() {
        return clipTags;
    }

    public String getClipDate() {
        return clipDate;
    }

    public SimpleBooleanProperty saveDbCheckedProperty() {
        return saveDbChecked;
    }

    public boolean getSaveDbChecked() {
        return saveDbCheckedProperty().get();
    }

    public void setClipDate(String clipDate) {
        this.clipDate = clipDate;
    }

    public void setClipTags(String clipTags) {
        this.clipTags = clipTags;
    }

    public void setClipText(String clipText) {
        this.clipText = clipText;
    }

    public void setSaveDbChecked(final boolean saveDbChecked) {
        clipInfo.setSaveToDb(saveDbChecked);
        this.saveDbCheckedProperty().set(saveDbChecked);
    }
}
