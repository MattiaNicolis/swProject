package test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.controller.MostraDatiPazienteController;

public class MostraDatiPazienteTest {
    
    private MostraDatiPazienteController controller;

    private Object invokeTryScelta(String scelta, LocalDate date) throws Exception {
        Method method = MostraDatiPazienteController.class.getDeclaredMethod("tryScelta", String.class, LocalDate.class);
        method.setAccessible(true); // Rende il metodo privato accessibile
        Object result =  method.invoke(controller, scelta, date);
        return result;
    }

    @BeforeEach
    void setup() {
        controller = new MostraDatiPazienteController();
    }

    @Test
    @DisplayName("EMPTY FIELDS CASE")
    void testEmptyFields() {
        try {
            Object result = invokeTryScelta("", null);
            result = result.toString();

            assertEquals("EMPTY_FIELD", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("DATE IN THE FUTURE CASE")
    void testDateInFuture() {
        try {
            Object result = invokeTryScelta("Settimana", LocalDate.of(2026, 12, 12));
            result = result.toString();

            assertEquals("DATE_IN_FUTURE", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("SUCCESS CASE")
    void testSuccess() {
        try {
            Object result = invokeTryScelta("Mese", LocalDate.of(2025, 12, 12));
            result = result.toString();

            assertEquals("OK", result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}