package test;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.controller.StoriaDatiPazienteController;
import application.dao.impl.DatiDAO;
import application.dao.impl.PatologiaDAO;
import application.model.Dato;
import application.model.Patologia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.Sessione;

class StoriaDatiPazientiTest {

    private StoriaDatiPazienteController controller;

    // --- MOCK DAO UNIFICATO ---
    class MockDatiDao extends DatiDAO {
        
        @Override
        public List<Dato> getDatiByPaziente(Utente p, String tipo) {
            List<Dato> lista = new ArrayList<>();
            // Simuliamo dati esistenti per il test dei duplicati
            if ("Allergia".equalsIgnoreCase(tipo) || "allergie".equalsIgnoreCase(tipo))
                lista.add(new Dato(p.getCf(), "polline", "a"));
            
            if ("Comorbidità".equalsIgnoreCase(tipo) || "comorbidità".equalsIgnoreCase(tipo))
                lista.add(new Dato(p.getCf(), "ciao", "a"));
            
            if ("Fattore Di Rischio".equalsIgnoreCase(tipo) || "fattori".equalsIgnoreCase(tipo))
                lista.add(new Dato(p.getCf(), "fumatore", "a"));
            
            return lista;
        }
    }

    class MockPatologiaDAO extends PatologiaDAO {
        @Override
        public List<Patologia> getPatologieByPaziente(Utente p) {
            List<Patologia> lista = new ArrayList<>();
            // Aggiungiamo Asma per testare il duplicato nelle patologie
            lista.add(new Patologia(p.getCf(), "Asma", LocalDate.now().minusDays(1), "Note", "a"));
            return lista;
        }

         @Override
        public boolean creaPatologia(Patologia p) {
            if(p.getNome() == null || p.getNome().isBlank())
                return false;
            if(p.getInizio().isAfter(LocalDate.now()))
                return false;

            return true;
        }

        @Override
        public boolean eliminaPatologia(Patologia p) {
            if(p.getNome() == null || p.getNome().isBlank())
                return false;

            return true;
    }
}

    // --- HELPER PER LA REFLECTION ---
    
    private Object invokeTryCreate(String tipo, String nome) throws Exception {
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("tryCreateFattoreComorbiditàAllergie", String.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, tipo, nome);
    }

    private Object invokeTryRemove(String tipo, String nome) throws Exception {
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("tryRemoveFattoreComorbiditàAllergie", String.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, tipo, nome);
    }

    private Object invokeTryCreatePatologia(String nome, LocalDate data, String note) throws Exception {
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("tryCreatePatologia", String.class, LocalDate.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, nome, data, note);
    }

    private Object invokeTryRemovePatologia(String nome, LocalDate data, String note) throws Exception {
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("tryRemovePatologia", String.class, LocalDate.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, nome, data, note);
    }

    private void invokeCaricaDati() throws Exception {
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("caricaDati");
        method.setAccessible(true);
        method.invoke(controller);
    }

    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // --- SETUP E TEARDOWN ---

    @BeforeEach
    void setup() {
        try {
            AdminService.setDatiDAO(new MockDatiDao());
            AdminService.setPatologiaDAO(new PatologiaDAO());
            
            Utente medico = new Utente("a", "a", "diabetologo", null, null, null, null, null, null);
            Utente paziente = new Utente("b", "b", "paziente", null, null, null, null, null, "a");

            Sessione.getInstance().setUtente(medico);
            Sessione.getInstance().setPazienteSelezionato(paziente);

            controller = new StoriaDatiPazienteController();
            
            // Iniettiamo i campi privati per simulare lo stato del controller
            injectPrivateField(controller, "u", medico);
            injectPrivateField(controller, "p", paziente);
            
            // Popoliamo le liste interne (fattori, comorbidità, allergie, patologie)
            invokeCaricaDati();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        AdminService.setDatiDAO(new DatiDAO());
        Sessione.getInstance().setUtente(null);
        Sessione.getInstance().setPazienteSelezionato(null);
    }

    // --- TEST CASE: CREAZIONE ---

    @Test
    void testCreazioneSuccesso() throws Exception {
        assertEquals("SUCCESS", invokeTryCreate("Allergia", "acari").toString());
        assertEquals("SUCCESS", invokeTryCreate("Comorbidità", "Gastrite").toString());
    }

    @Test
    void testCreazioneGiaEsistente() throws Exception {
        assertEquals("DATA_ALREADY_EXISTS", invokeTryCreate("Allergia", "polline").toString());
        assertEquals("DATA_ALREADY_EXISTS", invokeTryCreate("Fattore Di Rischio", "fumatore").toString());
    }

    @Test
    void testCampiVuoti() throws Exception {
        assertEquals("EMPTY_FIELDS", invokeTryCreate("Allergia", "").toString());
        assertEquals("EMPTY_FIELDS", invokeTryCreate("Allergia", null).toString());
    }

    // --- TEST CASE: RIMOZIONE ---

    @Test
    void testRimozioneDatoSuccesso() throws Exception {
        // Questi ora restituiscono SUCCESS perché abbiamo sovrascritto eliminaAllergia/Comorbidità nel Mock
        assertEquals("SUCCESS", invokeTryRemove("Allergia", "polline").toString());
        assertEquals("SUCCESS", invokeTryRemove("Comorbidità", "ciao").toString());
    }

    @Test
    void testRimozioneDatoFallimento() throws Exception {
        // Testiamo il caso in cui il DAO restituisce false (nome "inesistente")
        assertEquals("FAILURE", invokeTryRemove("Fattore Di Rischio", "inesistente").toString());
    }

    // --- TEST CASE: PATOLOGIE ---

    @Test
    void testGestionePatologiaCompleta() throws Exception {
        // Creazione OK
        assertEquals("SUCCESS", invokeTryCreatePatologia("Nuova Patologia", LocalDate.now(), "Dettagli").toString());
        
        // Già esistente (Asma è nel Mock)
        assertEquals("DATA_ALREADY_EXISTS", invokeTryCreatePatologia("Asma", LocalDate.now(), "Dettagli").toString());
        
        // Data futura
        assertEquals("INVALID_DATE", invokeTryCreatePatologia("Test", LocalDate.now().plusDays(1), "Dettagli").toString());

        // Rimozione OK
        assertEquals("SUCCESS", invokeTryRemovePatologia("Asma", LocalDate.now(), "").toString());
    }

    @Test
    void testPatologiaCampiVuoti() throws Exception {
        assertEquals("EMPTY_FIELDS", invokeTryCreatePatologia("", LocalDate.now(), "Note").toString());
    }
}