package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.controller.MailController;
import application.service.AdminService;
import application.dao.impl.MailDAO;
import application.model.Mail;
import application.model.Utente;

class MailControllerTest {

    private MailController controller;
    private Utente utenteSuccesso;
    private Utente utenteFail;
    private Utente mittente; // Serve un utente che invia la mail

    // --- MOCK MAIL DAO ---
    class MockMailDAO extends MailDAO {
        @Override
        public boolean scriviMail(Mail mail) {
            // Controlli di base
            if(mail.getDestinatario() == null || mail.getDestinatario().isBlank() ||
               mail.getOggetto() == null || mail.getOggetto().isBlank())
                return false;
            
            // Logica del test: se il destinatario è utenteFail, ritorna false
            if(utenteFail.getMail().equals(mail.getDestinatario()))
                return false;

            return true;
        }
    }

    // --- HELPER REFLECTION PER METODI PRIVATI ---
    private Object invokeTrySendMail(String destinatario, String oggetto, String corpo) throws Exception {
        Method method = MailController.class.getDeclaredMethod("trySendMail", String.class, String.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, destinatario, oggetto, corpo);
    }

    // --- HELPER REFLECTION PER CAMPI PRIVATI ---
    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @BeforeEach
    void setup() throws Exception {
        // 1. Configura i Mock
        AdminService.setMailDAO(new MockMailDAO());
        // Non serve MockUtenteDAO qui perché iniettiamo la mappa manualmente
        
        controller = new MailController();
        
        // 2. Crea gli utenti di test
        // Nota: Assicurati che il costruttore corrisponda alla tua classe Utente. 
        // Ho usato stringhe generiche per i campi non rilevanti.
        utenteSuccesso = new Utente("CF1", "pass", "paziente", "Utente Successo", null, null, null, "successo@test.it", null);
        utenteFail = new Utente("CF2", "pass", "paziente", "Utente Fail", null, null, null, "fail@test.it", null);
        mittente = new Utente("CF_ME", "pass", "diabetologo", "Io Mittente", null, null, null, "me@test.it", null);

        // 3. INIEZIONE MANUALE (La parte fondamentale che mancava)
        
        // Iniettiamo l'utente corrente 'u' nel controller
        injectPrivateField(controller, "u", mittente);

        // Iniettiamo la mappa emailToNameMap popolata
        // Il controller usa questa mappa per verificare se l'email esiste
        Map<String, String> mappaTest = new HashMap<>();
        mappaTest.put(utenteSuccesso.getMail(), utenteSuccesso.getNomeCognome());
        mappaTest.put(utenteFail.getMail(), utenteFail.getNomeCognome());
        
        injectPrivateField(controller, "emailToNameMap", mappaTest);
    }

    @AfterEach
    void tearDown() {
        AdminService.setMailDAO(new MailDAO());
    }

    @Test
    void testMailFailure() {
        try {
            // Questo deve restituire FAILURE perché MockMailDAO restituisce false per questa email
            Object result = invokeTrySendMail(utenteFail.getMail(), "Oggetto Test", "Corpo Test");
            assertEquals("FAILURE", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void testMailSuccess() {
        try {
            // Questo deve restituire SUCCESS
            Object result = invokeTrySendMail(utenteSuccesso.getMail(), "Oggetto Ok", "Tutto ok");
            assertEquals("SUCCESS", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void testInvalidData() {
        try {
            // Questa mail non è nella mappa iniettata -> INVALID_DATA
            Object result = invokeTrySendMail("inesistente@test.it", "Oggetto", "Corpo");
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void testEmptyFields() {
        try {
            Object result = invokeTrySendMail("", "", "");
            assertEquals("EMPTY_FIELDS", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}