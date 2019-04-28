package system.common.keyboard;
import system.common.keyboard.classes.HotKeySettings;

public interface InterfaceKeyboardHelper {

    void setHotKey(HotKeySettings hotKey);

    void removeHotKey(HotKeySettings hotKey);

    void process();

    void unInitialize();

    void sendVkKeyCode(int vkCodePrimary, int vkCodeSecondly, boolean releaseSecondly);

    void releaseVkKeyCode(int vkCode);

}
