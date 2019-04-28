package system.os.windows;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import system.common.windows.InterfaceWindowsHelper;

public class WindowsHelper implements InterfaceWindowsHelper {

    private WinDef.HWND rememberedWindow = null;

    @Override
    public void rememberActiveWindow() {
        rememberedWindow = User32.INSTANCE.GetForegroundWindow();
    }

    @Override
    public void setRememberedActiveWindow() {
        User32.INSTANCE.SetForegroundWindow(rememberedWindow);
    }
}
