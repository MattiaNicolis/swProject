package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach; // Cambiato da BeforeAll
import org.junit.jupiter.api.Test;

import application.controller.LoginController;
import application.controller.LoginController.LoginResult;
import application.model.Utente;
import application.service.AdminService;

class LoginControllerTest {

    // Pulisce la lista prima di OGNI test per evitare dati residui
    @BeforeEach
    void setup() {
        AdminService.utenti.clear();
    }
    
    @Test
    void testEmptyFields() {
        LoginController controller = new LoginController();
        LoginResult result = controller.tryLogin("", "");
        assertEquals(LoginResult.EMPTY_FIELDS, result);
    }
    
    @Test
    void testNullFields() {
        LoginController controller = new LoginController();
        LoginResult result = controller.tryLogin(null, null);
        assertEquals(LoginResult.EMPTY_FIELDS, result);
    }

    @Test
    void testUserNotFound() {
        LoginController controller = new LoginController();
        LoginResult result = controller.tryLogin("f", "pwd");
        assertEquals(LoginResult.USER_NOT_FOUND, result);
    }

    @Test
    void testWrongPassword() {
        // Setup dati finti per questo test
        Utente user = new Utente("a", "a", "paziente", "Nome Cognome", null, "M", null, null, null);
        AdminService.utenti.add(user);
        
        LoginController controller = new LoginController();
        LoginResult result = controller.tryLogin("a", "b"); // Password errata
        assertEquals(LoginResult.WRONG_CREDENTIALS, result);
    }

    @Test
    void testSuccessPaziente() {
        Utente user = new Utente("a", "a", "paziente", "Nome Cognome", null, "M", null, null, null);
        AdminService.utenti.add(user);
        
        LoginController controller = new LoginController();
        LoginResult result = controller.tryLogin(user.getCf(), user.getPw());
        assertEquals(LoginResult.SUCCESS_PAZIENTE, result);
    }

    @Test
    void testSuccessDiabetologo() {
         Utente user = new Utente("b", "b", "diabetologo", "Nome Cognome", null, "M", null, null, null);
         AdminService.utenti.add(user);
         
        LoginController controller = new LoginController();
        LoginResult result = controller.tryLogin(user.getCf(), user.getPw());
        assertEquals(LoginResult.SUCCESS_DIABETOLOGO, result);
    }
}