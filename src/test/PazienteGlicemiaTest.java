package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList; // Import necessario
import java.util.List;      // Import necessario

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.controller.PazienteController;
import application.dao.impl.GlicemiaDAO;
import application.model.Glicemia;
import application.model.Utente;
import application.service.AdminService;

public class PazienteGlicemiaTest {
    
    private PazienteController controller;
    private Utente paziente;

    // --- MOCK DAO ---
    class MockGlicemiaDAO extends GlicemiaDAO {
        @Override
        public boolean creaGlicemia(Glicemia g) {
            // Se il valore è 100, simuliamo il successo
            if(100 == g.getValore())
                return true;
            return false;
        }

        @Override
        public List<Glicemia> getGlicemiaByPaziente(Utente p) {
            return new ArrayList<>(); 
        }
    }

    // --- HELPER REFLECTION ---
    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokeTryCreateGlicemia(String valore, String ora, String minuti, String indicazioni) throws Exception {
        Method method = PazienteController.class.getDeclaredMethod("tryCreateGlicemia", String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, valore, ora, minuti, indicazioni);
    }

    // --- SETUP ---
    @BeforeEach
    void setup() throws Exception {
        AdminService.setGlicemiaDAO(new MockGlicemiaDAO());
        
        controller = new PazienteController();
        paziente = new Utente("CF_PAZIENTE", "pass", "paziente", "Nome Cognome", null, null, null, "mail@test.it", null);

        // 1. Iniettiamo il paziente 'p'
        injectPrivateField(controller, "p", paziente);

        // 2. CORREZIONE IMPORTANTE:
        // Il controller probabilmente prova ad aggiungere il dato a una lista interna dopo il successo.
        // Dobbiamo inizializzare questa lista manualmente perché initialize() non gira.
        // VERIFICA CHE NEL TUO CONTROLLER LA VARIABILE SI CHIAMI "glicemie"
        try {
            List<Glicemia> listaVuota = new ArrayList<>();
            injectPrivateField(controller, "glicemia", listaVuota); 
        } catch (NoSuchFieldException e) {
            // Se la variabile non si chiama "glicemie", ignora o prova un altro nome
            // System.out.println("Attenzione: campo 'glicemie' non trovato nel controller.");
        }
    }

    @AfterEach
    void tearDown() {
        AdminService.setGlicemiaDAO(new GlicemiaDAO());
    }

    // --- TESTS ---

    @Test
    @DisplayName("EMPTY FIELDS CASE")
    void testEmptyFields() {
        try {
            Object result = invokeTryCreateGlicemia("", "13", "", "");
            assertEquals("EMPTY_FIELDS", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("INVALID DATA CASE")
    void testInvalidData() {
        // Caso caratteri non numerici
        try {
            Object result = invokeTryCreateGlicemia("a", "13", "45", "Pre pasto");
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Caso minuti errati
        try {
            Object result = invokeTryCreateGlicemia("143", "13", "60", "Pre pasto");
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Caso ora errata
        try {
            Object result = invokeTryCreateGlicemia("143", "24", "14", "Pre pasto");
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("FAILURE CASE")
    void testFailure() {
        try {
            // Valore 50 -> Il Mock ritorna false -> FAILURE
            Object result = invokeTryCreateGlicemia("50", "22", "14", "Pre pasto");
            assertEquals("FAILURE", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("SUCCESS CASE")
    void testSuccess() {
        try {
            // Eseguiamo il metodo logico.
            Object result = invokeTryCreateGlicemia("100", "22", "14", "Pre pasto");
            
            assertEquals("SUCCESS", result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            // Stampa l'errore completo se fallisce ancora
            fail("Eccezione inattesa: " + e.toString());
        }
    }
}