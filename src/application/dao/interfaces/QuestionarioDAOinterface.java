package application.dao.interfaces;

import java.util.List;

import application.model.Questionario;
import application.model.Utente;

public interface QuestionarioDAOinterface {
    public boolean creaQuestionario(Questionario questionario);
    public List<Questionario> getQuestionariByPaziente(Utente paziente);
    public List<Questionario> getQuestionariNonConformi();
    public boolean segnaComeControllato(Questionario questionario);
    public boolean esisteQuestionarioOggi(int terapiaId);
} 