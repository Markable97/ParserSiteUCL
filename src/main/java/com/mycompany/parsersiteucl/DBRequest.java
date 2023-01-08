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
import java.sql.Types;
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
    
    void addMedia(ArrayList<Media> medias, int tournamenId) {
        String sql = "insert into media (match_id, media_name, url_origin, url_preview)\n" +
                    "select m.id, ?, ?, ?\n" +
                    "from `match` m\n" +
                    "where m.team_home = (select t.id from team t where t.team_name = ?)\n" +
                    " and m.team_guest = (select t.id from team t where t.team_name = ?)\n" +
                    " and m.tour = ?\n" +
                    " and m.tournament_id = ?;";
        for(Media media : medias){
            for(Media.Image image : media.images){
                try {            
                    preparedStatement = connect.prepareStatement(sql);
                    preparedStatement.setString(1, image.name);
                    preparedStatement.setString(2, image.origin);
                    preparedStatement.setString(3, image.preview);
                    preparedStatement.setString(4, media.teamHome);
                    preparedStatement.setString(5, media.teamGuest);
                    preparedStatement.setString(6, media.tour);
                    preparedStatement.setInt(7, tournamenId);
//                    System.out.println(preparedStatement.toString());
                    boolean add = preparedStatement.execute();
//                    System.out.println("add = " + add);
                    connect.commit();
                } catch (SQLException ex) {
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    void addTeams(ArrayList<Team> teams) {
        String sql = "insert into team(league_id, team_name, team_url, team_image) "
                   + "values (2, ?, ?, ?)";
        try {
            for(Team t : teams){
                preparedStatement = connect.prepareStatement(sql);
                preparedStatement.setString(1, t.teamName);
                preparedStatement.setString(2, t.urlName);
                preparedStatement.setString(3, t.urlImage);
                preparedStatement.execute();
                connect.commit();
            }
            connect.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<Team> getTeamWithCountPlayers(){
        ArrayList<Team> teams = new ArrayList<>();
        try {
            String sql = " select t.team_name, t.team_url, count(1) cnt\n" +
                    " from team t " +
                    " join tournamnet_team tt on t.id = tt.team_id " +
                    " left join squad_actual sa on t.id = sa.team_id \n" +
                    " where t.league_id = 2 and tt.tournament_id = 7 \n" +
                    " group by t.team_name, t.team_url;";
            preparedStatement = connect.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String teamName = resultSet.getString(1);
                String teamUrl = resultSet.getString(2);
                int count = resultSet.getInt(3);
                teams.add(new Team(teamName, teamUrl, count));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                preparedStatement.close();
                resultSet.close();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return teams;
    }
    
    private void addActionGoal(int match_id, String player_url, String player_assist_url, String time) throws SQLException{
        String sql_dop;
        if(player_assist_url == null){
            sql_dop = ", ?";
        } else {
            sql_dop = ", (select id from player where player_url = ?) ";
        }
        String sql = "call addActionGoal("
                + "? "
                + ",(select id from player where player_url = ?) "
                + sql_dop
                + ",?)";
        preparedStatement = connect.prepareStatement(sql);
        preparedStatement.setInt(1, match_id);
        preparedStatement.setString(2, player_url);
        if(player_assist_url == null){
            preparedStatement.setNull(3, Types.INTEGER);
        } else {
            preparedStatement.setString(3, player_assist_url);
        }
        preparedStatement.setString(4, time);
        preparedStatement.execute();
    }
    
    public void addPlayerAction(Action action, int match_id) throws SQLException{
        if (action.action.equals("Гол") || action.action.equals("Стандарт")){
            //Обрабатываем только гол
            addActionGoal(match_id, action.urlPlayer,action.urlAssist, action.time);
        } else {
            String sql;
            switch(action.action){
                case "Пенальти": sql = "call addActionPenalty(?, (select id from player where player_url = ?), ?)"; break;
                case "Автогол": sql = "call addActionOwnGoal(?, (select id from player where player_url = ?), ?)"; break;
                case "Жёлтая карточка": sql = "call addActionYellowCard(?, (select id from player where player_url = ?), ?)"; break;
                case "Красная карточка": sql = "call addActionRedCard(?, (select id from player where player_url = ?), ?)"; break;
                default: sql = "call addActionPenaltyOut(?, (select id from player where player_url = ?), ?)"; break;
            }
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setInt(1, match_id);
            preparedStatement.setString(2, action.urlPlayer);
            preparedStatement.setString(3, action.time);
            preparedStatement.execute();
        }
    }
    
    
    void addPlayerAction(Match m, ArrayList<Player> playersHome, ArrayList<Player> playersGuest,  ArrayList<Action> actions) {
        String sqlInsertProtocol = "insert into player_in_match (match_id, player_id, team_id) "
                + "select ?, p.id, ? "
                + " from player p "
                + " where p.player_url = ? ";
        try{
            for(Player p : playersHome){
                preparedStatement = connect.prepareStatement(sqlInsertProtocol);
                preparedStatement.setInt(1, m.idmatch);
                preparedStatement.setInt(2, m.teamHomeId);
                preparedStatement.setString(3, p.urlName);
                try{
                    preparedStatement.execute();
                    System.out.println("Home");
                }catch (SQLException ex) {
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    connect.rollback();
                }
            }
            for(Player p : playersGuest){
                preparedStatement = connect.prepareStatement(sqlInsertProtocol);
                preparedStatement.setInt(1, m.idmatch);
                preparedStatement.setInt(2, m.teamGusetId);
                preparedStatement.setString(3, p.urlName);
                try{
                    preparedStatement.execute();
                    System.out.println("Guest");
                }catch (SQLException ex) {
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    connect.rollback();
                }
            }
            for(Action a :  actions){
                try{
                    System.out.println(a.toString());
                    addPlayerAction(a, m.idmatch);
                }catch(SQLException ex) {
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Action = " + a.toString());
                    System.out.println(preparedStatement.toString());
                    connect.rollback();
                    break;
                }
            }
            connect.commit();
        }catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    ArrayList<Match> getMatchesForParser(String tour){
        ArrayList<Match> matches = new ArrayList<>();
        String sql = "select id, match_url, team_home, team_guest "
                + " from `match` m where tour = ? and tournament_id = 7 and played = 2 "
                + " and 0 = (select count(1) from  player_in_match where match_id = m.id) ";
        try {
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, tour);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Match match = new Match();
                match.idmatch = resultSet.getInt("id");
                match.urlMatch = resultSet.getString("match_url");
                match.teamHomeId = resultSet.getInt("team_home");
                match.teamGusetId = resultSet.getInt("team_guest");
                matches.add(match);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matches;
    }
    
    void addedPlayers(String teamUrl, ArrayList<Player> players){
        String sqlInsertPLayer = "INSERT INTO player (league_id, name, surname, birthday, amplua_id, player_url, image_url)"
                + "SELECT 2, ?, ?, ?, (select id from amplua where short_name = ?), ?, ?";
        String sqlInsertSqua = "INSERT INTO squad_actual (player_id, team_id)" +
                "SELECT (select id from player where player_url = ?),(select id from team where team_url = ?)";
       
        for(Player p : players){
            try {
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
                }catch(SQLException ex){
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                preparedStatementPlayer = connect.prepareStatement(sqlInsertSqua);
                preparedStatementPlayer.setString(1, p.urlName);
                preparedStatementPlayer.setString(2, teamUrl);
                preparedStatementPlayer.executeUpdate();
                connect.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        String sqlAddMatch = "INSERT INTO `match` (tournament_id, team_home, team_guest, tour, played, match_url) " +
                                "select  7,\n" +
                                "(select id from team where league_id = 2 and team_url = ?) as team_home,\n" +
                                "(select id from team where league_id = 2 and team_url = ?) as team_guest,\n" +
                                "? as tour, " + 
                                "1, " +
                                "? as match_url";
        String sqlAddSchedule = "INSERT INTO `schedule` (stadium_id, league_id, game_date, match_id)\n" +
                                "SELECT 6, 2, ?, (select id from `match` where match_url = ?)" ;
        try{
            for(ParserSiteUCL.MatchLocal match : matches){
                preparedStatement = connect.prepareStatement(sqlAddMatch);
                preparedStatement.setString(1, match.teamHomeUrl);
                preparedStatement.setString(2, match.teamGuestUrl);
                preparedStatement.setString(3, match.tour);
                preparedStatement.setString(4, match.url);
                try{
                    preparedStatement.executeUpdate();
                    preparedStatementPlayer = connect.prepareStatement(sqlAddSchedule);
                    System.out.println(match.date+" "+match.time);
                    preparedStatementPlayer.setString(1, match.date+" "+match.time);
                    preparedStatementPlayer.setString(2, match.url);
                    preparedStatementPlayer.executeUpdate();
                }catch(SQLException ex){
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
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
