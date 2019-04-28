package ui.tray.events;

public interface TrayUIEvents {
    void onExitProgram();
    void onOpenClipList();
    void onCreateNewCopy();
    void onAboutClicked();
    void onRecordClipsClicked(boolean state);
}
