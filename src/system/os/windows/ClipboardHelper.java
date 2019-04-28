package system.os.windows;

import classes.ClipboardDataFormats;
import common.CommonHelpers;
import events.ErrorEvents;
import javafx.scene.input.ClipboardContent;
import system.common.clipboard.ClipboardHelperBase;
import system.common.clipboard.InterfaceClipboardHelper;
import javafx.scene.input.Clipboard;

public class ClipboardHelper extends ClipboardHelperBase implements InterfaceClipboardHelper {

    private Clipboard clipboard;

    public ClipboardHelper() {
        initialize();
    }

    private void initialize() {

    }


    @Override
    public ClipboardDataFormats getClipboardDataFormats() {

        clipboard = Clipboard.getSystemClipboard();

        String rtf = null;
        String html = clipboard.getHtml();
        if (html == null || html.isEmpty() || CommonHelpers.isHtmlEmpty(html)) {
            html = null;
            rtf = clipboard.getRtf();
            if (rtf != null && rtf.isEmpty()) rtf = null;
        }

        String url = clipboard.getUrl();
        if (url != null && url.isEmpty()) url = null;

        String plainText = clipboard.getString();
        if (plainText == null || plainText.isEmpty()) {
            if (url != null)
                plainText = url;
            else if (html != null) {
                plainText = CommonHelpers.getTextFromHtml(html);
                if (CommonHelpers.removeNewLines(plainText).isEmpty()) return null;
            } else {
                //ErrorEvents.onErrorEvent(new Exception("Failed to get plain text for the new clip"));
                return null;
            }
        }
        return new ClipboardDataFormats(plainText,html,rtf,url,null);
    }

    @Override
    public void setClipboardDataFormats(ClipboardDataFormats clipboardDataFormats) {

        if (clipboardDataFormats == null) {
            //ErrorEvents.onErrorEvent(new Exception("Null Error"));
            return;
        }

        clipboard = Clipboard.getSystemClipboard();

        ClipboardContent content = new ClipboardContent();

        clipboard.clear();

        if (clipboardDataFormats.getPlainText() != null)
            content.putString(clipboardDataFormats.getPlainText());

        if (clipboardDataFormats.getHtml() != null)
            content.putHtml(clipboardDataFormats.getHtml());

        if (clipboardDataFormats.getRtf() != null)
            content.putRtf(clipboardDataFormats.getRtf());

        if (clipboardDataFormats.getUrl() != null)
            content.putUrl(clipboardDataFormats.getUrl());

        if (clipboardDataFormats.getImage() != null)
            content.putImage(clipboardDataFormats.getImage());

        clipboard.setContent(content);

    }


    @Override
    public void clearClipboard()
    {
        clipboard = Clipboard.getSystemClipboard();
        clipboard.clear();
    }

}
