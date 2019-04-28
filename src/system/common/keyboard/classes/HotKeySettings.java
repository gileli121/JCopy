package system.common.keyboard.classes;

import system.common.keyboard.enums.PressKeyType;
import system.common.keyboard.events.HotkeyEvents;

import java.awt.event.KeyEvent;

public class HotKeySettings {

    public HotKeySettings(PressKeyType pressKeyType, int primaryKey, int secondlyKey, HotkeyEvents eventHandler) {
        this.pressKeyType = pressKeyType;
        this.primaryKey = primaryKey;
        this.secondlyKey = secondlyKey;
        this.eventHandler = eventHandler;
    }

    public PressKeyType pressKeyType;
    public int primaryKey;
    public int secondlyKey = -1;
    public HotkeyEvents eventHandler;

}
