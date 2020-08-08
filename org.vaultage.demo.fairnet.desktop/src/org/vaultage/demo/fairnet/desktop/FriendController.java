package org.vaultage.demo.fairnet.desktop;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class FriendController implements Initializable {

	private String friendUsername;
	private boolean isConfirmed = false;

	@FXML
	private Label labelFriendUsername;

	@FXML
	private Label labelFriendStatus;

	@FXML
	private Button buttonFriendRemove;

	public FriendController() {

	}

	public FriendController(String friendUsername, boolean isConfirmed) {
		this.friendUsername = friendUsername;
		this.isConfirmed = isConfirmed;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.labelFriendUsername.setText(this.friendUsername);
		if (isConfirmed) {
			labelFriendStatus.setText("");
		} else {
			labelFriendStatus.setText("Requested");
		}
	}

	@FXML
	void buttonFriendRemoveOnAction(ActionEvent event) {
		Button button = (Button)event.getSource();
		AnchorPane anchorPane = (AnchorPane) button.getParent();
		VBox vBox = (VBox) anchorPane.getParent();
		vBox.getChildren().remove(anchorPane);
	}
}
