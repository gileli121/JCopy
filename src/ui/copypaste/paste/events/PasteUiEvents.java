package ui.copypaste.paste.events;

import database.classes.ClipInfo;

public interface PasteUiEvents {

    void onPasteUserRequest(ClipInfo clipInfo, boolean pasteAsPlain);

    void onPasteUiClose();
}
