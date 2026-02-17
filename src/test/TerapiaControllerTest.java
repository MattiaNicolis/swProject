package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.controller.TerapiaController;
import application.dao.impl.TerapiaDAO;
import application.model.Diabetologo;
import application.model.Paziente;
import application.model.Terapia;
import application.service.AdminService;

public class TerapiaControllerTest {

    private TerapiaController controller;
    private List<Terapia> terapie;
    private Paziente paziente;
    private Diabetologo diabetologo;
    private LocalDate dataInizio;
    private LocalDate dataFine;

    // Mock interno
    class MockTerapiaDAO extends TerapiaDAO {
        @Override
        public boolean creaTerapia(Terapia t) {
            if(t.getNomeFarmaco() == null || t.getNomeFarmaco().isBlank())
                return false;
            if(t.getDataFine().isBefore(LocalDate.now()))
                return false;
            if(t.getDataInizio().isBefore(LocalDate.now()))
                return false;
            if("farmacoSbagliato".equalsIgnoreCase(t.getNomeFarmaco()))
                return false;

            return true;
        }
    }

    private void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokeManageTerapia(String nomeFarmaco, String dosiGiornaliere, String quantità, LocalDate dataInizio, LocalDate dataFine, String indicazioni) throws Exception {
        Method method = TerapiaController.class.getDeclaredMethod("manageTerapia", 
            String.class, String.class, String.class, LocalDate.class, LocalDate.class, String.class);
        method.setAccessible(true);
        return method.invoke(controller, nomeFarmaco, dosiGiornaliere, quantità, dataInizio, dataFine, indicazioni);
    }

    @BeforeEach
    void setup() throws Exception {
        AdminService.setTerapiaDAO(new MockTerapiaDAO());
        controller = new TerapiaController();

        dataInizio = LocalDate.of(2026, 12, 31);
        dataFine = LocalDate.of(2027, 1, 10);

        terapie = new ArrayList<>();
        terapie.add(new Terapia(1, "a", "farmacoSbagliato", 0, 0, dataInizio, dataFine, null, null, false, 0));
        
        paziente = new Paziente("a", "a", "Simone", null, null, null, null, null, null);
        diabetologo = new Diabetologo("b", "b", "Mattia", null, dataFine, null, null, null);

        injectPrivateField(controller, "t", null);
        injectPrivateField(controller, "p", paziente);
        injectPrivateField(controller, "d", diabetologo);
        injectPrivateField(controller, "terapie", terapie);
    }

    @AfterEach
    void tearDown() throws Exception {
        AdminService.setTerapiaDAO(new TerapiaDAO());
    }

    //--- TEST SU CREA TERAPIA ---
    @Test
    @DisplayName("CREATION SUCCESS CASE")
    void testCreazioneTerapiaSuccesso() throws Exception {
        assertEquals("SUCCESS", invokeManageTerapia("farmacoTest", "1", "1", dataInizio, dataFine, "indicazioni").toString());
    }

    @Test
    @DisplayName("CREATION FAIL CASE")
    void testCreazioneTerapiaFallita() throws Exception {
        assertEquals("FAILURE", invokeManageTerapia("farmacoSbagliato", "1", "1", LocalDate.of(2026, 12, 19), LocalDate.of(2026, 12, 20), "indicazioni").toString());
    }

    @Test
    @DisplayName("CREATION INVALID DATA CASE")
    void testCreazioneTerapiaDatiNonValidi() throws Exception {
        assertEquals("INVALID_DATA", invokeManageTerapia("farmacoSbagliato", "1", "1", LocalDate.of(202, 11, 11), dataFine, "indicazioni").toString());
        assertEquals("INVALID_DATA", invokeManageTerapia("farmacoSbagliato", "1", "1", dataInizio, LocalDate.of(202, 11, 11), "indicazioni").toString());
    }

    @Test
    @DisplayName("CREATION INVALID DATA RANGE CASE")
    void testCreazioneTerapiaDataNonValida() throws Exception {
        assertEquals("INVALID_DATE_RANGE", invokeManageTerapia("farmacoSbagliato", "1", "1", dataInizio, dataFine, "indicazioni").toString());
    }

    @Test
    @DisplayName("CREATION EMPTY FIELDS CASE")
    void testCreazioneTerapiaCampiVuoti() throws Exception {
        assertEquals("EMPTY_FIELDS", invokeManageTerapia("", "1", "1", dataInizio, dataFine, "indicazioni").toString());
        assertEquals("EMPTY_FIELDS", invokeManageTerapia("farmacoSbagliato", "", "1", dataInizio, dataFine, "indicazioni").toString());
        assertEquals("EMPTY_FIELDS", invokeManageTerapia("farmacoSbagliato", "1", "", dataInizio, dataFine, "indicazioni").toString());
        assertEquals("EMPTY_FIELDS", invokeManageTerapia("", "", "", dataInizio, dataFine, "indicazioni").toString());
    }
}