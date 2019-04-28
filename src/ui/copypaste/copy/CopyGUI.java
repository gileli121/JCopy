package ui.copypaste.copy;

import common.CommonHelpers;
import database.DatabaseManager;
import database.classes.ClipInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import settings.Settings;
import settings.enums.SaveCopyMode;
import ui.copypaste.ControllerBase;
import ui.copypaste.copy.events.CopyUIEvents;

import java.net.URL;
import java.util.ResourceBundle;

public class CopyGUI extends ControllerBase implements Initializable {

    @FXML
    public CheckBox saveCopy;

    @FXML
    public TabPane clipInfoArea;

    @FXML
    public Tab richTextTab;

    @FXML
    public HTMLEditor richTextView;

    @FXML
    public Tab plainTextTab;

    @FXML
    public TextArea plainTextView;

    @FXML
    public Tab clipPropertiesTab;

    @FXML
    public TextArea selectedClipDisplayName;

    @FXML
    public ComboBox<String> selectedClipTagsCombo;

    @FXML
    public Button selectedClipAddTag;

    @FXML
    public HBox selectedClipTags;

    @FXML
    public Button saveClip;

    @FXML
    public ComboBox saveCopyModeCombo;

    @FXML
    public HBox newPasswordArea;

    @FXML
    public Tab clipEncryptionTab;

    @FXML
    public TextField newPasswordField;

    private DatabaseManager db;

    private CopyUIEvents copyUIEvents;

    private ClipInfo clipInfo;

    private String password = null;

    public void initUI(DatabaseManager db, CopyUIEvents copyUIEvents) {
        this.db = db;
        super.db = db;
        this.copyUIEvents = copyUIEvents;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        super.clipInfoArea = clipInfoArea;
        super.clipPropertiesTab = clipPropertiesTab;
        super.plainTextTab = plainTextTab;
        super.plainTextView = plainTextView;
        super.richTextTab = richTextTab;
        super.richTextView = richTextView;
        super.selectedClipTagsCombo = selectedClipTagsCombo;
        super.selectedClipTags = selectedClipTags;
        super.selectedClipDisplayName = selectedClipDisplayName;
        super.clipEncryptionTab = clipEncryptionTab;
        super.newPasswordField = newPasswordField;

        saveCopyModeCombo.getItems().addAll("None", "Save", "Save & Database Save");

        drawSaveCopyModeCombo();
        enableDisableRootTab();
    }

    public void onClipDisplayNameChange(KeyEvent keyEvent) {
        String newDisplayName = selectedClipDisplayName.getText();
        if (newDisplayName != null && newDisplayName.isEmpty()) newDisplayName = null;
        clipInfo.setClipDisplayName(newDisplayName);
    }

    public void onNewTagInSelectedClip(ActionEvent actionEvent) {
        String tagName = getSelectedTagFromCombo();
        if (tagName == null) return;
        Button tagButton = drawTag(clipInfo.getClipTags(), tagName);
        if (tagButton == null) return;
        tagButton.setOnAction(event -> onTagUnselectedInSelectedClip(tagButton));
        clipInfo.addTag(tagName);
    }

    private void onTagUnselectedInSelectedClip(Button tagButton) {
        clipInfo.removeTag(tagButton.getText());
        removeTag(tagButton);
    }

    public void onSetEncryptionBtn(ActionEvent actionEvent) {
        // Get the password

        password = null;

        String newPassword = newPasswordField.getText().trim();
        if (newPassword.isEmpty()) return;

        String displayName = selectedClipDisplayName.getText().trim();
        if (displayName.isEmpty()) {
            displayName = getClipDisplayDialog();
            if (displayName == null) return;
        }
        password = newPassword;
    }

    private void enableDisableRootTab() {

        if (Settings.getUiSaveCopyMode() == SaveCopyMode.NONE)
            clipInfoArea.setDisable(true);
        else if (clipInfoArea.isDisabled())
            clipInfoArea.setDisable(false);
    }

    public void onSaveCopyModeChanged(ActionEvent actionEvent) {
        updateSaveCopyModeSettings();
        enableDisableRootTab();
    }

    public void onSaveClip(ActionEvent actionEvent) {

        if (Settings.getUiSaveCopyMode() == SaveCopyMode.NONE) {
            copyUIEvents.onCopyUIClose();
            return;
        }

        String clipDisplayName = selectedClipDisplayName.getText();
        if (clipDisplayName != null && !clipDisplayName.isEmpty())
            clipInfo.setClipDisplayName(clipDisplayName);
        else
            clipDisplayName = null;

        String clipHtmlText = richTextView.getHtmlText();
        if (clipHtmlText != null && !clipHtmlText.isEmpty())
            clipInfo.clipboardDataFormats.setHtml(clipHtmlText);
        else
            clipHtmlText = null;

        String clipPlainText = plainTextView.getText();
        if (clipPlainText != null && !clipPlainText.isEmpty())
            clipInfo.clipboardDataFormats.setPlainText(clipPlainText);
        else {
            if (clipHtmlText != null) {
                clipPlainText = CommonHelpers.getTextFromHtml(clipHtmlText);
                if (CommonHelpers.removeNewLines(clipPlainText).isEmpty()) {
                    copyUIEvents.onCopyUIClose();
                    return;
                }
                clipInfo.clipboardDataFormats.setPlainText(clipPlainText);
            } else {
                copyUIEvents.onCopyUIClose();
                return;
            }
        }

        if (password != null) {
            clipInfo.clipboardDataFormats.setEncryption(password);
        }

        switch (Settings.getUiSaveCopyMode()) {
            case SAVE_AND_DATABASE:
                clipInfo.setSaveToDb(true);
                break;
            case SAVE_ONLY:
                clipInfo.setSaveToDb(false);
                break;
        }

        clipInfo.updateTimestamp();

        copyUIEvents.onCopyUISave(clipInfo);
    }

    // Draw functions
    private void drawSaveCopyModeCombo() {
        switch (Settings.getUiSaveCopyMode()) {
            case NONE:
                saveCopyModeCombo.getSelectionModel().select(0);
                break;
            case SAVE_ONLY:
                saveCopyModeCombo.getSelectionModel().select(1);
                break;
            case SAVE_AND_DATABASE:
                saveCopyModeCombo.getSelectionModel().select(2);
                break;
        }
    }

    private void drawEmptyTabs() {

    }

    public void clearContent() {
        selectedClipDisplayName.clear();
        richTextView.setHtmlText("");
        plainTextView.clear();
        selectedClipTags.getChildren().clear();
    }

    // Controller functions

    private void updateSaveCopyModeSettings() {
        switch (saveCopyModeCombo.getSelectionModel().getSelectedIndex()) {
            case 0:
                Settings.setUiSaveCopyMode(SaveCopyMode.NONE);
                break;
            case 1:
                Settings.setUiSaveCopyMode(SaveCopyMode.SAVE_ONLY);
                break;
            case 2:
                Settings.setUiSaveCopyMode(SaveCopyMode.SAVE_AND_DATABASE);
                break;
        }
    }

    public void setClipInfo(ClipInfo clipInfo) {
        this.clipInfo = clipInfo;
        redrawClipTabs(clipInfo);
        selectedClipTags.getChildren().clear();
    }

    public void setNewCopyMode() {
        clipInfo = new ClipInfo();
        clipInfoArea.getTabs().removeAll(richTextTab, plainTextTab, clipPropertiesTab);
        richTextView.setHtmlText("");
        plainTextView.setText("");
        selectedClipDisplayName.setText("");
        clipInfoArea.getTabs().addAll(richTextTab, plainTextTab, clipPropertiesTab);
    }
}
