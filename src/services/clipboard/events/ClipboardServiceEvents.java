package services.clipboard.events;

import classes.ClipboardDataFormats;

public interface ClipboardServiceEvents {
    void onNewCopy(ClipboardDataFormats clipboardDataFormats);

    void onNewCopyLong(ClipboardDataFormats clipboardDataFormats);

    void onNewPasteLong();

}
