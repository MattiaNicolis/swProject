package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.controller.PazienteController;
import application.dao.impl.PesoDAO;
import application.model.Peso;
import application.model.Utente;
import application.service.AdminService;

public class PazientePesoTest {
    
    private PazienteController controller;
    private Utente paziente; // Variabile di classe per riutilizzarla

    class MockPesoDAO extends PesoDAO {
        @Override
        public boolean creaPeso(Peso p) {
            // Simuliamo il successo se il valore è 100
            if(100 == p.getValore())
                return true;
            return false;
        }

        @Override
        public boolean aggiornaPeso(Peso p) {
            if(101 == p.getValore())
                return true;
            return false;
        }
    }

    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokeTryCreatePeso(String valore, boolean aggiorna) throws Exception {
        Method method = PazienteController.class.getDeclaredMethod("tryCreatePeso", String.class, boolean.class);
        method.setAccessible(true);
        return method.invoke(controller, valore, aggiorna);
    }

    @BeforeEach
    void setup() throws Exception {
        AdminService.setPesoDAO(new MockPesoDAO());
        
        controller = new PazienteController();
        
        // 1. Creiamo un utente fittizio
        paziente = new Utente("CF_TEST", "pass", "paziente", "Nome Cognome", null, null, null, "mail@test.it", null);

        // 2. INIETTIAMO L'UTENTE 'p' (Questo risolve il NullPointerException su p.getCf())
        injectPrivateField(controller, "p", paziente);

        // 3. INIETTIAMO LA LISTA 'peso' (Questo risolve il NullPointerException sullo stream/filter)
        // Inizializziamo una lista vuota di default
        injectPrivateField(controller, "peso", new ArrayList<Peso>());
    }

    @AfterEach
    void tearDown() {
        AdminService.setPesoDAO(new PesoDAO());
    }

    @Test
    @DisplayName("EMPTY FIELDS CASE")
    void testEmptyFields() {
        try {
            Object result = invokeTryCreatePeso("", false);
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
            Object result = invokeTryCreatePeso("a", false);
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) { e.printStackTrace(); }

        try {
            Object result = invokeTryCreatePeso("1000", false);
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) { e.printStackTrace(); }

        try {
            Object result = invokeTryCreatePeso("0", false);
            assertEquals("INVALID_DATA", result.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Test
    @DisplayName("FAILURE CASE")
    void testFailure() {
        try {
            // Valore 1 -> Mock ritorna false -> FAILURE
            Object result = invokeTryCreatePeso("1", false);
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
            // Valore 100 -> Mock ritorna true -> SUCCESS
            Object result = invokeTryCreatePeso("100", true); 
            assertEquals("SUCCESS", result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("ALREADY INSERT CASE")
    void testAlreadyInsert() {
        // Qui sovrascriviamo la lista "peso" iniettata nel setup con una che contiene dati
        List<Peso> listaSimulata = new ArrayList<>();
        listaSimulata.add(new Peso(1, paziente.getCf(), 75.0, LocalDate.now())); // Data odierna

        try {
            // Re-iniettiamo la lista popolata
            injectPrivateField(controller, "peso", listaSimulata);

            // Proviamo a inserire un nuovo peso (80.0) senza forzare l'aggiornamento (false)
            Object result = invokeTryCreatePeso("80.0", false);

            assertEquals("ALREADY_INSERT", result.toString());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}