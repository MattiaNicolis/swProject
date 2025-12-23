package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.dao.impl.QuestionarioDAO;
import application.dao.impl.TerapiaDAO;
import application.model.Glicemia;
import application.model.Questionario;
import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.DiabetologoUtils;

class DiabetologoPageTest {
    Questionario questionario;

    class MockTerapiaDAO extends TerapiaDAO {
        @Override
        public boolean creaTerapia(Terapia t) {
            // Simuliamo il comportamento
            if ("testFarmaco".equals(t.getNomeFarmaco())) {
                return true;
            }
            return false;
        }

        public int getNumeroTerapieAttive(String cf, LocalDate data) {
            return 1;
        }

        public int getTerapieSoddisfatte(String cf, LocalDate data) {
            if(questionario.getGiornoCompilazione().isEqual(data))
                return 1;

            return 0;
        }
    }

    class MockQuestionarioDAO extends QuestionarioDAO {
        @Override
        public boolean creaQuestionario(Questionario q) {
            // Simuliamo il comportamento
            if ("testFarmaco".equals(q.getNomeFarmaco())) {
                questionario = q;
                return true;
            }
            return false;
        }
    }

    @BeforeEach
    void setup() {
        AdminService.setTerapiaDAO(new MockTerapiaDAO());
        AdminService.setQuestionarioDAO(new MockQuestionarioDAO());
    }

    @AfterEach
    void tearDown() {
        AdminService.setTerapiaDAO(new TerapiaDAO());
        AdminService.setQuestionarioDAO(new QuestionarioDAO());
    }
    
    // --- TEST LOGICA COLORI (PRE PASTO) ---

    @Test
    @DisplayName("Pre-Pasto: Deve essere ROSSO se < 60 (Ipoglicemia Grave)")
    void testRossoPrePastoLow() throws Exception {
        Glicemia g = new Glicemia("G1", 50, LocalDate.now(), "10:00", "Pre pasto");
        String colore = DiabetologoUtils.getColoreSeverita(g);
        assertEquals("#FF0000", colore);
    }

    @Test
    @DisplayName("Pre-Pasto: Deve essere ROSSO se > 150 (Iperglicemia Grave)")
    void testRossoPrePastoHigh() throws Exception {
        Glicemia g = new Glicemia("G2", 160, LocalDate.now(), "10:00", "Pre pasto");
        String colore = DiabetologoUtils.getColoreSeverita(g);
        assertEquals("#FF0000", colore);
    }

    @Test
    @DisplayName("Pre-Pasto: Deve essere ARANCIONE se 145 (Attenzione)")
    void testArancionePrePasto() throws Exception {
        // Range Arancione: 140 < x <= 150 (secondo la logica del tuo if)
        // O anche < 70
        Glicemia g = new Glicemia("G3", 145, LocalDate.now(), "10:00", "Pre pasto");
        String colore = DiabetologoUtils.getColoreSeverita(g);
        assertEquals("#FFA500", colore);
    }

    @Test
    @DisplayName("Pre-Pasto: Deve essere NULL (Normale) se 100")
    void testNormalePrePasto() throws Exception {
        // Range normale (nessun if scatta)
        Glicemia g = new Glicemia("G4", 100, LocalDate.now(), "10:00", "Pre pasto");
        String colore = DiabetologoUtils.getColoreSeverita(g);
        assertNull(colore, "Se la glicemia è ok, il metodo deve ritornare null");
    }

    // --- TEST LOGICA COLORI (POST PASTO) ---

    @Test
    @DisplayName("Post-Pasto: Soglie piu' alte (Rosso > 200)")
    void testRossoPostPasto() throws Exception {
        Glicemia g = new Glicemia("G5", 210, LocalDate.now(), "10:00", "Post pasto");
        String colore = DiabetologoUtils.getColoreSeverita(g);
        assertEquals("#FF0000", colore);
    }

    @Test
    @DisplayName("Post-Pasto: Normale a 160")
    void testNormalePostPasto() throws Exception {
        // 160 è alto per Pre-pasto, ma OK per Post-pasto (che diventa giallo > 180)
        Glicemia g = new Glicemia("G6", 160, LocalDate.now(), "10:00", "Post pasto");
        String colore = DiabetologoUtils.getColoreSeverita(g);
        assertNull(colore);
    }

    // --- TEST CONTA GIORNI DI NON COMPILAZIONE
    @Test
    @DisplayName("Giorni di non compilazione < 3")
    void testSituazioneRegolare() throws Exception {
        int giorni = 0;
        Utente p = new Utente("a", null, null, null, null, null, null, null, null);

        LocalDate dataInizio = LocalDate.of(2025, 12, 21);
        LocalDate dataFine = LocalDate.of(2025, 12, 31);
        Terapia t = new Terapia(0, "a", "testFarmaco", 1, 1, dataInizio, dataFine, "", "", false, 0);
        boolean creata = AdminService.creaTerapia(t);

        LocalDate dataCompilazione = LocalDate.of(2025, 12, 21);
        Questionario q = new Questionario(0, "a", dataCompilazione, "testFarmaco", 1, 1, null, false, 0);
        boolean creato = AdminService.creaQuestionario(q);
        if(creata && creato) {
            giorni = DiabetologoUtils.calcolaGiorniNonCompilati(p);
        }
        assertEquals(2, giorni);
    }

    @Test
    @DisplayName("Giorni di non compilazione 3 +")
    void testNonCompila() throws Exception {
        int giorni = 0;
        Utente p = new Utente("a", null, null, null, null, null, null, null, null);

        LocalDate dataInizio = LocalDate.of(2025, 12, 20);
        LocalDate dataFine = LocalDate.of(2025, 12, 31);
        Terapia t = new Terapia(0, "a", "testFarmaco", 1, 1, dataInizio, dataFine, "", "", false, 0);
        boolean creata = AdminService.creaTerapia(t);

        LocalDate dataCompilazione = LocalDate.of(2025, 12, 20);
        Questionario q = new Questionario(0, "a", dataCompilazione, "testFarmaco", 1, 1, null, false, 0);
        boolean creato = AdminService.creaQuestionario(q);
        if(creata && creato) {
            giorni = DiabetologoUtils.calcolaGiorniNonCompilati(p);
        }
        assertEquals(3, giorni);
    }
}