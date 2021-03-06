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
    String url = "jdbc:mysql://localhost:3306/football_main_ucl";
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
        } catch (SQLException ex) {
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
