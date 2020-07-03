package org.vaultage.demo.fairnet.desktop;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class PostController implements Initializable {

	private String username;
	private Date datetime;
	private String content;

	@FXML
	private Label labelPostUsername;

	@FXML
	private Label labelPostDateTime;

	@FXML
	private Text labelPostContent;

	public PostController() {

	}

	public PostController(String username, Date datetime, String content) {
		this.username = username;
		this.datetime = datetime;
		this.content = content;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		labelPostUsername.setText(this.username);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
		String datetimeString = formatter.format(this.datetime);
		labelPostDateTime.setText(datetimeString);
		labelPostContent.setText(this.content);
	}
}
