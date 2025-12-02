package application;

import application.service.AdminService;
import application.view.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	public void init() throws Exception {
		// Carico inizialmente solo gli utenti dal database per fare le liste pazienti e diabetologi
		AdminService.loadAllUtenti();
	}
	
	public void start(Stage primaryStage) {
		try {
            Navigator.getInstance().setStage(primaryStage);
            Navigator.getInstance().switchToLogin(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}