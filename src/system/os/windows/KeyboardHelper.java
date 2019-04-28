package system.os.windows;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import system.common.keyboard.InterfaceKeyboardHelper;
import system.common.keyboard.classes.HotKeySettings;
import system.common.keyboard.events.HotkeyEvents;
import system.os.windows.internal.User32Ex;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinUser.KEYBDINPUT.KEYEVENTF_KEYUP;

public class KeyboardHelper implements InterfaceKeyboardHelper, WinUser.HOOKPROC {

    // Windows - variables
    private User32.MSG msg;

    private WinNT.HANDLE processHandle;

    // Windows - constants
    private static final int WM_KEYDOWN = 0x0100;

    private static final int WM_KEYUP = 0x0101;

    // Windows - Read from windows variables
    private IntByReference readVkCode = new IntByReference(0);

    private Memory vkCodeMemOutput = new Memory(4);

    // Object - variables
    private HotkeyEvents hotkeyEvents;

    private List<HotKeySettings> hotkeyEventsList;

    private boolean disableKeyListen = false;

    public int key1 = -1;

    public int key2 = -1;

    @Override
    public void setHotKey(HotKeySettings hotKey) {
        hotkeyEventsList.add(hotKey);
    }

    @Override
    public void removeHotKey(HotKeySettings hotKey) {
        hotkeyEventsList.remove(hotKey);
    }

    @Override
    public void process() {
        User32.INSTANCE.GetMessage(msg, null, 0, 0);

        if (msg.message == 0x0301)
            System.out.println("COPY_MESSAGE");
    }

    @Override
    public void unInitialize() {

    }

    @Override
    public void sendVkKeyCode(int vkCodePrimary, int vkCodeSecondly, boolean releaseSecondly) {

//        User32.I.PostMessage(User32.I.GetForegroundWindow(),
//                WM_KEYDOWN,new WinDef.WPARAM(vkCode), new WinDef.LPARAM(vkCode));

        disableKeyListen = true;

        if (vkCodeSecondly > 0) {
            User32Ex.INSTANCE.keybd_event(new WinDef.BYTE(vkCodeSecondly), new WinDef.BYTE(0), new WinDef.DWORD(KEYEVENTF_KEYUP), new BaseTSD.ULONG_PTR(0));
            User32Ex.INSTANCE.keybd_event(new WinDef.BYTE(vkCodeSecondly), new WinDef.BYTE(0), new WinDef.DWORD(0), new BaseTSD.ULONG_PTR(0));
        }
        User32Ex.INSTANCE.keybd_event(new WinDef.BYTE(vkCodePrimary), new WinDef.BYTE(0), new WinDef.DWORD(0), new BaseTSD.ULONG_PTR(0));
        User32Ex.INSTANCE.keybd_event(new WinDef.BYTE(vkCodePrimary), new WinDef.BYTE(0), new WinDef.DWORD(KEYEVENTF_KEYUP), new BaseTSD.ULONG_PTR(0));

        if (releaseSecondly) {
            User32Ex.INSTANCE.keybd_event(new WinDef.BYTE(vkCodeSecondly), new WinDef.BYTE(0), new WinDef.DWORD(KEYEVENTF_KEYUP), new BaseTSD.ULONG_PTR(0));
        }

        disableKeyListen = false;
    }

    @Override
    public void releaseVkKeyCode(int vkCode) {
        User32Ex.INSTANCE.keybd_event(new WinDef.BYTE(vkCode), new WinDef.BYTE(0), new WinDef.DWORD(KEYEVENTF_KEYUP), new BaseTSD.ULONG_PTR(0));
    }

    public KeyboardHelper() {

        initialize();
    }

    private boolean initialize() {

        // Register the keyboard hook
        WinDef.HINSTANCE hInst = Kernel32.INSTANCE.GetModuleHandle(null);

        User32.HHOOK hHook = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, this, hInst, 0);
        if (hHook == null) return false;
        msg = new User32.MSG();

        // Open the process handle
        String process = ManagementFactory.getRuntimeMXBean().getName();
        int proccessPid = Integer.valueOf(process.split("@")[0]);
        processHandle = Kernel32.INSTANCE.OpenProcess(0x0010, true, proccessPid);

        // Initialize list of hotkeys
        hotkeyEventsList = new ArrayList<>();

        return true;
    }

    public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {

        if (disableKeyListen)
            return new WinDef.LRESULT(0);

        // Get vkCode and intWParam
        Kernel32.INSTANCE.ReadProcessMemory(processHandle, lParam.toPointer(), vkCodeMemOutput, 4, readVkCode);
        int vkCode = vkCodeMemOutput.getInt(0);
        int intWParam = wParam.intValue();

        switch (intWParam) {
            case WM_KEYDOWN:

                if (key1 == -1 || key1 == vkCode) {

                    if (key1 == -1)
                        key1 = vkCode;
                } else if (key2 == -1) {
                    key2 = key1; // ctrl
                    key1 = vkCode; // v
                } else {

                }

                for (HotKeySettings hotKeySettings : hotkeyEventsList) {

                    if (hotKeySettings.primaryKey == key1 && hotKeySettings.secondlyKey == key2) {
                        int block = (hotKeySettings.eventHandler.onHotkeyEvent(true, key1, key2)) ? 1 : 0;
                        return new WinDef.LRESULT(block);
                    }
                }

                break;
            case WM_KEYUP:

                int block = 0;

                for (HotKeySettings hotKeySettings : hotkeyEventsList) {
                    if (hotKeySettings.primaryKey == key1 && hotKeySettings.secondlyKey == key2) {
                        block = (hotKeySettings.eventHandler.onHotkeyEvent(false, key1, key2)) ? 1 : 0;
                        break;
                    }
                }

                if (key2 == -1)
                    key1 = -1;
                else if (key2 == vkCode) {
                    key1 = -1;
                    key2 = -1;
                } else if (key1 == vkCode) {
                    key1 = -1;
                }

                return new WinDef.LRESULT(block);

//                if (vkCode == key1) {
//                    int block = 0;
//                    for (HotKeySettings hotKeySettings : hotkeyEventsList) {
//                        if (hotKeySettings.primaryKey == key1 && hotKeySettings.secondlyKey == key2) {
//                            block = (hotKeySettings.eventHandler.onHotkeyEvent(false, key1, key2)) ? 1 : 0;
//                            break;
//                        }
//                    }
//                    key1 = -1;
//                    return new WinDef.LRESULT(block);
//
//                } else if (vkCode == key2)
//                    key2 = -1;

            //break;

        }

        return new WinDef.LRESULT(0);
    }

    public WinDef.LRESULT callback_old(int nCode, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {

        Kernel32.INSTANCE.ReadProcessMemory(processHandle, lParam.toPointer(), vkCodeMemOutput, 4, readVkCode);

        int vkCode = vkCodeMemOutput.getInt(0);

        int intWParam = wParam.intValue();

        switch (intWParam) {
            case WM_KEYDOWN:
                System.out.println("KEY_DOWN - Key: " + vkCode);
                break;
            case WM_KEYUP:
                System.out.println("KEY_UP - Key: " + vkCode);
                break;

            default:
                return new WinDef.LRESULT(0);
        }


        /*
            return new LRESULT(0);  - to unblock the key
            return new LRESULT(1); - to block the key

            hotkeyEvents.onHotkeyEvent(10,PressKeyType.SHORT_PRESS);
         */
        return new WinDef.LRESULT(1);
    }
}
