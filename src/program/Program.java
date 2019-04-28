package program;

import classes.ClipboardDataFormats;
import database.DatabaseManager;
import database.classes.ClipInfo;
import events.ErrorEvents;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import services.clipboard.events.ClipboardServiceEvents;
import javafx.stage.WindowEvent;
import services.clipboard.ClipboardService;
import settings.Settings;
import system.os.windows.ClipboardHelper;
import system.os.windows.WindowsHelper;
import ui.about.AboutGUI;
import ui.about.events.AboutUIEvents;
import ui.common.UndecoratorScene;
import ui.copypaste.copy.CopyGUI;
import ui.copypaste.copy.events.CopyUIEvents;
import ui.copypaste.paste.PasteGUI;
import ui.copypaste.paste.events.PasteUiEvents;
import ui.tray.MainTray;
import ui.tray.events.TrayUIEvents;

import java.io.IOException;
import java.sql.SQLException;

public class Program extends Application
        implements ClipboardServiceEvents, PasteUiEvents, CopyUIEvents, TrayUIEvents,
        AboutUIEvents {

    private Stage pasteGuiStage = null;

    private Stage copyGuiStage = null;

    private Stage aboutGuiStage = null;

    private PasteGUI pasteGUI;

    private CopyGUI copyGUI;

    private AboutGUI aboutGUI;

    private MainTray mainTray;

    private ClipboardService clipboardService;

    private DatabaseManager db;

    private WindowsHelper windowsHelper;

    public static void main(String[] args) {
        launch(args);
    }



    public void start(Stage primaryStage) throws Exception {

//        String test = "{\\rtf\\ansi{\\fonttbl{\\f0 Consolas;}}{\\colortbl;\\red163\\green21\\blue21;}\\f0 \\fs19 \\cf1 \\cb0 \\highlight0 n6zOfIL7N6bnG3GMOZwSIJBB_u4-xKNZW3FTqMkcUig1}\u0000";
//        String temp = FormatConverter.rtfToHtml(test);
//
//        System.out.println(CommonHelpers.isHtmlEmpty(temp));

//        String out = EncryptionHelper.encrypt("abcd","a1234");
//        System.out.println(out);


        initProgram();
        clipboardService.startThread();

        // Create the database manager
//        db = new DatabaseManager(this);
//
//
//        for (int i = 1; i <= 10; i++) {
//            db.getClipInfos().
//                    add(new ClipInfo(
//                            new ClipboardDataFormats("plainText", "html", "rtf", null)
//                    ));
//
//        }

//        pasteGuiStage.show();
//

//        onNewCopyLong(new ClipboardDataFormats("Some Text", "Some HTML","Some RTF", "Some URL", null));
//        onNewPasteLong();
//

//        copyGuiStage.show();

//        String rtf = "{\\rtf\\ansi{\\fonttbl{\\f0 Consolas;}}{\\colortbl;\\red163\\green21\\blue21;}\\f0 \\fs19 \\cf1 \\cb0 \\highlight0 https://example.testproject.io/web/}";
////
//        String html = FormatConverter.rtfToHtml(rtf);
////
//        System.out.println(html);

    }

    private void initProgram() throws SQLException, ClassNotFoundException {

        Settings.initialize();

        // Disable exiting the program when user press the X button in some window
        Platform.setImplicitExit(false);

        //Register the ClipboardService service
        clipboardService = new ClipboardService(this);

        // Create the database manager
        db = new DatabaseManager();

        // Create the windows helper
        windowsHelper = new WindowsHelper();

        // Load GUIs
        createTrayIcon();
        createPasteGUI();
        createCopyGUI();
    }

    private void setGuiIcon(Stage stage) {
        stage.getIcons().add(new Image("/ui/tray/icon.png"));
    }

    private void createPasteGUI() {
        // Load the fxml data that used for building the GUI
        FXMLLoader pasteGuiLoader = new FXMLLoader(getClass().getResource("/ui/copypaste/paste/PasteGUI.fxml"));

        Region pasteGuiFxml = null;
        try {
            pasteGuiFxml = (Region) pasteGuiLoader.load();
        } catch (IOException e) {
            ErrorEvents.onErrorEvent(new Exception(e));
        }

        // Create window that will contain the content of the GUI
        pasteGuiStage = new Stage();

        pasteGuiStage.setTitle(ProgramDetails.NAME + " - Copies List");

        final UndecoratorScene undecoratorScene = new UndecoratorScene(pasteGuiStage, pasteGuiFxml);

        pasteGuiStage.setScene(undecoratorScene);

        // Get the controller of the GUI
        pasteGUI = pasteGuiLoader.getController();

        // Set database to the controller
        pasteGUI.initUI(db, this);

        pasteGuiStage.addEventHandler(WindowEvent.WINDOW_SHOWING, window -> pasteGUI.redrawAll());

        pasteGuiStage.setOnCloseRequest(event -> onPasteUiClose());


    }


    private void createCopyGUI() {

        // Load the fxml data that used for building the GUI
        FXMLLoader copyGuiLoader = new FXMLLoader(getClass().getResource("/ui/copypaste/copy/CopyGUI.fxml"));

        Region copyGuiFxml = null;
        try {
            copyGuiFxml = (Region) copyGuiLoader.load();
        } catch (IOException e) {
            ErrorEvents.onErrorEvent(new Exception(e));
        }


        // Create window that will contain the content of the GUI
        copyGuiStage = new Stage();

        copyGuiStage.setTitle(ProgramDetails.NAME + " - Create new copy");


        final UndecoratorScene undecoratorScene = new UndecoratorScene(copyGuiStage, copyGuiFxml);

        copyGuiStage.setScene(undecoratorScene);

        // Get the controller of the GUI
        copyGUI = copyGuiLoader.getController();

        // Set database to the controller
        copyGUI.initUI(db, this);

        copyGuiStage.setOnCloseRequest(event -> onCopyUIClose());

        setGuiIcon(copyGuiStage);
    }

    private void createAboutGUI() {

        if (aboutGuiStage != null) return;

        // Load the fxml data that used for building the GUI
        FXMLLoader aboutGuiLoader = new FXMLLoader(getClass().getResource("/ui/about/AboutGUI.fxml"));

        // Create window that will contain the content of the GUI
        aboutGuiStage = new Stage();
        aboutGuiStage.setTitle("About " + ProgramDetails.NAME);
        aboutGuiStage.setResizable(false);

        // Set the content on the window
        try {
            aboutGuiStage.setScene(new Scene(aboutGuiLoader.load(), 412, 268));
        } catch (IOException e) {
            ErrorEvents.onErrorEvent(e);
            aboutGuiStage.close();
            aboutGuiStage = null;
        }

        aboutGUI = aboutGuiLoader.getController();
        aboutGUI.initUI(this);
    }

    private void createTrayIcon() {
        mainTray = new MainTray(this);
        mainTray.create();
    }

    @Override
    public void onNewCopy(ClipboardDataFormats clipboardDataFormats) {
        System.out.println("onNewCopy");

        clipboardService.initializeHotkeys();

        if (!Settings.getClipRecord()) return;

        int clipIndex = db.getClipInfos().findIndex(clipboardDataFormats, false);
        if (clipIndex != -1) {
            db.getClipInfos().moveClipToLast(clipIndex);
            return;
        }

        db.getClipInfos().add(new ClipInfo(clipboardDataFormats));
    }

    @Override
    public void onNewCopyLong(ClipboardDataFormats clipboardDataFormats) {
        System.out.println("Hello from onNewCopyLong");

        clipboardService.pauseListen();

        copyGUI.setClipInfo(new ClipInfo(clipboardDataFormats));

        copyGuiStage.show();
    }

    @Override
    public void onNewPasteLong() {
        System.out.println("Hello from long press paste");

        clipboardService.pauseListen();

        windowsHelper.rememberActiveWindow();

        pasteGuiStage.show();
    }

    @Override
    public void onPasteUserRequest(ClipInfo clipInfo, boolean pasteAsPlain) {
        // Close the window
        pasteGuiStage.close();

        // Activate the window that was active before opening the window
        windowsHelper.setRememberedActiveWindow();

        clipboardService.pauseListen();

        // Get the clipboard helper
        ClipboardHelper clipboardHelper = clipboardService.getClipboardHelper();

        // Get the current clip from clipboard
        ClipboardDataFormats currentClip = clipboardHelper.getClipboardDataFormats();

        // Get the decrypted data formats
        ClipboardDataFormats decryptedDataFormats;
        if (clipInfo.decryptedDataFormats != null)
            decryptedDataFormats = clipInfo.decryptedDataFormats;
        else
            decryptedDataFormats = clipInfo.clipboardDataFormats;

        // Create the clip to paste
        ClipboardDataFormats clipToPaste;
        if (pasteAsPlain)
            clipToPaste = new ClipboardDataFormats(decryptedDataFormats.getPlainText());
        else
            clipToPaste = decryptedDataFormats;

        if (!currentClip.equals(decryptedDataFormats)) {
            clipboardHelper.setClipboardDataFormats(clipToPaste);
//            // Repos the selected clip to the the last one
//            db.getClipInfos().remove(clipInfo);
//            clipInfo.updateTimestamp();
//            db.getClipInfos().add(clipInfo);
        }

        // Send paste command
        clipboardService.sendPaste();

        // Start to listen again to clipboard changes
        onPasteUiClose();
    }

    @Override
    public void onPasteUiClose() {
        pasteGUI.onStageClosed();
        clipboardService.initializeHotkeys();
        clipboardService.resumeListen();
    }

    @Override
    public void onCopyUISave(ClipInfo clipInfo) {
        int clipIndex = db.getClipInfos().findIndex(clipInfo.clipboardDataFormats, false);

        if (clipIndex == -1)
            db.getClipInfos().add(clipInfo);
        else
            db.getClipInfos().moveClipToLast(clipIndex);

        onCopyUIClose();
    }

    @Override
    public void onCopyUIClose() {
        copyGUI.clearContent();
        clipboardService.initializeHotkeys();
        clipboardService.setOldClipoardDataFormats(clipboardService.getClipboardHelper().getClipboardDataFormats());
        clipboardService.resumeListen();
        if (copyGuiStage.isShowing()) copyGuiStage.close();
    }

    @Override
    public void onExitProgram() {
        clipboardService.stopThread();
        Platform.exit();
    }

    @Override
    public void onOpenClipList() {
        if (pasteGuiStage.isShowing()) return;
        clipboardService.pauseListen();
        pasteGuiStage.show();
    }

    @Override
    public void onCreateNewCopy() {
        clipboardService.pauseListen();
        copyGUI.setNewCopyMode();
        copyGuiStage.show();
    }

    @Override
    public void onAboutClicked() {
        Platform.runLater(() -> {
            if (aboutGuiStage == null) {
                createAboutGUI();
            } else if (aboutGuiStage.isShowing()) {
                return;
            }
            aboutGuiStage.show();
        });
    }

    @Override
    public void onRecordClipsClicked(boolean state) {
        Settings.setClipRecord(state);

    }

    @Override
    public void onAboutUiClose() {
        aboutGuiStage.close();

    }
}
