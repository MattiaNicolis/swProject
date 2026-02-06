package application.view;

import java.io.IOException;

import application.controller.MailController;
import application.utils.MessageUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Navigator {
	
	// Singleton
    private static Navigator instance;

    private Stage stage; // riferimento alla finestra principale

    private Navigator() {}

    public static Navigator getInstance() {
        if (instance == null)
            instance = new Navigator();
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
		stage.setTitle("Software per pazienti diabetici");
		Image icon = new Image(getClass().getResourceAsStream("/risorse/images/logo.png"));
		stage.getIcons().add(icon);
    }
	
	//---------------------------------------------------------------
	public void switchToLogin(ActionEvent event) throws IOException {
		loadScene("/risorse/fxml/Login.fxml");
	}

	//-------------------------------------------------------------------------
	public void switchToDiabetologoPage(ActionEvent event) throws IOException {
		loadScene("/risorse/fxml/DiabetologoPage.fxml");
	}
			
	//----------------------------------------------------------------------
	public void switchToPazientePage(ActionEvent event) throws IOException {
		loadScene("/risorse/fxml/PazientePage.fxml");
	}
	
	//------------------------------------------------------------------------------------
	public void switchToMostraDatiPaziente(Event event) throws IOException {
		loadScene("/risorse/fxml/MostraDatiPaziente.fxml");
	}
	
	public void switchToMostraPatologia(Event event) throws IOException {
		loadScene("/risorse/fxml/PatologiaPregressa.fxml");
	}
	
	public void switchToMostraTerapiaConcomitante(Event event) throws IOException {
		loadScene("/risorse/fxml/TerapiaConcomitante.fxml");
	}
	
	//------------------------------------------------------------------
	public void switchToMailPage(Event event) throws IOException {
		loadScene("/risorse/fxml/MailPage.fxml");
	}
	
	public void switchToVediMail(Event event) throws IOException {
		loadScene("/risorse/fxml/VediMail.fxml");
	}
	
	public void switchToRispondi(Event event, String mail, String oggetto) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/risorse/fxml/MailPage.fxml"));
        Parent root = loader.load();
		MailController mailController = loader.getController();
		mailController.rispondi(mail, oggetto);
        setScene(root);
	}
	
	//--------------------------------------------------------------------------
	public void switchToQuestionarioPage(ActionEvent event) throws IOException {
		loadScene("/risorse/fxml/QuestionarioPage.fxml");
	}

	public void switchVediQuestionario(Event event) throws IOException {
		loadScene("/risorse/fxml/VediQuestionario.fxml");
	}
	
	//----------------------------------------------------------------------
	public void switchToTerapia(ActionEvent event) throws IOException {
		loadScene("/risorse/fxml/NuovaTerapia.fxml");
	}
	
	public void switchToMostraDettagliTerapia(Event event) throws IOException {
		loadScene("/risorse/fxml/MostraDettagliTerapia.fxml");
	}

	//----------------------------------------------------------------------------
	public void switchToStoriaDatiPaziente(ActionEvent event) throws IOException {
		loadScene("/risorse/fxml/StoriaDatiPaziente.fxml");
	}
	
	// -----------------------------------------------------------------
    private void loadScene(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent root = loader.load();
        setScene(root);
    }
	
	//-----------------------------------------------
	private void setScene(Parent root) throws IOException {
		if(stage == null) {
			MessageUtils.showError("Stage non impostato.");
		}
		else {
			stage.setScene(new Scene(root));
			stage.show();
			
			Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			stage.setX(screenBounds.getMinX());
			stage.setY(screenBounds.getMinY());
			stage.setWidth(screenBounds.getWidth());
			stage.setHeight(screenBounds.getHeight());
			stage.setMaximized(true);
		}
    }
}
