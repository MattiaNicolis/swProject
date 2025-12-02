package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.controller.DiabetologoController;
import application.model.Glicemia;

class DiabetologoControllerTest {

    private DiabetologoController controller;

    @BeforeEach
    void setUp() {
        controller = new DiabetologoController();
        // NON chiamiamo initialize() per evitare NullPointerException sui componenti grafici
    }

    /**
     * HELPER METHOD per invocare il metodo privato "getColoreSeverita"
     */
    private String invokeGetColoreSeverita(Glicemia g) throws Exception {
        Method method = DiabetologoController.class.getDeclaredMethod("getColoreSeverita", Glicemia.class);
        method.setAccessible(true); // Rende il metodo privato accessibile
        return (String) method.invoke(controller, g);
    }

    // --- TEST LOGICA COLORI (PRE PASTO) ---

    @Test
    @DisplayName("Pre-Pasto: Deve essere ROSSO se < 60 (Ipoglicemia Grave)")
    void testRossoPrePastoLow() throws Exception {
        Glicemia g = new Glicemia("G1", 50, LocalDate.now(), "10:00", "Pre pasto");
        String colore = invokeGetColoreSeverita(g);
        assertEquals("#FF0000", colore);
    }

    @Test
    @DisplayName("Pre-Pasto: Deve essere ROSSO se > 150 (Iperglicemia Grave)")
    void testRossoPrePastoHigh() throws Exception {
        Glicemia g = new Glicemia("G2", 160, LocalDate.now(), "10:00", "Pre pasto");
        String colore = invokeGetColoreSeverita(g);
        assertEquals("#FF0000", colore);
    }

    @Test
    @DisplayName("Pre-Pasto: Deve essere ARANCIONE se 145 (Attenzione)")
    void testArancionePrePasto() throws Exception {
        // Range Arancione: 140 < x <= 150 (secondo la logica del tuo if)
        // O anche < 70
        Glicemia g = new Glicemia("G3", 145, LocalDate.now(), "10:00", "Pre pasto");
        String colore = invokeGetColoreSeverita(g);
        assertEquals("#FFA500", colore);
    }

    @Test
    @DisplayName("Pre-Pasto: Deve essere NULL (Normale) se 100")
    void testNormalePrePasto() throws Exception {
        // Range normale (nessun if scatta)
        Glicemia g = new Glicemia("G4", 100, LocalDate.now(), "10:00", "Pre pasto");
        String colore = invokeGetColoreSeverita(g);
        assertNull(colore, "Se la glicemia è ok, il metodo deve ritornare null");
    }

    // --- TEST LOGICA COLORI (POST PASTO) ---

    @Test
    @DisplayName("Post-Pasto: Soglie piu' alte (Rosso > 200)")
    void testRossoPostPasto() throws Exception {
        Glicemia g = new Glicemia("G5", 210, LocalDate.now(), "10:00", "Post pasto");
        String colore = invokeGetColoreSeverita(g);
        assertEquals("#FF0000", colore);
    }

    @Test
    @DisplayName("Post-Pasto: Normale a 160")
    void testNormalePostPasto() throws Exception {
        // 160 è alto per Pre-pasto, ma OK per Post-pasto (che diventa giallo > 180)
        Glicemia g = new Glicemia("G6", 160, LocalDate.now(), "10:00", "Post pasto");
        String colore = invokeGetColoreSeverita(g);
        assertNull(colore);
    }
    
    // --- TEST UNITA' ---
    
    @Test
    @DisplayName("Il controller viene istanziato correttamente")
    void testControllerNotNull() {
        assertNotNull(controller);
    }
}