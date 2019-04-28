package system.common.keyboard.events;

import system.common.keyboard.enums.PressKeyType;

public interface HotkeyEvents {
    boolean onHotkeyEvent(boolean keyDown, int vkCodePrimary, int vkCodeSecondly);

}
