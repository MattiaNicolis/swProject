package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.controller.QuestionarioController;
import application.dao.impl.QuestionarioDAO;
import application.dao.impl.TerapiaDAO;
import application.model.Questionario;
import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;

public class QuestionarioTest {

    private QuestionarioController controller;
    private Terapia terapia;
    private Utente paziente;

    class MockQuestionarioDAO extends QuestionarioDAO {

        public boolean creaQuestionario(Questionario q) {
            if("farmacoTest".equals(q.getNomeFarmaco())) 
                return true;

            return false;
        }
    }

    class MockTerapiaDAO extends TerapiaDAO {

        public boolean aggiornaNumQuestionari(Terapia t) {
            if("farmacoTest".equals(t.getNomeFarmaco())) 
                return true;

            return false;
        }
    }

    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokeTryCreateQuestionario(String nomeFarmaco, String dose, String quantità, String sintomi) throws Exception {
        Method method = QuestionarioController.class.getDeclaredMethod("tryCreateQuestionario", String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, nomeFarmaco, dose, quantità, sintomi);
    }

    @BeforeEach
    void setup() throws Exception {
        AdminService.setQuestionarioDAO(new MockQuestionarioDAO());
        AdminService.setTerapiaDAO(new MockTerapiaDAO());
        
        controller = new QuestionarioController();

        terapia = new Terapia(1, "a", "farmacoTest", 0, 0, null, null, null, null, false, 0);
        injectPrivateField(controller, "t", terapia);

        paziente = new Utente("a", "a", "paziente", null, null, null, null, null, null);
        injectPrivateField(controller, "p", paziente);
    }

    @AfterEach
    void tearDown() throws Exception {
        AdminService.setQuestionarioDAO(new QuestionarioDAO());
        AdminService.setTerapiaDAO(new TerapiaDAO());
    }

    @Test
    @DisplayName("EMPTY FIELDS CASE")
    void testEmptyFields() {
        try {
            Object result = invokeTryCreateQuestionario("", "2", "", "Mal di testa");
            assertEquals("EMPTY_FIELDS", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("INVALID DATA CASE")
    void testInvalidData() {
        try {
            Object result = invokeTryCreateQuestionario("Farmaco1", "a", "b", "Mal di testa");
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            Object result = invokeTryCreateQuestionario("Farmaco1", "0", "-1", "Mal di testa");
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("SUCCESS CASE")
    void testSuccess() {
        try{
            Object result = invokeTryCreateQuestionario("farmacoTest", "2", "4", ":)");
            assertEquals("SUCCESS", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("FAIL CASE")
    void testFailure() {
        try{
            Object result = invokeTryCreateQuestionario("errore_database", "2", "4", ":)");
            assertEquals("FAILURE", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}