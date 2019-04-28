package ui.copypaste.copy.events;

import database.classes.ClipInfo;

public interface CopyUIEvents {
    void onCopyUISave(ClipInfo clipInfo);
    void onCopyUIClose();

}
