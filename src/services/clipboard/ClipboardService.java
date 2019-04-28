package services.clipboard;

import classes.ClipboardDataFormats;
import events.ErrorEvents;
import javafx.application.Platform;
import javafx.concurrent.Task;
import services.clipboard.events.ClipboardServiceEvents;
import services.clipboard.enums.PressKeyType;
import system.common.keyboard.classes.HotKeySettings;
import system.common.keyboard.events.HotkeyEvents;
import system.os.windows.ClipboardHelper;
import system.os.windows.KeyboardHelper;

import java.awt.event.KeyEvent;

import static com.sun.jna.platform.win32.WinUser.*;

public class ClipboardService extends Task<Void> {

    private static final int SLEEP_BETWEEN_CLIPBOARD_CHECKS = 10;

    // Thread
    private Thread thread = null;
    private boolean listen = true;

    // Helpers
    private ClipboardHelper clipboardHelper;
    private KeyboardHelper keyboardHelper;

    // Events
    private ClipboardServiceEvents clipboardServiceEvents;


    // Variables
    private ClipboardDataFormats clipboardDataFormats;
    private long hotKeyTimestamp = -1;


    // Flags
    private PressKeyType copyPressType = PressKeyType.NORMAL;
    private PressKeyType pastePressType = PressKeyType.NORMAL;
    boolean triggerPasteEvent = false;

    // Settings
    private long longHotkeyTime = 1000;
    private long blockKeysTimestamp = 0;


    private HotkeyEvents onKeyboardCopy = new HotkeyEvents() {
        @Override
        public boolean onHotkeyEvent(boolean keyDown, int vkCodePrimary, int vkCodeSecondly) {

            if (!listen) return false;

            if (keyDown) {
                if (hotKeyTimestamp == -1)
                    hotKeyTimestamp = System.currentTimeMillis();

                if (System.currentTimeMillis() - hotKeyTimestamp >= longHotkeyTime) {
                    copyPressType = PressKeyType.LONG;
                    hotKeyTimestamp = -1;
                    keyboardHelper.sendVkKeyCode(vkCodePrimary, vkCodeSecondly, false);
                    return false;
                } else {
                    //if (copyPressType == PressKeyType.LONG) System.out.println(66);
                    //copyPressType = PressKeyType.NONE;
                    return true;
                }


            } else {
                boolean ret;
                if (System.currentTimeMillis() - hotKeyTimestamp < longHotkeyTime) {
                    keyboardHelper.sendVkKeyCode(vkCodePrimary, vkCodeSecondly, false);
                    copyPressType = PressKeyType.SHORT;
                    ret = false;
                } else
                    ret = true;
                hotKeyTimestamp = -1;
                return ret;
            }
        }
    };


    private HotkeyEvents onKeyboardPaste = new HotkeyEvents() {
        @Override
        public boolean onHotkeyEvent(boolean keyDown, int vkCodePrimary, int vkCodeSecondly) {

            if (blockKeysTimestamp == 0) {
                if (!listen) return false;
            } else if (System.currentTimeMillis()-blockKeysTimestamp <= 1000) {
                return true;
            } else {
                blockKeysTimestamp = 0;
            }


            if (keyDown) {

                if (hotKeyTimestamp == -1)
                    hotKeyTimestamp = System.currentTimeMillis();

                if (System.currentTimeMillis() - hotKeyTimestamp >= longHotkeyTime) {
                    hotKeyTimestamp = -1;
                    clipboardHelper.clearClipboard();
                    copyPressType = PressKeyType.NORMAL;
                    clipboardServiceEvents.onNewPasteLong();
                    blockKeysTimestamp = System.currentTimeMillis();
                    clipboardHelper.setClipboardDataFormats(clipboardDataFormats);
                }
                return true;
            } else {
                boolean ret;
                if (System.currentTimeMillis() - hotKeyTimestamp < longHotkeyTime) {
                    keyboardHelper.sendVkKeyCode(vkCodePrimary, vkCodeSecondly, false);
                    ret = false;
                } else
                    ret = true;
                hotKeyTimestamp = -1;
                return ret;
            }
        }
    };


    public ClipboardService(Object parent) {
        this.clipboardServiceEvents = (ClipboardServiceEvents) parent;
        initialize();
    }

    private void initialize() {

        clipboardHelper = new ClipboardHelper();
        clipboardDataFormats = clipboardHelper.getClipboardDataFormats();
        initializeHotkeys();

    }

    public void initializeHotkeys() {
        keyboardHelper = new KeyboardHelper();

        keyboardHelper.setHotKey(
                new HotKeySettings(
                        system.common.keyboard.enums.PressKeyType.SHORT_PRESS,
                        KeyEvent.VK_C,
                        VK_LCONTROL,
                        onKeyboardCopy)
        );

        keyboardHelper.setHotKey(
                new HotKeySettings(
                        system.common.keyboard.enums.PressKeyType.SHORT_PRESS,
                        KeyEvent.VK_V,
                        VK_LCONTROL,
                        onKeyboardPaste)
        );
    }



    public void startThread() {


        // Get the clipboard data
        clipboardDataFormats = clipboardHelper.getClipboardDataFormats();

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread() {
        thread.interrupt();
    }

    public void pauseListen() {
        listen = false;
    }

    public void resumeListen() {
        listen = true;
    }

    public void setOldClipoardDataFormats(ClipboardDataFormats clipboardDataFormats) {
        this.clipboardDataFormats = clipboardDataFormats;
    }

    public void sendPaste() {
        keyboardHelper.sendVkKeyCode(KeyEvent.VK_V, VK_LCONTROL, true);
    }

    public ClipboardHelper getClipboardHelper() {
        return clipboardHelper;
    }

    @Override
    protected Void call() throws Exception {


        try {

            while (!isCancelled()) {

                Thread.sleep(SLEEP_BETWEEN_CLIPBOARD_CHECKS);


                Platform.runLater(() -> {
                if (listen) {

                    ClipboardDataFormats dataFormatNew = clipboardHelper.getClipboardDataFormats();


                    if (dataFormatNew != null && !dataFormatNew.equals(clipboardDataFormats)) {

                        switch (copyPressType) {
                            case SHORT:
                            case NORMAL:

                                clipboardServiceEvents.onNewCopy(dataFormatNew);
                                break;
                            case LONG:
                                clipboardServiceEvents.onNewCopyLong(dataFormatNew);
                        }

                        copyPressType = PressKeyType.NORMAL;

                        clipboardDataFormats = dataFormatNew;

                    }
                }
                });

            }
        } catch (InterruptedException e) {
            if (!isCancelled())
                Platform.runLater(() -> ErrorEvents.onErrorEvent(e));
            else if (Thread.currentThread().isInterrupted()) {
                return null; // Exit thread event
            }
        }

        return null;
    }


}
