package ui.tray;

import events.ErrorEvents;
import javafx.application.Platform;
import program.ProgramDetails;
import settings.Settings;
import ui.tray.events.TrayUIEvents;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;


public class MainTray {

    private TrayUIEvents trayUIEvents;

    public MainTray(TrayUIEvents trayUIEvents) {
        this.trayUIEvents = trayUIEvents;
    }


    public void create() {
        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
    }


    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            Toolkit.getDefaultToolkit();

            if (!SystemTray.isSupported()) {
                ErrorEvents.onErrorEvent(new Exception("No system tray support"));
                return;
            }

            // Create the system tray icon
            SystemTray tray = SystemTray.getSystemTray();
            Image image = ImageIO.read(getClass().getResource("/ui/tray/icon.png"));
            TrayIcon trayIcon = new TrayIcon(image);


            Font defaultFont = Font.decode(null);
            Font boldFont = defaultFont.deriveFont(Font.BOLD);

            // Create the menu for the tray icon
            final PopupMenu popup = new java.awt.PopupMenu();
            trayIcon.setPopupMenu(popup);


            // Set double click event
            trayIcon.addActionListener(event -> Platform.runLater(() -> trayUIEvents.onOpenClipList()));


            // View all copies
            MenuItem viewClipsItem = new MenuItem("View all copies");
            viewClipsItem.addActionListener(event -> Platform.runLater(() -> trayUIEvents.onOpenClipList()));
            viewClipsItem.setFont(boldFont);
            popup.add(viewClipsItem);


            // Create new copy
            MenuItem createNewCopy = new MenuItem("Create new copy");
            createNewCopy.addActionListener(event -> Platform.runLater(() -> trayUIEvents.onCreateNewCopy()));
            createNewCopy.setFont(boldFont);
            popup.add(createNewCopy);


            // Create the about item
            MenuItem aboutItem = new MenuItem("About " + ProgramDetails.NAME);
            aboutItem.addActionListener(event -> {
                System.out.println("On About clicked");
                trayUIEvents.onAboutClicked();
            });
            popup.add(aboutItem);

            CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem("Record new copies", Settings.getClipRecord());
            checkboxMenuItem.addItemListener(event -> trayUIEvents.onRecordClipsClicked(checkboxMenuItem.getState()));

            popup.add(checkboxMenuItem);

            // Create the exit item
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(event -> {
                System.out.println("On exit clicked");
                tray.remove(trayIcon);
                trayUIEvents.onExitProgram();
            });
            popup.add(exitItem);


            // Add the tray to the system tray icons
            tray.add(trayIcon);


        } catch (AWTException | IOException e) {
            ErrorEvents.onErrorEvent( new Exception("Unable to init system tray", e));
        }
    }



}
