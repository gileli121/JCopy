package ui.about;

import events.ErrorEvents;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import program.ProgramDetails;
import ui.about.events.AboutUIEvents;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutGUI implements Initializable {

    @FXML
    public Label versionLabel;

    @FXML
    public Label programNameLabel;

    @FXML
    public Hyperlink websiteDisplayLabel;


    private AboutUIEvents aboutUIEvents;

    public void initUI(AboutUIEvents aboutUIEvents) {
        this.aboutUIEvents = aboutUIEvents;
    }

    public void onWebsiteClick(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URI(ProgramDetails.WEBSITE));
        } catch (IOException | URISyntaxException e) {
            ErrorEvents.onErrorEvent(new Exception("Failed to open the browser window", e));
        }
    }

    public void onCloseWindow(ActionEvent actionEvent) {
        aboutUIEvents.onAboutUiClose();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        programNameLabel.setText(ProgramDetails.NAME);
        versionLabel.setText("Version " + ProgramDetails.VERSION);
        websiteDisplayLabel.setText(ProgramDetails.WEBSITE_DISPLAY);
    }
}
