/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Player;
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
    
    void addOnlyMatchesInfo(String tournamentUrl, ArrayList<MatchLocal> matches) {
        String sql = "INSERT INTO `match` (match_url, tournament_id, tour, team_home, team_guest, played)\n" +
                    "select ?, (select id from tournament t where t.url = ?), ?, (select id from team where team_url = ?), (select id from team where team_url = ?), 0;";
        matches.forEach((match) -> {
            try {
                PreparedStatement preparedStatement = connect.prepareStatement(sql);
                preparedStatement.setString(1, match.url);
                preparedStatement.setString(2, tournamentUrl);
                preparedStatement.setString(3, match.tour);
                preparedStatement.setString(4, match.teamHomeUrl);
                preparedStatement.setString(5, match.teamGuestUrl);
                preparedStatement.executeUpdate();
                connect.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    ArrayList<String> getTournamnetWithCountTours(){
        String sql = "select distinct url\n" +
                    "from tournament t, tournamnet_team tt, team tm\n" +
                    "where tt.team_id = tm.id\n" +
                    "and tt.tournament_id = t.id\n" +
                    "and tm.league_id = 3\n" +
                    "#and t.url = '/tournament18634'\n" +
                    "group by t.url;";
        ArrayList<String> touurnamnetTours = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String data = resultSet.getString(1);
                touurnamnetTours.add(data);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return touurnamnetTours;
                
    }
    
    void addedPlayers(String teamUrl, ArrayList<Player> players){
        String sqlInsertPLayer = "INSERT INTO player (league_id, name, surname, middle, birthday, amplua_id, player_url, image_url)"
                + "SELECT 3, ?, ?, ?, ?, (select id from amplua where amplua_name = ?), ?, ?";
        String sqlInsertSqua = "INSERT INTO squad_actual (player_id, team_id)" +
                "SELECT (select id from player where player_url = ?),(select id from team where team_url = ?)";
       
        for(Player p : players){
            try {
                PreparedStatement preparedStatement = connect.prepareStatement(sqlInsertPLayer);
                String[] name = p.name.split(" ");
                preparedStatement.setString(1, name[1]);
                preparedStatement.setString(2, name[0]);
                String middle = "";
                if(name.length > 2) {
                    middle = name[2];
                }
                preparedStatement.setString(3, middle);
                preparedStatement.setString(4, p.birthday);
                preparedStatement.setString(5, p.amplua);
                preparedStatement.setString(6, p.urlName);
                preparedStatement.setString(7, p.urlPictures);
                try{
                    preparedStatement.executeUpdate();
                }catch(SQLException ex){
                    System.out.println("Уже добавлен " + ex.getLocalizedMessage());
//                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                PreparedStatement preparedStatementPlayer = connect.prepareStatement(sqlInsertSqua);
                preparedStatementPlayer.setString(1, p.urlName);
                preparedStatementPlayer.setString(2, teamUrl);
                preparedStatementPlayer.executeUpdate();
                connect.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    ArrayList<Team> getTeams(String urlTournament){
        String sql = "select tm.*\n" +
                    "from tournament t, tournamnet_team tt, team tm\n" +
                    "where tt.team_id = tm.id\n" +
                    "and tt.tournament_id = t.id\n" +
                    "and tm.league_id = 3\n";
        if(urlTournament != null){
            sql +=  "and t.url = ?;";
        }
        ArrayList<Team> teams = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            if(urlTournament != null){
                preparedStatement.setString(1, urlTournament);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String teamName = resultSet.getString("team_name");
                String teamUrl = resultSet.getString("team_url");
                Team team = new Team();
                team.teamName = teamName;
                team.urlName = teamUrl;
                teams.add(team);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return teams;
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
