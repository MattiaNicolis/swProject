package application;

import application.view.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	public void init() throws Exception {}
	
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