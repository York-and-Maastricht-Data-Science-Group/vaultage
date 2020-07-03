package org.vaultage.demo.fairnet.desktop;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

import org.vaultage.core.VaultageServer;
import org.vaultage.demo.fairnet.FairnetBroker;
import org.vaultage.demo.fairnet.FairnetVault;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class FairnetDesktop extends Application {

	public static FairnetBroker BROKER;
	private static final String BROKER_ADDRESS = "tcp://localhost:61616";

	public static String HOME_DIR = "D:\\A-DATA\\GoogleDriveYork\\Responsible Data by Design\\vaultage\\org.vaultage.demo.fairnet.desktop\\";
	public static String VAULT_DIR;
	public static String ID;
	public static FairnetVault FAIRNET;

	public static void main(String[] args) throws Exception {
		try {
			BROKER = new FairnetBroker();
			BROKER.start(BROKER_ADDRESS);
		} catch (Exception e) {
		}

		ID = args[0];

		// dealing with persistence, do this later
		// VAULT_DIR = HOME_DIR + File.separator + ID;
		// check if profile exists

		VaultageServer vaultageServer = new VaultageServer(BROKER_ADDRESS);
		FairnetVault vault = new FairnetVault();
		vault.setId(ID);
		vault.setName(ID);
		
		if (vault.register(vaultageServer)){
			System.out.println("Connected!");
		}

		FAIRNET = vault;
		
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("FairNet");
			AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("FairnetDesktop.fxml"));
			Scene scene = new Scene(root, root.getWidth(), root.getHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		
//			FXMLLoader loader = new FXMLLoader(getClass().getResource("Post.fxml"));
//			PostController controller = new PostController();
//	        loader.setController(controller);
//	        BorderPane borderPane =  (BorderPane) loader.load();
//	        BorderPane borderPane = (BorderPane) FXMLLoader.load(getClass().getResource("Post.fxml"));
//	        Scene newScene = new Scene(borderPane);
//	        newScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//	        Stage newStage = new Stage();
//	        newStage.setTitle("Post");
//	        newStage.setScene(newScene);
//	        newStage.show();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
