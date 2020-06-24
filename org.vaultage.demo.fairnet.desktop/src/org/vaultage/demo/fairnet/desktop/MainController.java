package org.vaultage.demo.fairnet.desktop;

/**
 * Sample Skeleton for 'Main.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class MainController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private Label labelTopPaneTitle;

    
    
    @FXML
    void panelFriendsOnClicked(MouseEvent event) {
    	labelTopPaneTitle.setText("Friends");
    }

    @FXML
    void panelPostsOnClicked(MouseEvent event) {
    	labelTopPaneTitle.setText("Posts");
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

    }
}
