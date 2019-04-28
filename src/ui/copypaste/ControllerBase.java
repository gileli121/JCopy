package ui.copypaste;

import classes.ClipboardDataFormats;
import common.CommonHelpers;
import common.FormatConverter;
import database.DatabaseManager;
import database.classes.ClipInfo;
import events.ErrorEvents;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.HTMLEditor;
import program.ProgramDetails;
import ui.copypaste.dialogs.setdisplayname.SetDisplayNameDialogController;
import ui.copypaste.paste.classes.ClipTableViewRow;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ControllerBase {

    protected HTMLEditor richTextView;

    protected TextArea plainTextView;

    protected TabPane clipInfoArea;

    protected Tab richTextTab;

    protected Tab plainTextTab;

    protected Tab clipPropertiesTab;

    protected HBox selectedClipTags;

    @FXML
    protected Tab clipEncryptionTab;

    protected Tab clipLockedTab;

    protected ComboBox<String> selectedClipTagsCombo;

    protected TextArea selectedClipDisplayName;

    @FXML
    protected TextField newPasswordField;

    protected DatabaseManager db;




    protected void redrawClipTabs(ClipInfo clipInfo) {

        boolean isClipPropertiesTabSelected = clipPropertiesTab.isSelected();

        richTextView.setHtmlText("");
        plainTextView.setText("");
        selectedClipDisplayName.setText("");
        newPasswordField.setText("");



        clipInfoArea.getTabs().removeAll(clipLockedTab, richTextTab,plainTextTab,clipPropertiesTab,clipEncryptionTab);

        if (clipInfo == null) return;


        ClipboardDataFormats clipboardDataFormats = clipInfo.clipboardDataFormats;
        if (clipboardDataFormats == null) return;
        //ClipInfo clipInfo = getSelectedClipInfo();

        String plainText = null;
        String htmlText = null;

        if (!clipboardDataFormats.getIsEncrypted() || clipInfo.decryptedDataFormats != null) {

            if (clipInfo.decryptedDataFormats != null) clipboardDataFormats = clipInfo.decryptedDataFormats;

            if (clipboardDataFormats.getPlainText() != null && !clipboardDataFormats.getPlainText().isEmpty())
                plainText = clipboardDataFormats.getPlainText();
            else if (clipboardDataFormats.getUrl() != null && !clipboardDataFormats.getUrl().isEmpty())
                plainText = clipboardDataFormats.getUrl();

            if (clipboardDataFormats.getHtml() != null && !clipboardDataFormats.getHtml().isEmpty())
                htmlText = clipboardDataFormats.getHtml();
            else if (clipboardDataFormats.getRtf() != null && !clipboardDataFormats.getRtf().isEmpty()) {
                try {
                    String temp = FormatConverter.rtfToHtml(clipboardDataFormats.getRtf());

                    if (temp != null && !CommonHelpers.isHtmlEmpty(temp)) htmlText = temp;
                } catch (IOException e) {
                    ErrorEvents.onErrorEvent(e);
                }
            }

            if (htmlText != null) {
                richTextView.setHtmlText(htmlText);
                clipInfoArea.getTabs().add(richTextTab);
            }

            if (plainText != null) {
                plainTextView.setText(plainText);
                clipInfoArea.getTabs().add(plainTextTab);
            }
        } else {
            clipInfoArea.getTabs().add(clipLockedTab);
        }

        clipInfoArea.getTabs().add(clipPropertiesTab);

        clipInfoArea.getTabs().add(clipEncryptionTab);


        if (clipInfo.getClipDisplayName() != null)
            selectedClipDisplayName.setText(clipInfo.getClipDisplayName());

        if (isClipPropertiesTabSelected)
            clipInfoArea.getSelectionModel().select(clipPropertiesTab);
        else {
            if (htmlText != null)
                clipInfoArea.getSelectionModel().select(richTextTab);
            else if (plainText != null)
                clipInfoArea.getSelectionModel().select(plainTextTab);
        }
    }

    protected void removeTag(Button tagButton) {
        selectedClipTags.getChildren().remove(tagButton);
    }


    protected Button drawTag(List<String> selectedTags, String tag) {
        return drawTag(selectedClipTags, selectedTags, tag);
    }

    protected Button drawTag(HBox tagsArea, List<String> selectedTags, String tag) {

        if (selectedTags != null && selectedTags.contains(tag)) return null;

        ImageView xIconImageView = new ImageView(new Image("ui/copypaste/15px-x_mark.png"));
        Button result = new Button(tag, xIconImageView);
        result.setPrefHeight(20);
        result.setContentDisplay(ContentDisplay.RIGHT);

        tagsArea.getChildren().add(result);

        return result;
    }

    protected String getSelectedTagFromCombo() {
        return getSelectedTagFromCombo(selectedClipTagsCombo);
    }

    protected String getSelectedTagFromCombo(ComboBox<String> tagsCombo) {
        // Return if no tag selected
        SingleSelectionModel<String> model = tagsCombo.getSelectionModel();
        if (model.getSelectedItem().equals("Add Tag")) return null;

        // Get the selected tag
        String selectedTag = model.getSelectedItem();
        String selectedTagLowCase = selectedTag.toLowerCase();
        // Find the selected tag in the database and return it's ClipTagInfo struct
        List<String> clipTags = db.getClipInfos().getClipTags();

        for (String tag : clipTags) {
            if (tag.toLowerCase().equals(selectedTagLowCase)) return tag;
        }

        // If the selected tag is not available then add it to the database and the gui
        db.getClipInfos().getClipTags().add(selectedTag);
        tagsCombo.getItems().add(selectedTag);

        return selectedTag;
    }





    protected String getClipDisplayDialog() {
        String newDisplayName;
        do {

            // Load the fxml data that used for building the GUI
            FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/ui/copypaste/dialogs/setdisplayname/SetDisplayNameDialog.fxml"));

            Region dialogFxml;
            try {
                dialogFxml = dialogLoader.load();
            } catch (IOException e) {
                ErrorEvents.onErrorEvent(new Exception(e));
                return null;
            }

            SetDisplayNameDialogController dialogController = dialogLoader.getController();

            // Create window that will contain the content of the GUI
            Dialog<String> dialogStage = new Dialog<>();

            dialogStage.setTitle(ProgramDetails.NAME + " - Set copy name");
            dialogStage.getDialogPane().setContent(dialogFxml);

            dialogStage.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);


            dialogStage.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK)
                    return dialogController.getDisplayName();
                else {
                    newPasswordField.setText("");
                    return null;
                }
            });

            Optional<String> result = dialogStage.showAndWait();
            newDisplayName = null;
            if (result.isPresent()) newDisplayName = result.get();

            dialogStage.close();
            if (newDisplayName == null) return null;


        } while (newDisplayName.isEmpty());
        selectedClipDisplayName.setText(newDisplayName);
        return newDisplayName;
    }



}
