package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.FattoriComorbiditàAllergie;
import application.model.Utente;

public class FattoriComorbiditàAllergieDAO implements application.dao.interfaces.FattoriComorbiditàAllergieDAOinterface {
    
    public List<FattoriComorbiditàAllergie> getFattoriComorbiditàAllergieByPaziente(Utente p) {
        // Implementazione del metodo per recuperare tutti i fattori, comorbidità e allergie dal database
        List<FattoriComorbiditàAllergie> lista = new ArrayList<>();
        String query = "SELECT * FROM fattoricomorbiditàallergie WHERE CF = ? ORDER BY nome";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
            stmt.setString(1, p.getCf());

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    FattoriComorbiditàAllergie fca = new FattoriComorbiditàAllergie(
                        rs.getString("CF"),
                        rs.getString("tipo"),
                        rs.getString("nome"),
                        rs.getString("modificato")
                    );
                    lista.add(fca);
                }
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }
    
    public boolean creaFattoreComorbiditàAllergia(FattoriComorbiditàAllergie fca) {
        // Implementazione del metodo per creare un nuovo fattore, comorbidità o allergia nel database
        String query = "INSERT INTO fattoricomorbiditàallergie (CF, tipo, nome, modificato) VALUES (?, ?, ?, ?)";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
					
			stmt.setString(1, fca.getCF());
			stmt.setString(2, fca.getTipo());
			stmt.setString(3, fca.getNome());
			stmt.setString(4, fca.getModificato());
					
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

    public boolean eliminaFattoreComorbiditàAllergia(FattoriComorbiditàAllergie fca) {
        // Implementazione del metodo per eliminare un fattore, comorbidità o allergia dal database
        String query = "DELETE FROM fattoricomorbiditàallergie WHERE CF = ? AND tipo = ? AND nome = ?";
        try(Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
                    
            stmt.setString(1, fca.getCF());
            stmt.setString(2, fca.getTipo());
            stmt.setString(3, fca.getNome());
                    
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