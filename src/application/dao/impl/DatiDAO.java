package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Dato;
import application.model.Utente;

public class DatiDAO implements application.dao.interfaces.DatiDAOinterface {
    
    //Restituisce la lista del tipo di dato del paziente
    public List<Dato> getDatiByPaziente(Utente p, String tipo) {
        List<Dato> lista = new ArrayList<>();
        String query = "SELECT * FROM " + tipo + " WHERE CF = ? ORDER BY nome";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
			
            stmt.setString(1, p.getCf());

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Dato dato = new Dato(
                        rs.getString("CF"),
                        rs.getString("nome"),
                        rs.getString("modificato")
                    );
                    lista.add(dato);
                }
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }
    
    //Crea un nuovo dato conoscendo il tipo e lo inserisce nel database
    public boolean creaDato(Dato d, String tipo) {
        String query = "INSERT INTO " + tipo + " (CF, nome, modificato) VALUES (?, ?, ?)";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {
					
			stmt.setString(1, d.getCF());
			stmt.setString(2, d.getNome());
			stmt.setString(3, d.getModificato());
					
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

    //Elimina dal database un particolare tipo di dato
    public boolean eliminaDato(Dato d, String tipo) {
        String query = "DELETE FROM " + tipo + " WHERE CF = ? AND nome = ?";
        try(Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
                    
            stmt.setString(1, d.getCF());
            stmt.setString(2, d.getNome());
                    
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