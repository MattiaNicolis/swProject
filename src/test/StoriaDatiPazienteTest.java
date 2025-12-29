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
import application.dao.impl.TerapiaConcomitanteDAO;
import application.model.Dato;
import application.model.Patologia;
import application.model.TerapiaConcomitante;
import application.model.Utente;
import application.service.AdminService;

class StoriaDatiPazientiTest {

    private StoriaDatiPazienteController controller;
    Dato allergia;
    Dato fattore;
    Dato comorbidità;

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

        @Override
        public boolean creaDato(Dato d, String tipo) {
            // simuliamo la creazione 
            if ("Allergia".equalsIgnoreCase(tipo) || "allergie".equalsIgnoreCase(tipo)) {
                allergia = new Dato(d.getCF(), "polline", d.getModificato());
                return true;
            }
            
            if ("Comorbidità".equalsIgnoreCase(tipo) || "comorbidità".equalsIgnoreCase(tipo)) {
                comorbidità = new Dato(d.getCF(), "ciao", d.getModificato());
                return true;
            }
            
            if ("Fattore Di Rischio".equalsIgnoreCase(tipo) || "fattori".equalsIgnoreCase(tipo)) {
                fattore = new Dato(d.getCF(), "fact", d.getModificato());
                return true;
            }
            
            return false;
        }

        @Override
        public boolean eliminaDato(Dato d, String tipo) {
            // Simuliamo l'eliminazione

            if ("Allergia".equalsIgnoreCase(tipo) || "allergie".equalsIgnoreCase(tipo)) {
                boolean isEsistente = "polline".equalsIgnoreCase(d.getNome());
                boolean isNuovo = (allergia != null && allergia.getNome().equals(d.getNome()));
                
                return isEsistente || isNuovo;
            }
            
            if ("Comorbidità".equalsIgnoreCase(tipo) || "comorbidità".equalsIgnoreCase(tipo)) {
                boolean isEsistente = "ciao".equalsIgnoreCase(d.getNome());
                boolean isNuovo = (comorbidità != null && comorbidità.getNome().equals(d.getNome()));

                return isEsistente || isNuovo;
            }
            
            if ("Fattore Di Rischio".equalsIgnoreCase(tipo) || "fattori".equalsIgnoreCase(tipo)) {
                boolean isEsistente = "fumatore".equalsIgnoreCase(d.getNome());
                boolean isNuovo = (fattore != null && fattore.getNome().equals(d.getNome()));
                
                return isEsistente || isNuovo;
            }
            
            return false;
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

            if ("Asma".equalsIgnoreCase(p.getNome())) {
                return false; 
            }

            return true;
        }

        @Override
        public boolean eliminaPatologia(Patologia p) {
            if(p.getNome() == null || p.getNome().isBlank())
                return false;

            return true;
        }
    }

    class MockTerapiaConcomitanteDAO extends TerapiaConcomitanteDAO {
        @Override
        public List<TerapiaConcomitante> getTerapieConcomitantiByPaziente(Utente p) {
            List<TerapiaConcomitante> lista = new ArrayList<>();
            // TC1 inizia IERI (minusDays(1))
            lista.add(new TerapiaConcomitante(p.getCf(), "TC1", LocalDate.now().minusDays(1), LocalDate.now().plusDays(10), "b"));
            return lista;
        }

         @Override
        public boolean creaTerapiaConcomitante(TerapiaConcomitante tc) {
            if(tc.getNome() == null || tc.getNome().isBlank()) return false;
            if(tc.getDataInizio() == null || tc.getDataFine() == null) return false;
            if(tc.getDataFine().isBefore(tc.getDataInizio())) return false;

            // Se proviamo a creare TC1, il DB direbbe che esiste già (anche se il controller lo blocca prima)
            if ("TC1".equalsIgnoreCase(tc.getNome())) return false; 

            return true;
        }

        @Override
        public boolean eliminaTerapiaConcomitante(TerapiaConcomitante tc) {
            // Restituisce true SOLO se stiamo eliminando la terapia che sappiamo esistere
            if ("TC1".equalsIgnoreCase(tc.getNome())) {
                return true;
            }
            return false;
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

    private Object invokeTryCreateTerapiaConcomitante(String nome, LocalDate dataInizio, LocalDate dataFine) throws Exception {
        // CORRETTO: Solo 3 parametri, come nel Controller
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("tryCreateTerapiaConcomitante", String.class, LocalDate.class, LocalDate.class);
        method.setAccessible(true);
        return method.invoke(controller, nome, dataInizio, dataFine);
    }

    private Object invokeTryRemoveTerapia(String nome, LocalDate dataInizio, LocalDate dataFine) throws Exception {
        // CORRETTO: Solo 3 parametri
        Method method = StoriaDatiPazienteController.class.getDeclaredMethod("tryRemoveTerapia", String.class, LocalDate.class, LocalDate.class);
        method.setAccessible(true);
        return method.invoke(controller, nome, dataInizio, dataFine);
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
            AdminService.setPatologiaDAO(new MockPatologiaDAO());
            AdminService.setTerapiaConcomitanteDAO(new MockTerapiaConcomitanteDAO());
            
            Utente medico = new Utente("a", "a", "diabetologo", null, null, null, null, null, null);
            Utente paziente = new Utente("b", "b", "paziente", null, null, null, null, null, "a");

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
        AdminService.setPatologiaDAO(new PatologiaDAO());
        AdminService.setTerapiaConcomitanteDAO(new TerapiaConcomitanteDAO());
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
        assertEquals("EMPTY_FIELDS", invokeTryRemovePatologia("", LocalDate.now(), "Note").toString());
    }

    // --- TEST CASE: TERAPIE CONCOMITANTI ---
    @Test
    void testGestioneTerapiaConcomitanteCompleta() throws Exception {
        // 1. Creazione OK
        assertEquals("SUCCESS", invokeTryCreateTerapiaConcomitante("Nuova TC", LocalDate.now(), LocalDate.now().plusDays(5)).toString());
        
        // 2. Già esistente 
        // IMPORTANTE: La data di inizio DEVE coincidere con quella definita nel Mock (minusDays(1))
        assertEquals("DATA_ALREADY_EXISTS", invokeTryCreateTerapiaConcomitante("TC1", LocalDate.now().minusDays(1), LocalDate.now().plusDays(5)).toString());
        
        // 3. Data fine prima di data inizio
        assertEquals("INVALID_DATE", invokeTryCreateTerapiaConcomitante("Test", LocalDate.now(), LocalDate.now().minusDays(5)).toString());

        // 4. Rimozione OK (TC1 esiste nel mock)
        assertEquals("SUCCESS", invokeTryRemoveTerapia("TC1", LocalDate.now(), LocalDate.now().plusDays(10)).toString());
    }

    @Test
    void testTerapiaConcomitanteCampiVuoti() throws Exception {
        // Test nome vuoto
        assertEquals("EMPTY_FIELDS", invokeTryCreateTerapiaConcomitante("", LocalDate.now(), LocalDate.now().plusDays(5)).toString());
        
        // Test date null (passiamo null esplicitamente)
        assertEquals("EMPTY_FIELDS", invokeTryCreateTerapiaConcomitante("Valid Name", null, LocalDate.now()).toString());
        
        assertEquals("EMPTY_FIELDS", invokeTryRemoveTerapia("", LocalDate.now(), LocalDate.now().plusDays(5)).toString());
    }
}
