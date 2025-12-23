package application.dao.interfaces;

import java.util.List;

import application.model.Mail;
import application.model.Utente;

public interface MailDAOinterface {
    public List<Mail> getMailRicevute(Utente utente);
    public List<Mail> getMailInviate(Utente utente);
    public boolean scriviMail(Mail mail);
    public boolean vediMail(Mail mail);
}
