package application.dao.interfaces;

import java.time.LocalDate;
import java.util.List;

import application.model.Terapia;
import application.model.Utente;

public interface TerapiaDAOinterface {
    public boolean creaTerapia(Terapia terapia);
    public boolean eliminaTerapia(Terapia terapia);
    public boolean modificaTerapia(Terapia terapia);
    public List<Terapia> getTerapieByPaziente(Utente paziente);
    public boolean notificaTerapia(Terapia terapia);
    public int getNumeroTerapieAttive(String cf, LocalDate data);
    public int getTerapieSoddisfatte(String cf, LocalDate data);
    public boolean aggiornaNumQuestionari(Terapia terapia);
    public Terapia getTerapiaById(int id);
}
