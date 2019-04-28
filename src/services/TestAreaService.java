package services;

import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import javafx.concurrent.Task;
import system.common.clipboard.exceptions.ClipboardGetDataFormatsException;
import system.os.windows.KeyboardHelper;

public class TestAreaService extends Task<Void> {

    // Thread
    private Thread thread = null;
    private boolean runThread;

    public WinUser.HOOKPROC hookproc;

    private static KeyboardHelper keyboardHelper;

    public void start() {
        // Set the thread
        runThread = true;
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();


    }

    public void stop() throws InterruptedException {
        runThread = false;
        thread.join();
    }


    @Override
    protected Void call() throws Exception {



//
//        Platform.runLater(() -> {
//
//            WinUser.HOOKPROC hookproc2 = new WinUser.HOOKPROC() {
//                public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
//                    System.err.println("callback bbbnhkilhjkibh nCode: " + nCode);
//
//
//        /*
//            return new LRESULT(0);  - to unblock the key
//            return new LRESULT(1); - to block the key
//         */
//                    return new WinDef.LRESULT(1);
//                }
//            };
//
//            KeyboardHelper keyboardHelper = new KeyboardHelper();
//
//            while (true) {
//                keyboardHelper.process();
//            }
//        });

        return null;
    }
}
