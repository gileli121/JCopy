package system.os.windows.internal;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ex extends StdCallLibrary, WinUser, WinNT {

    User32Ex INSTANCE = (User32Ex)Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

    void keybd_event(
            BYTE      bVk,
            BYTE      bScan,
            DWORD     dwFlags,
            ULONG_PTR dwExtraInfo
    );



}
