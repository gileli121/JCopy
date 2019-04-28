package ui.copypaste.paste;

import classes.ClipboardDataFormats;
import database.DatabaseManager;
import database.classes.ClipInfo;
import database.classes.ClipInfoList;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.util.Callback;
import settings.Settings;
import ui.copypaste.ControllerBase;
import ui.copypaste.paste.classes.ClipTableViewRow;
import ui.copypaste.paste.events.PasteUiEvents;
import ui.copypaste.paste.exceptions.ClipTableViewRowException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasteGUI extends ControllerBase implements Initializable {

    // ##############################################################################
    //                                  Controls
    // ##############################################################################

    @FXML
    public TableView<ClipTableViewRow> clipsTableView;

    @FXML
    public TableColumn clipCol;

    @FXML
    public TableColumn TagCol;

    @FXML
    public TableColumn dateCol;

    @FXML
    public TableColumn<ClipTableViewRow, Boolean> saveCol;

    @FXML
    public TabPane clipInfoArea;

    @FXML
    public Tab richTextTab;

    @FXML
    public Tab plainTextTab;

    @FXML
    public TextArea selectedClipDisplayName;

    @FXML
    public ComboBox<String> selectedClipTagsCombo;

    @FXML
    public Button selectedClipAddTag;

    @FXML
    public HBox selectedClipTags;

    @FXML
    public Tab clipPropertiesTab;

    @FXML
    public CheckBox autoSaveNextCopiesCheckbox;

    @FXML
    public Button maximizeButton;

    @FXML
    public Button closeButton;

    @FXML
    public Tab clipEncryptionTab;

    @FXML
    public HBox newPasswordArea;

    @FXML
    public TextField newPasswordField;

    @FXML
    public Button setEncryptionBtn;

    @FXML
    public Tab clipLockedTab;

    @FXML
    private ComboBox<String> tagsCombo;

    @FXML
    private Button pasteSelectedClip;

    @FXML
    private TextArea searchClip;

    @FXML
    private Button deleteSelectedClip;

    @FXML
    private HTMLEditor richTextView;

    @FXML
    private HBox tagsArea;

    @FXML
    private TextArea plainTextView;

    // ##############################################################################
    //                                  Variables
    // ##############################################################################

    private DatabaseManager db;

    private boolean isClipControlsEnabled = false;

    private PasteUiEvents pasteUiEvents;

    private String searchText = "";

    private TableRow<ClipTableViewRow> clipRow;

    private List<String> selectedTags;

    private boolean richTextExist = false;

    private Stage mainStage;

    private List<ClipInfo> decryptedClipInfoList;

    private ClipInfo oldClipInfo = null;

    // ##############################################################################
    //                                  Initializers
    // ##############################################################################
    public PasteGUI() throws IOException {
    }

    public void initUI(DatabaseManager databaseManager, PasteUiEvents pasteUiEvents) {
        this.db = databaseManager;
        super.db = databaseManager;
        this.pasteUiEvents = pasteUiEvents;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        super.clipLockedTab = clipLockedTab;
        super.clipInfoArea = clipInfoArea;
        super.clipPropertiesTab = clipPropertiesTab;
        super.plainTextTab = plainTextTab;
        super.plainTextView = plainTextView;
        super.richTextTab = richTextTab;
        super.clipEncryptionTab = clipEncryptionTab;
        super.newPasswordField = newPasswordField;
        super.richTextView = richTextView;
        super.selectedClipTags = selectedClipTags;
        super.selectedClipDisplayName = selectedClipDisplayName;

        decryptedClipInfoList = new ArrayList<>();

        clipCol.setCellValueFactory(new PropertyValueFactory<>("ClipText"));

        TagCol.setCellValueFactory(new PropertyValueFactory<>("ClipTags"));

        dateCol.setCellValueFactory(new PropertyValueFactory<>("ClipDate"));

        disableEnableClipControlsGroupA(false);

        clipsTableView.setRowFactory(tv -> {
            clipRow = new TableRow<>();
            clipRow.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2)
                    onPasteSelectedClip(getSelectedClipInfo());
            });
            return clipRow;
        });

        selectedTags = new ArrayList<>();

        //saveCol.setCellValueFactory(new PropertyValueFactory<ClipTableViewRow, String>("name"));
        //saveCol.setCellValueFactory(new PropertyValueFactory<ClipTableViewRow, Boolean>("checked"));

        Image lockedImage = new Image("/ui/copypaste/paste/locked.png");

        saveCol.setCellFactory(new SaveCheckBoxCellFactory());

        clipCol.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<ClipTableViewRow, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (isEmpty())
                            setText(null);
                        else
                            setText(item);

                        ClipTableViewRow clipTableViewRow = (ClipTableViewRow) getTableRow().getItem();

                        if (clipTableViewRow != null) {

                            if (clipTableViewRow.clipInfo.getClipDisplayName() != null) {
                                setStyle("-fx-text-fill: blue;-fx-font-weight: bold");
                            } else {
                                setStyle("");
                            }

                            if (clipTableViewRow.clipInfo.getIsEncrypted())
                                setGraphic(new ImageView(lockedImage));
                            else
                                setGraphic(null);
                        } else {
                            setStyle("");
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        autoSaveNextCopiesCheckbox.setSelected(Settings.getAutoSaveDb());
    }

    public void onUnlockClip() {
        ClipInfo clipInfo = getSelectedClipInfo();
        if (clipInfo == null) return;
        decryptClipDialog(clipInfo);
        decryptedClipInfoList.add(clipInfo);
        redrawResults();
    }

    private class SaveCheckBoxCellFactory implements Callback {

        @Override
        public TableCell call(Object param) {
            CheckBoxTableCell<ClipTableViewRow, Boolean> checkBoxCell = new CheckBoxTableCell<>();
            checkBoxCell.setOnMouseClicked(e -> {
                ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
                if (clipTableViewRow == null) return;
                if (clipTableViewRow.getSaveDbChecked()) {
                    clipTableViewRow.setSaveDbChecked(false);
                    clipTableViewRow.clipInfo.sqlRemove();
                } else {
                    clipTableViewRow.setSaveDbChecked(true);
                    clipTableViewRow.clipInfo.sqlAdd();
                }
            });
            return checkBoxCell;
        }
    }

    // ##############################################################################
    //                                  Draw methods
    // ##############################################################################

    public void redrawAll() {
        redrawSearchControls();
        redrawResults();
        redrawTagsCombo(selectedClipTagsCombo);
    }

    private void redrawResults() {

        // Draw clips from the database
        redrawClips();

        redrawDisplayName();

        redrawClipTabs();

        redrawButtonsForSelectedClip();
    }

    private void redrawSearchControls() {
        redrawTagsCombo(tagsCombo);
    }

    private void redrawTagsCombo(ComboBox<String> tagsCombo) {
        ObservableList<String> tagsComboItems = tagsCombo.getItems();
        tagsComboItems.clear();

        tagsComboItems.addAll(db.getClipInfos().getClipTags());
    }

    private void redrawSelectedTagsForSelectedClip() {
        selectedClipTags.getChildren().clear();

        ClipInfo clipInfo = getSelectedClipInfo();
        if (clipInfo == null || clipInfo.getClipTags() == null) return;

        for (String tag : clipInfo.getClipTags()) {
            Button tagButton = drawTag(null, tag);
            tagButton.setOnAction(event -> onTagUnselectedInSelectedClip(tagButton));
        }
    }

    private void redrawClips() {

        ClipInfo selectedClip = getSelectedClipInfo();

        clipsTableView.getItems().clear();

        ClipInfoList clipInfos = db.getClipInfos();

        if (clipInfos.size() == 0) return;

        for (int i = clipInfos.size() - 1; i >= 0; i--) {

            if (!searchText.isEmpty() && !clipInfos.get(i).clipboardDataFormats.contains(searchText) &&
                    (clipInfos.get(i).getClipDisplayName() == null ||
                            !clipInfos.get(i).getClipDisplayName().contains(searchText))) continue;

            if (!isTagsSelected(clipInfos.get(i))) continue;

            try {
                addClipItem(clipInfos.get(i),
                        selectedClip != null && selectedClip.equals(clipInfos.get(i)));
            } catch (ClipTableViewRowException e) {
                //errorEvents.onErrorEvent(e);
                ClipInfoList clipInfoList = db.getClipInfos();
                clipInfoList.remove(getSelectedClipInfoIndex());
            }
        }
    }

    private void redrawDisplayName() {

        ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
        if (clipTableViewRow == null) return;

        String displayName = clipTableViewRow.clipInfo.getClipDisplayName();
        if (displayName == null) displayName = "";
        selectedClipDisplayName.setText(displayName);
    }

    private void redrawClipTabs() {

        super.redrawClipTabs(getSelectedClipInfo());
    }

    public void saveClipDataFormats() {
        if (oldClipInfo == null) return;
        boolean isEncrypted = oldClipInfo.getIsEncrypted();
        String decryptPassword = oldClipInfo.getDecryptPassword();
        ClipboardDataFormats decryptedDataFormats = oldClipInfo.decryptedDataFormats;
        if (isEncrypted && (decryptPassword == null || decryptedDataFormats == null)) return;
        if (isEncrypted) oldClipInfo.clipboardDataFormats = decryptedDataFormats;
        String clipInfoHtmlText = oldClipInfo.clipboardDataFormats.getHtml();
        if (clipInfoHtmlText == null) clipInfoHtmlText = "";
        if (!clipInfoHtmlText.equals(richTextView.getHtmlText()))
            oldClipInfo.updateHtmlText(richTextView.getHtmlText());

        String clipInfoPlainText = plainTextView.getText();
        if (clipInfoPlainText == null) clipInfoPlainText = "";

        if (
                !clipInfoPlainText.isEmpty() && !clipInfoPlainText.equals(plainTextView.getText())
                )
            oldClipInfo.updatePlainText(plainTextView.getText());

        if (isEncrypted) oldClipInfo.setEncryption(decryptPassword);
    }

    public void onHtmlTextViewChanged() {
        ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
        if (clipTableViewRow == null || clipTableViewRow.clipInfo == null || clipTableViewRow.clipInfo.getIsEncrypted())
            return;
        clipTableViewRow.clipInfo.updateHtmlText(richTextView.getHtmlText());
        clipTableViewRow.updateClipText();
        clipsTableView.refresh();
    }

    public void onPlainTextViewChanged() {
        ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
        if (clipTableViewRow == null || clipTableViewRow.clipInfo == null || clipTableViewRow.clipInfo.getIsEncrypted())
            return;
        if (plainTextView.getText() == null || plainTextView.getText().isEmpty()) return;
        clipTableViewRow.clipInfo.updatePlainText(plainTextView.getText());
        clipTableViewRow.updateClipText();
        clipsTableView.refresh();
    }

    private void redrawButtonsForSelectedClip() {
        int index = getSelectedRowIndex();
        if (index >= 0 && !isClipControlsEnabled)
            disableEnableClipControlsGroupA(true);
        else if (index < 0 && isClipControlsEnabled)
            disableEnableClipControlsGroupA(false);
    }

    // ##############################################################################
    //                                  Event methods
    // ##############################################################################
    public void onNewSearchClipInput(KeyEvent keyEvent) {

        searchText = searchClip.getText();
        redrawResults();
    }

    public void onSearchTagSelected(ActionEvent dragEvent) {

        String tag = getSelectedTagFromCombo(tagsCombo);

        if (tag == null) return;

        // Add the selected tag to the selected tags array
        Button tagButton = drawTag(tagsArea, selectedTags, tag);

        if (tagButton != null) {
            tagButton.setOnAction(event -> {
                tagsArea.getChildren().remove(tagButton);
                selectedTags.remove(tag);
                redrawResults();
            });

            selectedTags.add(tag);
        }

        redrawResults();
    }

    public void onNewTagInSelectedClip(ActionEvent actionEvent) {
        String tag = getSelectedTagFromCombo(selectedClipTagsCombo);

        if (tag == null) return;

        ClipInfo clipInfo = getSelectedClipInfo();

        if (clipInfo == null) return;

        Button tagButton = drawTag(selectedClipTags, clipInfo.getClipTags(), tag);

        if (tagButton != null) {
            tagButton.setOnAction(event -> onTagUnselectedInSelectedClip(tagButton));
            clipInfo.addTag(tag);
            ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
            if (clipTableViewRow == null) return;
            renderTagList(clipTableViewRow);
            clipsTableView.refresh();
        }
    }

    private void onTagUnselectedInSelectedClip(Button tagButton) {
        ClipInfo clipInfo = getSelectedClipInfo();
        if (clipInfo == null) return;
        clipInfo.removeTag(tagButton.getText());
        removeTag(tagButton);
        redrawResults();
    }

    public void onTableViewKeyPress(KeyEvent keyEvent) {
        onTableViewClick();
    }

    public void onTableViewMouseClick(MouseEvent mouseEvent) {

//        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
//
//        int index = clipsTableView.getSelectionModel().selectedIndexProperty().get();

        onTableViewClick();
    }

    private void onTableViewClick() {

        saveClipDataFormats();

        oldClipInfo = getSelectedClipInfo();

        redrawButtonsForSelectedClip();

        redrawDisplayName();

        redrawClipTabs();

        redrawSelectedTagsForSelectedClip();
    }

    private void onPasteSelectedClip(ClipInfo clipInfo) {
        if (clipInfo == null) return;

        if (clipInfo.clipboardDataFormats.getIsEncrypted() && clipInfo.decryptedDataFormats == null) {
            if (!decryptClipDialog(clipInfo)) return;
        }

        pasteUiEvents.onPasteUserRequest(clipInfo, false);

        if (clipInfo.decryptedDataFormats != null) clipInfo.decryptedDataFormats = null;
    }

    public void onPasteSelectedClip(ActionEvent actionEvent) {
        onPasteSelectedClip(getSelectedClipInfo());
    }

    public void onDeleteSelectedClip(ActionEvent actionEvent) {
        ClipInfoList clipInfos = db.getClipInfos();
        clipInfos.remove(getSelectedClipInfoIndex());
        redrawResults();
    }

    public void onPasteAsPlain(ActionEvent actionEvent) {

        ClipInfo clipInfo = getSelectedClipInfo();
        if (clipInfo == null) return;

        pasteUiEvents.onPasteUserRequest(clipInfo, true);
    }

    public void onClipDisplayNameChange(KeyEvent keyEvent) {

        ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
        if (clipTableViewRow == null) return;
        String newDisplayName = selectedClipDisplayName.getText();
        if (newDisplayName.isEmpty()) newDisplayName = null;
        clipTableViewRow.clipInfo.setClipDisplayName(newDisplayName);
        clipTableViewRow.updateClipText();
        clipsTableView.refresh();
    }

    public void onAutoSaveNextCopiesCheckbox(ActionEvent actionEvent) {
        Settings.setAutoSaveDb(autoSaveNextCopiesCheckbox.isSelected());
    }

    public void onSetEncryptionBtn(ActionEvent actionEvent) {

        ClipTableViewRow clipTableViewRow = getSelectedClipViewRow();
        if (clipTableViewRow == null) return;
        ClipInfo clipInfo = clipTableViewRow.clipInfo;
        if (clipInfo == null) return;

        ClipboardDataFormats clipboardDataFormats = clipInfo.clipboardDataFormats;
        ClipboardDataFormats decryptedDataFormats = null;

        if (clipboardDataFormats.getIsEncrypted()) {
            if (!decryptClipDialog(clipInfo)) return;
            decryptedDataFormats = clipInfo.decryptedDataFormats;
        }

        if (clipInfo.getClipDisplayName() == null) {
            String newDisplayName = getClipDisplayDialog();
            if (newDisplayName == null) return;
            clipInfo.setClipDisplayName(newDisplayName);
        }

        if (decryptedDataFormats != null) {
            clipboardDataFormats = decryptedDataFormats;
            clipInfo.clipboardDataFormats = decryptedDataFormats;
        }

        clipInfo.decryptedDataFormats = new ClipboardDataFormats(clipboardDataFormats);
        decryptedClipInfoList.add(clipInfo);

        String password = newPasswordField.getText().trim();
        if (!password.isEmpty()) {
            clipboardDataFormats.setEncryption(password);
        }

        clipTableViewRow.updateClipText();

        if (clipInfo.getSaveToDb()) clipInfo.sqlUpdateClipboardDataFormats();

        redrawResults();
    }

    public void onStageClosed() {
        for (ClipInfo clipInfo : decryptedClipInfoList) {
            clipInfo.decryptedDataFormats = null;
        }
        decryptedClipInfoList = new ArrayList<>();
    }
    // ##############################################################################
    //                                  Get methods
    // ##############################################################################

    private int getSelectedRowIndex() {
        return clipsTableView.getSelectionModel().getSelectedIndex();
    }

    private ClipTableViewRow getClipViewRow(int index) {
        return (ClipTableViewRow) clipsTableView.getItems().get(index);
    }

    private ClipTableViewRow getSelectedClipViewRow() {

        int index = getSelectedRowIndex();
        if (index == -1) return null;
        return getClipViewRow(index);
    }

    private ClipInfo getClipInfo(int index) {
        return getClipViewRow(index).clipInfo;
    }

    private ClipInfo getSelectedClipInfo() {
        int index = getSelectedRowIndex();
        if (index < 0) return null;
        return getClipInfo(index);
    }

    private int getSelectedClipInfoIndex() {
        return db.getClipInfos().findIndex(getSelectedClipInfo());
    }

    // ##############################################################################
    //                                  Is methods
    // ##############################################################################

    private boolean isTagsSelected(ClipInfo clipInfo) {
        if (selectedTags.size() == 0) return true;
        if (clipInfo.getClipTags() == null) return false;
        for (String tag : selectedTags) {
            if (!clipInfo.getClipTags().contains(tag)) return false;
        }
        return true;
    }

    // ##############################################################################
    //                                  Helper methods
    // ##############################################################################

    private boolean decryptClipDialog(ClipInfo clipInfo) {
        ClipboardDataFormats decryptedDataFormats = new ClipboardDataFormats(clipInfo.clipboardDataFormats);

        while (true) {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter the password");
            dialog.setHeaderText("Please enter the password to continue");
            dialog.setContentText("Password:");

            String password;
            Optional<String> result = dialog.showAndWait();

            if (!result.isPresent()) return false;

            password = result.get().trim();

            if (password.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Password cannot be set empty");
                alert.setContentText("Please enter valid password and try again.");
                alert.showAndWait();
            } else if (!decryptedDataFormats.removeEncryption(password)) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Password is incorrect");
                alert.setContentText("Please enter the correct password and try again.");
                alert.showAndWait();
            } else {
                clipInfo.decryptedDataFormats = decryptedDataFormats;
                clipInfo.setDecryptPassword(password);
                return true;
            }
        }
    }

    private void renderTagList(ClipTableViewRow clipTableViewRow) {
        List<String> tagIds = clipTableViewRow.clipInfo.getClipTags();
        if (tagIds == null || tagIds.size() == 0) {
            clipTableViewRow.setClipTags("");
            return;
        }
        clipTableViewRow.setClipTags(String.join(", ", clipTableViewRow.clipInfo.getClipTags()));
    }

    private void addClipItem(ClipInfo clipInfo, boolean addAsSelected) throws ClipTableViewRowException {
        ClipTableViewRow clipTableViewRow = null;
        clipTableViewRow = new ClipTableViewRow(clipInfo);
        renderTagList(clipTableViewRow);
        clipsTableView.getItems().add(clipTableViewRow);
        if (!addAsSelected) return;
        clipsTableView.getSelectionModel().select(clipsTableView.getItems().size() - 1);
    }

    private void clipsTableSelectRow(int rowIndex) {
        clipsTableView.requestFocus();
        clipsTableView.getSelectionModel().select(rowIndex);
        clipsTableView.getSelectionModel().focus(rowIndex);
    }

    private void disableEnableClipControlsGroupA(boolean flag) {

        isClipControlsEnabled = flag;

        flag = !flag;

        clipInfoArea.setDisable(flag);
        pasteSelectedClip.setDisable(flag);
        deleteSelectedClip.setDisable(flag);
    }

    public void onClipTableKeyPressed(KeyEvent keyEvent) {
        // TODO - make it that when I press enter then it will paste the selected clip
    }
}
