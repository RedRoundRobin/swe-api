package com.redroundrobin.thirema.apirest.models.timescale;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Conditions1 {

    @Autowired
    Environment env;

    public List<Conditions> prova() {
        List<Conditions> list = new ArrayList<Conditions>();

        try {
            //Carichiamo un driver di tipo 1 (bridge jdbc-odbc)
            Class.forName("org.postgresql.Driver");

            // Otteniamo una connessione con username e password
            Connection con = DriverManager.getConnection ("jdbc:postgresql://192.168.99.100:3456/sasso_database","user","user");

            // Creiamo un oggetto Statement per poter interrogare il db
            Statement cmd = con.createStatement ();

            // Eseguiamo una query e immagazziniamone i risultati
            // in un oggetto ResultSet
            String qry = "SELECT * FROM conditions";
            ResultSet rs = cmd.executeQuery(qry);
            // Stampiamone i risultati riga per riga

            while (rs.next()) {
                System.out.print("Column 1 returned ");
                System.out.println(rs.getString(1));
                list.add(new Conditions(rs.getTimestamp ("time"),rs.getString("location"),rs.getDouble("temperature"),rs.getDouble("humidity")));
            }

            rs.close();
            cmd.close();
            con.close();
        } catch (SQLException e) {
            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }

        return list;
    }
}
