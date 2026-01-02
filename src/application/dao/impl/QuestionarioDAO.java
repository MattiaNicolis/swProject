package application.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Questionario;
import application.model.Utente;

public class QuestionarioDAO implements application.dao.interfaces.QuestionarioDAOinterface {

    public boolean creaQuestionario(Questionario q) {
        String query = "INSERT INTO questionario (CF, giornoCompilazione, nomeFarmaco, dosiGiornaliere, quantità, sintomi, controllato, terapia_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection(); 
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, q.getCf());
			stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
			stmt.setString(3, q.getNomeFarmaco());
			stmt.setInt(4, q.getDosiGiornaliere());
			stmt.setInt(5, q.getQuantità());
			stmt.setString(6, q.getSintomi());
			stmt.setBoolean(7, false);
			stmt.setInt(8, q.getTerapiaId()); // --> da controllare

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

	public List<Questionario> getQuestionariByPaziente(Utente p) {
		List<Questionario> lista = new ArrayList<>();
        String query = "SELECT * FROM questionario WHERE CF = ? ORDER BY giornoCompilazione DESC";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setString(1, p.getCf());

			try(ResultSet rs = stmt.executeQuery()) {
				while(rs.next()) {
					Questionario q = new Questionario(
							rs.getInt("id"),
							rs.getString("CF"),
							rs.getDate("giornoCompilazione").toLocalDate(),
							rs.getString("nomeFarmaco"),
							rs.getInt("dosiGiornaliere"),
							rs.getInt("quantità"),
							rs.getString("sintomi"),
							rs.getBoolean("controllato"),
							rs.getInt("terapia_id")
						);
					lista.add(q);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
	}

	public List<Questionario> getQuestionariNonConformi() {
		List<Questionario> lista = new ArrayList<>();
		String query = """
			SELECT q.* FROM questionario q
			JOIN terapie t ON q.terapia_id = t.id
			WHERE 
				(
					q.nomeFarmaco != t.nomeFarmaco 
					OR q.dosiGiornaliere != t.dosiGiornaliere
					OR q.quantità != t.quantità
				)
			ORDER BY q.giornoCompilazione DESC
		""";

    	try (Connection conn = Database.getConnection();
        	 PreparedStatement stmt = conn.prepareStatement(query);
        	 ResultSet rs = stmt.executeQuery()) {

       		while (rs.next()) {
				// Costruisci l'oggetto Questionario
				Questionario q = new Questionario(
					rs.getInt("id"),
					rs.getString("CF"),
					rs.getDate("giornoCompilazione").toLocalDate(),
					rs.getString("nomeFarmaco"),
					rs.getInt("dosiGiornaliere"),
					rs.getInt("quantità"),
					rs.getString("sintomi"),
					rs.getBoolean("controllato"),
					rs.getInt("terapia_id")
				);
            	lista.add(q);
        	}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}

	public boolean segnaComeControllato(Questionario q) {
		String query = "UPDATE questionario SET controllato = 1 WHERE id = ?";
		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setInt(1, q.getId());
			
			int rows = stmt.executeUpdate();
			if(rows > 0){
				return true;
			}
			else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean esisteQuestionarioOggi(int terapiaId) {
		// SELECT 1 è un'ottimizzazione: non ci interessa leggere i dati, solo sapere se c'è una riga.
		String query = "SELECT 1 FROM questionario WHERE terapia_id = ? AND giornoCompilazione = ?";

		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setInt(1, terapiaId);
			
			stmt.setDate(2, Date.valueOf(LocalDate.now()));

			try (ResultSet rs = stmt.executeQuery()) {
				// Se rs.next() è true, significa che il database ha trovato almeno una riga
				return rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}