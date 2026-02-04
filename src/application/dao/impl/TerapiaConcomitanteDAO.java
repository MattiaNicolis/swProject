package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.TerapiaConcomitante;
import application.model.Utente;

public class TerapiaConcomitanteDAO implements application.dao.interfaces.TerapiaConcomitanteDAOinterface {

    public List<TerapiaConcomitante> getTerapieConcomitantiByPaziente(Utente p) {
        // Implementazione del metodo per recuperare tutte le terapie concomitanti dal database
        List<TerapiaConcomitante> lista = new ArrayList<>();
        String query = "SELECT * FROM terapieconcomitanti WHERE CF = ? ORDER BY dataInizio DESC";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setString(1, p.getCf());

			try(ResultSet rs = stmt.executeQuery()) {
				while(rs.next()) {
					TerapiaConcomitante terapia = new TerapiaConcomitante(
							rs.getString("CF"),
							rs.getString("nome"),
							rs.getDate("dataInizio").toLocalDate(),
							rs.getDate("dataFine").toLocalDate(),
							rs.getString("modificato")
						);
					lista.add(terapia);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }

    public boolean creaTerapiaConcomitante(TerapiaConcomitante terapiaConcomitante) {
        // Implementazione del metodo per creare una nuova terapia concomitante nel database
        String query = "INSERT INTO terapieconcomitanti (CF, nome, dataInizio, dataFine, modificato) VALUES (?, ?, ?, ?, ?)";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
					
			stmt.setString(1, terapiaConcomitante.getCf());
			stmt.setString(2, terapiaConcomitante.getNome());
			stmt.setDate(3, java.sql.Date.valueOf(terapiaConcomitante.getDataInizio()));
			stmt.setDate(4, java.sql.Date.valueOf(terapiaConcomitante.getDataFine()));
			stmt.setString(5, terapiaConcomitante.getModificato());
					
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

    public boolean eliminaTerapiaConcomitante(TerapiaConcomitante terapiaConcomitante) {
        // Implementazione del metodo per eliminare una terapia concomitante dal database
        String query = "DELETE FROM terapieconcomitanti WHERE CF = ? AND nome = ?";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
					
			stmt.setString(1, terapiaConcomitante.getCf());
			stmt.setString(2, terapiaConcomitante.getNome());
					
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
