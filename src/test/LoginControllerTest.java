package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.controller.LoginController;
import application.service.AdminService;
import application.dao.impl.PazienteDAO;
import application.dao.impl.DiabetologoDAO;
import application.model.Diabetologo;
import application.model.Paziente;

class LoginControllerTest {

    private LoginController controller;

    class MockPazienteDAO extends PazienteDAO {
        @Override
        public Paziente loginPaziente(String cf, String password) {
            // Simuliamo il comportamento
            if ("PAZIENTE_TEST".equals(cf) && "password123".equals(password)) {
                return new Paziente("PAZIENTE_TEST", "password123", "Mario", "Rossi", null, null, null, null, null);
            }
            return null;
        }
    }

    class MockDiabetologoDAO extends DiabetologoDAO {
        @Override
        public Diabetologo loginDiabetologo(String cf, String password) {
            // Simuliamo il comportamento
            if ("DIABETOLOGO_TEST".equals(cf) && "password123".equals(password)) {
                return new Diabetologo("DIABETOLOGO_TEST", "password123", "Luigi", "Vaona", null, null, null, null);
            }
            return null;
        }
    }

    private Object invokeTryLogin(String cf, String password) throws Exception {
        Method method = LoginController.class.getDeclaredMethod("tryLogin", String.class, String.class);
        method.setAccessible(true); // Rende il metodo privato accessibile
        Object result =  method.invoke(controller, cf, password);
        return result;
    }

    @BeforeEach
    void setup() {
        AdminService.setPazienteDAO(new MockPazienteDAO());
        AdminService.setDiabetologoDAO(new MockDiabetologoDAO());
        controller = new LoginController();
    }

    @AfterEach
    void tearDown() {
        AdminService.setPazienteDAO(new PazienteDAO());
        AdminService.setDiabetologoDAO(new DiabetologoDAO());
    }

    @Test
    void testLoginSuccessPaziente() {
        try {
            Object result = invokeTryLogin("PAZIENTE_TEST", "password123");
            result = result.toString();

            assertEquals("SUCCESS_PAZIENTE", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLoginSuccessDiabetologo() {
        try {
            Object result = invokeTryLogin("DIABETOLOGO_TEST", "password123");
            result = result.toString();
            
            assertEquals("SUCCESS_DIABETOLOGO", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testWrongCredentials() {
        try {
            Object result = invokeTryLogin("PAZIENTE_TEST", "passwordSbagliata");
            result = result.toString();
            
            assertEquals("WRONG_CREDENTIALS", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEmptyFields() {
        try {
            Object result = invokeTryLogin("", "");
            result = result.toString();
            
            assertEquals("EMPTY_FIELDS", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}