package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Terapia;
import application.model.Utente;

public class TerapiaDAO implements application.dao.interfaces.TerapiaDAOinterface {

    public boolean creaTerapia(Terapia t) {
        // Implementazione del metodo per creare una terapia nel database
        String query = "INSERT INTO terapie (CF, nomeFarmaco, dosiGiornaliere, quantità, dataInizio, dataFine, indicazioni, diabetologo, visualizzata, questionari) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = Database.getConnection(); 
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, t.getCf());
			stmt.setString(2, t.getNomeFarmaco());
			stmt.setInt(3, t.getDosiGiornaliere());
			stmt.setInt(4, t.getQuantità());
			stmt.setDate(5, java.sql.Date.valueOf(t.getDataInizio()));
			stmt.setDate(6, java.sql.Date.valueOf(t.getDataFine()));
			stmt.setString(7, t.getIndicazioni());
			stmt.setString(8, t.getDiabetologo());
			stmt.setBoolean(9, t.getVisualizzata());
			stmt.setInt(10, 0);

				
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

    public boolean eliminaTerapia(Terapia t) {
        // Implementazione del metodo per rimuovere una terapia dal database
        String sql = "DELETE FROM terapie WHERE id = ?";
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, t.getId());  // id della riga da eliminare
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modificaTerapia(Terapia t) {
        // Implementazione del metodo per modificare una terapia nel database
        String query = "UPDATE terapie SET dosiGiornaliere = ?, quantità = ?, dataInizio = ?, dataFine = ?, indicazioni = ?, diabetologo = ?, visualizzata = ?, questionari = ? WHERE id = ?";
		try (Connection conn = Database.getConnection(); 
			PreparedStatement stmt = conn.prepareStatement(query)) {

	        stmt.setInt(1, t.getDosiGiornaliere());
	        stmt.setInt(2, t.getQuantità());
	        stmt.setDate(3, java.sql.Date.valueOf(t.getDataInizio()));
	        stmt.setDate(4, java.sql.Date.valueOf(t.getDataFine()));
	        stmt.setString(5, t.getIndicazioni());
	        stmt.setString(6, t.getDiabetologo());
			stmt.setBoolean(7, false);
			stmt.setInt(8, 0);
	        stmt.setInt(9, t.getId());
	        
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

	public List<Terapia> getTerapieByPaziente(Utente p) {
		// Implementazione del metodo per ottenere tutte le terapie di un paziente dal database
		List<Terapia> lista = new ArrayList<>();
		String query = "SELECT * FROM terapie WHERE cf = ? ORDER BY dataInizio DESC";
		try (Connection conn = Database.getConnection(); 
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, p.getCf());
			
			try(ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Terapia terapia = new Terapia(
						rs.getInt("id"),
						p.getCf(),
						rs.getString("nomeFarmaco"),
						rs.getInt("dosiGiornaliere"),
						rs.getInt("quantità"),
						rs.getDate("dataInizio") != null ? rs.getDate("dataInizio").toLocalDate() : null,
						rs.getDate("dataFine") != null ? rs.getDate("dataFine").toLocalDate() : null,
						rs.getString("indicazioni"),
						rs.getString("diabetologo"),
						rs.getBoolean("visualizzata"),
						rs.getInt("questionari")
					);
					lista.add(terapia);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}

	public boolean notificaTerapia(Terapia t) {
		String query = "UPDATE terapie SET visualizzata = ? WHERE id = ?";
		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setBoolean(1, true);
			stmt.setInt(2, t.getId());

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

	public int getNumeroTerapieAttive(String cf, LocalDate data) {
		String query = "SELECT COUNT(*) FROM terapie WHERE cf = ? AND ? BETWEEN dataInizio AND dataFine";
		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setString(1, cf);
			stmt.setDate(2, java.sql.Date.valueOf(data));
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getTerapieSoddisfatte(String cf, LocalDate data) {
		String query = """
			SELECT COUNT(*) 
			FROM terapie t
			WHERE t.cf = ? 
			AND ? BETWEEN t.dataInizio AND t.dataFine -- Parametro data 2
			AND EXISTS (
				SELECT 1 
				FROM questionario q 
				WHERE q.terapia_id = t.id 
					AND q.giornoCompilazione = ? -- Parametro data 3
			)
		""";
		
		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {
			
			stmt.setString(1, cf);
			stmt.setDate(2, java.sql.Date.valueOf(data));
			stmt.setDate(3, java.sql.Date.valueOf(data));
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean aggiornaNumQuestionari(Terapia t) {
    String query = "UPDATE terapie SET questionari = questionari + 1 WHERE id = ?";

    try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, t.getId());

        int rows = stmt.executeUpdate();
        
        return rows > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

	public Terapia getTerapiaById(int id) {
		String query = "SELECT * FROM terapie WHERE id = ?";
		try (Connection conn = Database.getConnection(); 
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, id);
			
			try(ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Terapia terapia = new Terapia(
						rs.getInt("id"),
						rs.getString("CF"),
						rs.getString("nomeFarmaco"),
						rs.getInt("dosiGiornaliere"),
						rs.getInt("quantità"),
						rs.getDate("dataInizio") != null ? rs.getDate("dataInizio").toLocalDate() : null,
						rs.getDate("dataFine") != null ? rs.getDate("dataFine").toLocalDate() : null,
						rs.getString("indicazioni"),
						rs.getString("diabetologo"),
						rs.getBoolean("visualizzata"),
						rs.getInt("questionari")
					);
					return terapia;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}