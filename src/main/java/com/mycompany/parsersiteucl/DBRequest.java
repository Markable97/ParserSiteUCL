/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.sql.CallableStatement;
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
    PreparedStatement preparedStatement;
    PreparedStatement preparedStatementPlayer;
    PreparedStatement preparedStatementTournament;
    CallableStatement  procPlayerMatch;
    
    ResultSet resultSet;
    
    String sqlInsertTeam = ""
        + " insert into teams "
        + " set id_division = (select id_division "
        + "  from divisions "
        + " where name_division like ?), "
        + " team_name = ?" ;
    String sqlInserPlayers = ""
        + " insert into players "
        + " set id_team = (select id_team from teams where team_name = ?), "
        + " name = ?, "
        + " url_site = ?";
    String sqlUpdateTournamentTable = ""
            + " update tournament_table "
            + " set games = ?,"
            + " wins = ?, "
            + " draws = ?, "
            + " losses = ?, "
            + " goals_scored = ?, "
            + " goals_conceded = ?, "
            + " points = ? "
            + " where id_team = (select id_team from teams where team_name = ?) "
            + " and id_division = (select id_division from divisions where name_division like ?)";
    
    String sqlInsertMatches = ""
            + " insert into matches "
            + " set id_season = 1, "
            + " id_division = (select id_division from divisions where name_division = ?), "
            + " id_tour = ?, "
            + " team_home = (select id_team from teams where team_name = ?), "
            + " goal_home = ?, "
            + " goal_guest = ?, "
            + " team_guest = (select id_team from teams where team_name = ?), "
            + " m_date = ?, "
            + " id_stadium = (select id_stadium from stadiums where name_stadium like ?)";
    String sqlPocPlayerInMatche = "CALL insPlayerInMatche2(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
    
    public DBRequest(){
        try {
            this.connect = DriverManager.getConnection(url, user, password);
            connect.setAutoCommit(false);
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void addedPlayers(int team_id, ArrayList<Player> players){
        String sqlInsertPLayer = "INSERT INTO player (league_id, name, surname, birthday, amplua_id, player_url, image_url)"
                + "SELECT 2, ?, ?, ?, (select id from amplua where short_name = ?), ?, ?";
        String sqlInsertSqua = "INSERT INTO squad_actual (player_id, team_id)" +
                "SELECT `AUTO_INCREMENT` - 1, ?\n" +
                "FROM  INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_NAME   = 'player';";
        try{
            for(Player p : players){
                preparedStatement = connect.prepareStatement(sqlInsertPLayer);
                String[] name = p.name.split(" ");
                preparedStatement.setString(1, name[1]);
                preparedStatement.setString(2, name[0]);
                preparedStatement.setString(3, p.birthday);
                String amplua;
                if (p.amplua.isBlank()){
                    amplua = "Ун..";
                } else {
                    amplua = p.amplua;
                }
                preparedStatement.setString(4, amplua);
                preparedStatement.setString(5, p.urlName);
                preparedStatement.setString(6, p.urlPictures);
                try{
                    preparedStatement.executeUpdate();
                    preparedStatementPlayer = connect.prepareStatement(sqlInsertSqua);
                    preparedStatementPlayer.setInt(1, team_id);
                    preparedStatementPlayer.executeUpdate();
                    connect.commit();
                }catch(SQLException ex){
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    connect.rollback();
                    break; 
                }
            }           
        } catch(SQLException ex){
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void updateScore(ArrayList<ParserSiteUCL.MatchLocal> matches){
        String updateScore = "call matchAddResult((select id from `match` where match_url = ?), ?, ?);;";
        for(ParserSiteUCL.MatchLocal match : matches){
            try{
                if(!match.goalsHome.equals("-")){
                    try{
                       int goalsHome = Integer.parseInt(match.goalsHome);
                       int goalsGuest = Integer.parseInt(match.goalsGuest);
                       try {
                            preparedStatement = connect.prepareStatement(updateScore);
                            preparedStatement.setString(1, match.url);
                            preparedStatement.setInt(2, goalsHome);
                            preparedStatement.setInt(3, goalsGuest);
                            preparedStatement.executeUpdate();
                            connect.commit();
                        } catch (SQLException ex) {
                            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }catch(NumberFormatException ex){
                        connect.rollback();
                        break;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void updateUrl(ArrayList<ParserSiteUCL.MatchLocal> matches){
        String updateUrl = "update `match` "
                + "set match_url = ? "
                + "where id = (select match_id from `schedule` where league_id = 2 and game_date = ?)";
        for(ParserSiteUCL.MatchLocal match : matches){
            try {
                preparedStatement = connect.prepareStatement(updateUrl);
                preparedStatement.setString(1, match.url);
                preparedStatement.setString(2, match.date+" "+match.time);
                preparedStatement.executeUpdate();
                connect.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void addedMatches(ArrayList<ParserSiteUCL.MatchLocal> matches){
        String sqlAddMatch = "INSERT INTO `match` (tournament_id, team_home, team_guest, tour, played) " +
                                "select  5,\n" +
                                "(select id from team where league_id = 2 and team_name = ?) as team_home,\n" +
                                "(select id from team where league_id = 2 and team_name = ?) as team_guest,\n" +
                                "? as tour, 1";
        String sqlAddSchedule = "INSERT INTO `schedule` (stadium_id, league_id, game_date, match_id)\n" +
                                "VALUES (3, 2, ?, ?)";
        try{
            int match_id = 19;
            for(ParserSiteUCL.MatchLocal match : matches){
                preparedStatement = connect.prepareStatement(sqlAddMatch);
                preparedStatement.setString(1, match.teamHome);
                preparedStatement.setString(2, match.teamGuest);
                preparedStatement.setString(3, match.tour);
                try{
                    preparedStatement.executeUpdate();
                    match_id++;
                    preparedStatementPlayer = connect.prepareStatement(sqlAddSchedule);
                    System.out.println(match.date+" "+match.time);
                    preparedStatementPlayer.setString(1, match.date+" "+match.time);
                    preparedStatementPlayer.setLong(2, match_id);
                    preparedStatementPlayer.executeUpdate();
                    connect.commit();
                }catch(SQLException ex){
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    connect.rollback();
                    break; 
                }
                
            }
            connect.commit();
        }catch(SQLException ex){
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void insertMatchesAndDoingPlayers(ArrayList<Match> matches){
        System.out.println("Start add mathces");
        try{
            connect.setAutoCommit(false);
            for(Match m : matches){
                procPlayerMatch = connect.prepareCall(sqlPocPlayerInMatche);
                preparedStatement = connect.prepareStatement(sqlInsertMatches);
                preparedStatement.setString(1, m.division);
                preparedStatement.setInt(2, m.tour);
                preparedStatement.setString(3, m.teamHome);
                preparedStatement.setInt(4, m.goalHome);
                preparedStatement.setInt(5, m.goalGuest);
                preparedStatement.setString(6, m.teamGuest);
                preparedStatement.setString(7, m.dateMatch);
                preparedStatement.setString(8, m.stadium);
                try{
                    preparedStatement.executeUpdate();
                    if(m.players != null){
                        for(Player p : m.players){
                            procPlayerMatch.setString(1, m.teamHome);
                            procPlayerMatch.setString(2, m.teamGuest);
                            procPlayerMatch.setInt(3, m.tour);
                            procPlayerMatch.setString(4, p.name );                     
                            procPlayerMatch.setString(5, p.teamName);
                            procPlayerMatch.setInt(6,p.goal);
                            procPlayerMatch.setInt(7,p.assist);
                            procPlayerMatch.setInt(8,p.yellow);
                            procPlayerMatch.setInt(9,p.red);
                            procPlayerMatch.setInt(10, p.penalty);
                            procPlayerMatch.setInt(11, p.penaltyOut);
                            procPlayerMatch.setInt(12, p.ownGoal);
                            procPlayerMatch.setString(13, p.urlName);
                            procPlayerMatch.setString(14, p.teamName);
                            procPlayerMatch.setString(15, p.amplua);
                            try{
                                procPlayerMatch.execute();
                                System.out.println("Игрок добавлен = " + p.name + " " +  p.urlName+ " " + p.teamName);
                            }catch(SQLException ex){
                                System.out.println("Ошибка на игроке = " + p.name + " " +  p.urlName+ " " + p.teamName);
                                System.out.println("EROOOOOOOOOR!!! ИГРОК\n" + ex);
                            }
                        }
                        
                    }
                }catch(SQLException ex){
                    System.out.println("Error " + m.tour + " " + m.teamHome + " " + m.teamGuest);
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    connect.rollback();
                    break;
                    
                }
                connect.commit();
                
            }
        }catch(SQLException ex){
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void insertTeamAndPlayerAndTournamentUpdate(ArrayList<TournamentTable> table) throws SQLException{
        System.out.println("Start add");
        try{
            connect.setAutoCommit(false);
            for(TournamentTable t : table){
                preparedStatement = connect.prepareStatement(sqlInsertTeam);
                preparedStatement.setString(1, "%"+t.nameDivision+"%");
                preparedStatement.setString(2, t.team.teamName);
                try{
                    preparedStatement.executeUpdate();
                    System.out.println("Команда дабавлена: " + t.nameDivision + 
                                " " + t.team.teamName);
                 }catch(SQLException ex){
                    System.out.println("Ошибка на команде: " + t.nameDivision + 
                                " " + t.team.teamName);
                    System.out.println("EROOOOOOOOOR!!!\n" + ex);
                    }
                for(Player p : t.team.players){
                    preparedStatementPlayer = connect.prepareStatement(sqlInserPlayers);
                    preparedStatementPlayer.setString(1, p.teamName);
                    preparedStatementPlayer.setString(2, p.name);
                    preparedStatementPlayer.setString(3, p.urlName);
                    try{
                        preparedStatementPlayer.executeUpdate();
                        System.out.println("Игрок добавлен: " + p.teamName + " " + p.name);
                    }
                    catch(SQLException ex){
                        System.out.println("EROOOOOOOOOR!!!\n" + ex);
                    }
                }
                preparedStatementTournament = connect.prepareStatement(sqlUpdateTournamentTable);
                preparedStatementTournament.setInt(1, t.games);
                preparedStatementTournament.setInt(2, t.wins);
                preparedStatementTournament.setInt(3, t.draws);
                preparedStatementTournament.setInt(4, t.losses);
                preparedStatementTournament.setInt(5, t.goalsScored);
                preparedStatementTournament.setInt(6, t.goalsConceded);
                preparedStatementTournament.setInt(7, t.points);
                preparedStatementTournament.setString(8, t.team.teamName);
                preparedStatementTournament.setString(9, "%"+t.nameDivision+"%");
                preparedStatementTournament.executeUpdate();
            }
            connect.commit();
        }catch(SQLException ex){
             System.out.println("EROOOOOOOOOR!!!\n" + ex);
             connect.rollback();
        }finally{
            try {
                preparedStatement.close();
                preparedStatementPlayer.close();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
