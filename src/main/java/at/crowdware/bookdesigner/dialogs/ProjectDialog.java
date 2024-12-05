package at.crowdware.bookdesigner.dialogs;

import at.crowdware.bookdesigner.Messages;
import at.crowdware.bookdesigner.model.ProjectData;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class ProjectDialog
	extends Dialog<ProjectData> {

	public ProjectDialog(Window owner) {
		setTitle(Messages.get("ProjectDialog.title"));
		initOwner(owner);
		setResizable(true);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10));

		Label nameLabel = new Label("Name:");
		TextField nameField = new TextField();
		grid.add(nameLabel, 0, 0);
		grid.add(nameField, 1, 0);

		Label pathLabel = new Label("Path:");
		TextField pathField = new TextField();
		Button folderButton = new Button("?");
		folderButton.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Choose folder");
			File selectedDirectory = directoryChooser.showDialog(owner);
			if (selectedDirectory != null) {
				pathField.setText(selectedDirectory.getAbsolutePath());
			}
		});

		grid.add(pathLabel, 0, 1);
		grid.add(pathField, 1, 1);
		grid.add(folderButton, 2, 1);

		DialogPane dialogPane = getDialogPane();
		dialogPane.setContent(grid);
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		setResultConverter(button -> {
			if (button == ButtonType.OK) {
				return new ProjectData(nameField.getText(), pathField.getText());
			}
			return null;
		});
		getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
			setResult(null);
			closeDialog();
		});
	}

	private void closeDialog() {
		getDialogPane().getScene().getWindow().hide();
	}
}
