/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lfl_vao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author march
 */

public class DBConnection {
    String user = "root";
    String password = "7913194";
    String url = "jdbc:mysql://localhost:3306/sport_community";
    Connection connect;
    
    public DBConnection(){
        try {
            this.connect = DriverManager.getConnection(url, user, password);
            //connect.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    void close(){
        try {
            connect.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
