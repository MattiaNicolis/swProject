package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Mail;
import application.model.Utente;
import application.model.UtenteInfo;

public class MailDAO implements application.dao.interfaces.MailDAOinterface {

    //Restituisce la lista delle mail ricevute di un determinato utente (diabetologo/paziente)
    public List<Mail> getMailRicevute(Utente utente) {
        List<Mail> lista = new ArrayList<>();
        String query = "SELECT * FROM mail WHERE destinatario = ? ORDER BY giorno DESC, orario DESC";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
            stmt.setString(1, utente.getMail());

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Mail mail = new Mail(
                        rs.getInt("id"),
                        rs.getString("mittente"),
                        utente.getMail(),
                        rs.getString("oggetto"),
                        rs.getString("corpo"),
                        rs.getDate("giorno").toLocalDate(),
                        rs.getTime("orario").toLocalTime(),
                        rs.getBoolean("letta"));
                    lista.add(mail);
                }
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }

    //Restituisce la lista delle mail inviate da un particolare utente p (diabetologo/paziente)
    public List<Mail> getMailInviate(Utente utente) {
        List<Mail> lista = new ArrayList<>();
        String query = "SELECT * FROM mail WHERE mittente = ? ORDER BY giorno DESC, orario DESC";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
            stmt.setString(1, utente.getMail());

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Mail mail = new Mail(
                        rs.getInt("id"),
                        utente.getMail(),
                        rs.getString("destinatario"),
                        rs.getString("oggetto"),
                        rs.getString("corpo"),
                        rs.getDate("giorno").toLocalDate(),
                        rs.getTime("orario").toLocalTime(),
                        rs.getBoolean("letta"));
                    lista.add(mail);
                }
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }

    //Crea una nuova mail da inviare e la inserisce nel databse
    public boolean scriviMail(Mail m) {
        String query = "INSERT INTO mail (mittente, destinatario, oggetto, corpo, giorno, orario, letta) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection(); 
	    		PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, m.getMittente());
            stmt.setString(2, m.getDestinatario());
            stmt.setString(3, m.getOggetto());
            stmt.setString(4, m.getCorpo());
            stmt.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setTime(6, java.sql.Time.valueOf(LocalTime.now()));
            stmt.setBoolean(7, false);
                    
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                return true;
            } else {
                return false;
            }

		} catch (SQLException e) {
			e.printStackTrace();
            return false;
	    }
    }

    //Seleziona il contenuto della mail che si vuole visualizzare
    public boolean vediMail(Mail m) {
        String query = "UPDATE mail SET letta = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, true);
            stmt.setInt(2, m.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) return true;
            else return false;
        } catch (SQLException ev) {
            ev.printStackTrace();
            return false;
        }
    }

    //Restiusce la mail del del diabetologo di riferimento di un determinato paziente a partire dal cf del diabetolo
    public String getMailDiabetologoRif(String cf) {
        String query = "SELECT mail FROM diabetologi WHERE cf = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cf);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("mail");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Restituisce una lista di informazioni (nome, cognome, mail) di un determinato utente (diabetologo/paziente) a partire dal ruolo
    public List<UtenteInfo> getUtenteInfo(String role) {
        List<UtenteInfo> lista = new ArrayList<>();
        String query = "SELECT nome, cognome, mail FROM " + role;
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UtenteInfo ui = new UtenteInfo(
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("mail"));
                    lista.add(ui);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}