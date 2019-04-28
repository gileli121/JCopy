package system.common.clipboard;

import classes.ClipboardDataFormats;
import system.common.clipboard.exceptions.*;

public interface InterfaceClipboardHelper {

    // Should return null if no clip found
    ClipboardDataFormats getClipboardDataFormats() throws ClipboardGetDataFormatsException;

    void setClipboardDataFormats(ClipboardDataFormats clipboardDataFormats);

    void clearClipboard() throws ClearClipboardException;

}
