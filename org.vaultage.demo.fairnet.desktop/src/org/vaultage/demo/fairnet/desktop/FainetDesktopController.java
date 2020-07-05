package org.vaultage.demo.fairnet.desktop;

import java.io.IOException;

/**
 * Sample Skeleton for 'FairnetDesktop.fxml' Controller Class
 */

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import org.vaultage.demo.fairnet.FairnetVault;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class FainetDesktopController {

	private FairnetVault fairnet;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Label labelMainName;

	@FXML
	private StackPane stackPane;

	@FXML
	private BorderPane friendsPane;

	@FXML
	private BorderPane notificationsPane;

	@FXML
	private BorderPane profilePane;

	@FXML
	private TextField textFieldID;

	@FXML
	private TextField textFieldName;

	@FXML
	private TextArea textFieldPrivateKey;

	@FXML
	private TextArea textFieldPublicKey;

	@FXML
	private BorderPane postsPane;

	@FXML
	private VBox vBoxPostsPane;
	
	@FXML
	private VBox vBoxNotificationsPane;

	@FXML
	private Button buttonPost;

	@FXML
	private TextArea textAreaPost;

	@FXML
	private VBox vBoxFriendsPane;

	@FXML
	private Button buttonFriendAdd;

	@FXML
	private TextField textFieldFriendName;

	@FXML
	private TextArea textAreaFriendPublicKey;

	public FainetDesktopController() {
		super();
		fairnet = FairnetDesktop.FAIRNET;
	}

	@FXML
	void paneFriendsOnClicked(MouseEvent event) {
		System.out.println("Friends");
		friendsPane.toFront();
	}

	@FXML
	void panePostsOnClicked(MouseEvent event) {
		System.out.println("Posts");
		postsPane.toFront();
	}

	@FXML
	void paneNotificationsOnClicked(MouseEvent event) {
		System.out.println("Notifications");
		notificationsPane.toFront();
	}

	@FXML
	void paneProfileOnClicked(MouseEvent event) {
		System.out.println("Profile");
		profilePane.toFront();
	}

	@FXML
	void buttonUpdateProfileOnAction(ActionEvent event) {
		fairnet.setName(textFieldName.getText());
		labelMainName.setText(fairnet.getName());
	}

	@FXML
	void buttonPostOnAction(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Post.fxml"));
		PostController controller = new PostController(fairnet.getName(), new Date(), textAreaPost.getText());
		loader.setController(controller);
		BorderPane borderPane = (BorderPane) loader.load();
		vBoxPostsPane.getChildren().add(0, borderPane);
	}

	@FXML
	void buttonFriendAddOnAction(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Friend.fxml"));
		FriendController controller = new FriendController(textFieldFriendName.getText(), false);
		loader.setController(controller);
		AnchorPane anchorPane = (AnchorPane) loader.load();
		vBoxFriendsPane.getChildren().add(0, anchorPane);
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		postsPane.toFront();
		textFieldID.setText(fairnet.getId());
		textFieldName.setText(fairnet.getName());
		textFieldPrivateKey.setText(fairnet.getPrivateKey());
		textFieldPublicKey.setText(fairnet.getPublicKey());
		labelMainName.setText(fairnet.getName());
	}

}
