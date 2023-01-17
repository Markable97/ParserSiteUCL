/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Team;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author march
 */
public class DBRequest {
    String user = "root";
    String password = "7913194";
    String url = "jdbc:mysql://localhost:3306/sport_community";
    Connection connect;
    
    public DBRequest(){
        try {
            this.connect = DriverManager.getConnection(url, user, password);
            connect.setAutoCommit(false);
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
    
    void addTeams(ArrayList<Team> teams, String divisionUrl){
        String sql = "insert into team(league_id, team_name, team_url, team_image)\n" +
                     "values (3, ?, ?, ?);";
        String sqlDop = "insert into tournamnet_team(tournament_id, team_id)" +
                     "select (select id from tournament where url = ?), (select id from team t where t.team_url = ?)";
        for(Team team : teams){
            try {
                PreparedStatement preparedStatement = connect.prepareStatement(sql);
                preparedStatement.setString(1, team.teamName);
                preparedStatement.setString(2, team.urlName);
                preparedStatement.setString(3, team.urlImage);
                preparedStatement.execute();
                preparedStatement = connect.prepareStatement(sqlDop);
                preparedStatement.setString(1, divisionUrl);
                preparedStatement.setString(2, team.urlName);
                preparedStatement.execute();
                connect.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    ArrayList<String> getTournamentUrl(){
        ArrayList<String> urls = new ArrayList<>();
        String sql = "select t.url\n" +
                        "from tournament t, season s\n" +
                        "where t.season_id = s.id\n" +
                        "and s.id = 5;";
        try {
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String url = resultSet.getString(1);
                urls.add(url);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        };
        return urls;
    }
}
