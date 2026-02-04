package application.dao.interfaces;

import java.util.List;

import application.model.Mail;
import application.model.Utente;
import application.model.UtenteInfo;

public interface MailDAOinterface {
    public List<Mail> getMailRicevute(Utente utente);
    public List<Mail> getMailInviate(Utente utente);
    public boolean scriviMail(Mail mail);
    public boolean vediMail(Mail mail);
    public String getMailDiabetologoRif(String cf);
    public List<UtenteInfo> getUtenteInfo(String role);
}
