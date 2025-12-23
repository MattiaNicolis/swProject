package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.controller.MailController;
import application.service.AdminService;
import application.dao.impl.MailDAO;
import application.dao.impl.UtenteDAO;
import application.model.Mail;
import application.model.Utente;

class MailControllerTest {

    private MailController controller;
    private Utente utenteSuccesso;
    private Utente utenteFail;

    class MockMailDAO extends MailDAO {
        @Override
        public boolean scriviMail(Mail mail) {
            // Simuliamo il comportamento
            if("".equals(mail.getDestinatario()) || "".equals(mail.getOggetto())
                || " ".equals(mail.getDestinatario()) || " ".equals(mail.getOggetto()))
                return false;
            if(utenteFail.getMail().equals(mail.getDestinatario()))
                return false;

            return true;
        }
    }

    class MockUtenteDAO extends UtenteDAO {
        @Override
        public List<Utente> getPeopleByRole(String role) {
            utenteSuccesso = new Utente("a", "a", role, "Forte Debole", null, null, null, "forte.debole@glicocare.it", null);
            utenteFail = new Utente("b", "b", role, "Piango Sorrido", null, null, null, "piango.sorrido@glicocare.it", null);
            List<Utente> list = new ArrayList<>();

            list.add(utenteSuccesso);
            list.add(utenteFail);

            return list;
        }
    }

    private Object invokeTrySendMail(String destinatario, String oggetto, String corpo) throws Exception {
        Method method = MailController.class.getDeclaredMethod("trySendMail", String.class, String.class, String.class);
        method.setAccessible(true); // Rende il metodo privato accessibile
        Object result =  method.invoke(controller, destinatario, oggetto, corpo);
        return result;
    }

    @BeforeEach
    void setup() {
        AdminService.setMailDAO(new MockMailDAO());
        controller = new MailController();
    }

    @AfterEach
    void tearDown() {
        AdminService.setMailDAO(new MailDAO());
    }

    @Test
    void testMailFailure() {
        try {
            Object result = invokeTrySendMail(utenteFail.getMail(), "sdivn", "fjv");
            result = result.toString();

            assertEquals("FAILURE", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testMailSuccess() {
        try {
            Object result = invokeTrySendMail(utenteSuccesso.getMail(), "adljkf", "sdkjvd");
            result = result.toString();
            
            assertEquals("SUCCESS", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testInvalidData() {
        try {
            Object result = invokeTrySendMail("mail.nonEsistente@glicocare.it", "dkjcn", "kadjf");
            result = result.toString();
            
            assertEquals("INVALID_DATA", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testEmptyFields() {
        try {
            Object result = invokeTrySendMail("", "", "");
            result = result.toString();
            
            assertEquals("EMPTY_FIELDS", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}