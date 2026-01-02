package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Patologia;
import application.model.Utente;

public class PatologiaDAO implements application.dao.interfaces.PatologiaDAOinterface {

    public List<Patologia> getPatologieByPaziente(Utente p) {
        // Implementazione del metodo per recuperare tutte le patologie dal database
        List<Patologia> lista = new ArrayList<>();
        String query = "SELECT * FROM patologie WHERE CF = ? ORDER BY nome";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setString(1, p.getCf());

			try(ResultSet rs = stmt.executeQuery()) {
				while(rs.next()) {
					Patologia patologia = new Patologia(
							rs.getString("CF"), 
							rs.getString("nome"),
							rs.getDate("dataInizio").toLocalDate(),
							rs.getString("indicazioni"),
							rs.getString("modificato")
					);
					lista.add(patologia);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }

    public boolean creaPatologia(Patologia patologia) {
        // Implementazione del metodo per creare una nuova patologia nel database
        String query = "INSERT INTO patologie (CF, nome, dataInizio, indicazioni, modificato) VALUES (?, ?, ?, ?, ?)";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
					
			stmt.setString(1, patologia.getCf());
			stmt.setString(2, patologia.getNome());
			stmt.setDate(3, java.sql.Date.valueOf(patologia.getInizio()));
			stmt.setString(4, patologia.getIndicazioni());
			stmt.setString(5, patologia.getModificato());

			int rows = stmt.executeUpdate();
					
			if(rows > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
            return false;
		}
    }

    public boolean eliminaPatologia(Patologia patologia) {
        // Implementazione del metodo per eliminare una patologia dal database
        String query = "DELETE FROM patologie WHERE CF = ? AND nome = ?";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
					
			stmt.setString(1, patologia.getCf());
			stmt.setString(2, patologia.getNome());
					
			int rows = stmt.executeUpdate();
					
			if(rows > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
            return false;
		}
    }
}