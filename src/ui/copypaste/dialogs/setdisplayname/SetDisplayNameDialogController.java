package ui.copypaste.dialogs.setdisplayname;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;

public class SetDisplayNameDialogController extends Dialog<String> {

    @FXML
    public TextArea clipDisplayNameField;

    public String getDisplayName() {
        return clipDisplayNameField.getText().trim();
    }

}
