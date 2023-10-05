/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Action;
import com.mycompany.parsersiteucl.ParserSiteUCL;
import com.mycompany.parsersiteucl.Player;
import com.mycompany.parsersiteucl.Team;
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
    
    void addSchedule(String tournament, ArrayList<MatchLocal> matches) {
        String sql = "call addScheduleWithMatchUrl(3, ?, ?, ?)";
        matches.forEach((match) -> {
            if( !(match.stadium.equals("-") && match.date.equals("-") && match.time.equals("-"))){
                try {
                    PreparedStatement preparedStatement = connect.prepareStatement(sql);
                    preparedStatement.setString(1, match.stadium);
                    preparedStatement.setString(2, match.date + " " + match.time);
                    preparedStatement.setString(3, match.url);
                    preparedStatement.executeUpdate();
                    connect.commit();
                    System.out.println("матч добавлен +++++++++++++++++++++++");
                } catch (SQLException ex) {
                    System.out.println("матч уже был добавлен в расписание !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("матч не подходит под добавления ----------------------------");
            }
        });
    }

    void updateScore(ArrayList<MatchLocal> matches){
        String sql = "call matchAddResult((select id from `match` where match_url = ?), ?, ?);";
        for(MatchLocal match : matches){
            try{
                PreparedStatement preparedStatement;
                if(!match.goalsHome.equals("-")){
                    try{
                       int goalsHome = Integer.parseInt(match.goalsHome);
                       int goalsGuest = Integer.parseInt(match.goalsGuest);
                       try {
                            preparedStatement = connect.prepareStatement(sql);
                            preparedStatement.setString(1, match.url);
                            preparedStatement.setInt(2, goalsHome);
                            preparedStatement.setInt(3, goalsGuest);
                            preparedStatement.executeUpdate();
                            connect.commit();
                        } catch (SQLException ex) {
                            Logger.getLogger(com.mycompany.parsersiteucl.DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }catch(NumberFormatException ex){
                        connect.rollback();
                        break;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(com.mycompany.parsersiteucl.DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
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
//                    "and t.url = '/tournament19160'\n" +
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
    
    void updatePlayersUrl(Team team, ArrayList<Player> players) {
        String sql = "update player\n" +
                    "set player_url = ?\n" +
                    "where league_id = 3\n" +
                    "and id in (select sa.player_id from squad_actual sa where sa.team_id = ?)\n" +
                    "and name = ? and surname = ?;";
        
            for(Player p : players){
                try {
                    PreparedStatement preparedStatement = connect.prepareStatement(sql);
                    preparedStatement.setString(1, p.urlName);
                    preparedStatement.setInt(2, team.id);
                    String[] name = p.name.split(" ");
                    preparedStatement.setString(3, name[1]);
                    preparedStatement.setString(4, name[0]);
                    int rows = preparedStatement.executeUpdate();
                    if(rows == 0) {
                        System.out.println("not found player = " + p);
                        ParserLflVao.getPersonInfoMainPage(p);
                        if(p.urlPictures != null) {
                            System.out.println("Добавляем игрока ");
                            ArrayList<Player> onePlayer = new ArrayList<>();
                            onePlayer.add(p);
                            addedPlayers(team.urlName, onePlayer);
                        }
                    }
                    if(rows >  1) {
                        System.out.println("multy found player = " + team.id + " " + p);
                    }
                    connect.commit();
                } catch (SQLException ex) {
                    System.out.println("Уже добавлен " + ex.getLocalizedMessage() + " " + p);
    //                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
            }
            
            
        
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
                System.out.println("Добавление  " + p.toString());
                try{
                    preparedStatement.executeUpdate();
                }catch(SQLException ex){
                    System.out.println("Уже добавлен " + ex.getLocalizedMessage());
//                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                PreparedStatement preparedStatementPlayer = connect.prepareStatement(sqlInsertSqua);
                preparedStatementPlayer.setString(1, p.urlName);
                preparedStatementPlayer.setString(2, teamUrl);
                try{
                    preparedStatementPlayer.executeUpdate();
                }catch(SQLException ex){
                    System.out.println("Уже добавлен в состав " + ex.getLocalizedMessage());
//                    Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
                }
                connect.commit();
            } catch (SQLException ex) {
                Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    ArrayList<Team> getTeams(String urlTournament){
        String sql = "select tm.*, t.url\n" +
                    "from tournament t, tournamnet_team tt, team tm\n" +
                    "where tt.team_id = tm.id\n" +
                    "and tt.tournament_id = t.id\n" +
                    "and tm.league_id = 3 #and tm.id = 151\n";
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
                int teamId = resultSet.getInt("id");
                String teamName = resultSet.getString("team_name");
                String teamUrl = resultSet.getString("team_url");
                String tournamnetUrl = resultSet.getString("t.url");
                Team team = new Team();
                team.id = teamId;
                team.teamName = teamName;
                team.urlName = teamUrl;
                team.urlTournament = tournamnetUrl;
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

    ArrayList<String> getAssignTournamentMatches(String urlTournament) {
        ArrayList<String> urls = new ArrayList<>();
       String sqlDop;
        if(urlTournament != null) {
            sqlDop = " and t.url = ? ";
        } else {
            sqlDop ="";
        }
        String sql = "select m.match_url\n" +
                    "from `match` m\n" +
                    "where m.tournament_id in (select t.id from tournament t, season s where t.season_id = s.id and s.league_id = 3 "+ sqlDop +" )\n" +
                    "and played = 1 ";
        try {
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            if(urlTournament != null) {
                preparedStatement.setString(1, urlTournament);
            }
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

    ArrayList<MatchForParser> getMatchesForParsingAction(String utlTournament){
        ArrayList<MatchForParser> matches = new ArrayList<>();
        String sql = "select m.id, match_url\n" +
                        "from `match` m\n" +
                        "where m.tournament_id in (\n" +
                        "		select t.id \n" +
                        "        from tournament t, season s \n" +
                        "        where t.season_id = s.id and s.league_id = 3 and t.url = ?\n" +
                        ")\n" +
//                        "and m.match_url in ('/match3015526')";
                        "and played = 2 and (select count(1) from player_in_match where match_id = m.id) = 0";
        try {
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, utlTournament);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Long id = resultSet.getLong(1);
                String url = resultSet.getString(2);
                MatchForParser match = new MatchForParser(url, id);
                matches.add(match);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matches;
    }
    
    void addMatchAction(MatchForParser match) throws SQLException {
        
        addInProtocol(match.players, match.id);
        addPlayerAction(match.actions, match.id);
        connect.commit();
    }
    
    private void addInProtocol(ArrayList<Player> players, long matchId) throws SQLException{
        String sqlInsertProtocol = "insert into player_in_match (match_id, player_id, team_id) "
                + "select ?, p.id, (select id from team t where t.team_url = ?)"
                + " from player p "
                + " where p.player_url = ? ";
        for(Player player : players) {
            PreparedStatement preparedStatement = connect.prepareStatement(sqlInsertProtocol);
            preparedStatement.setLong(1, matchId);
            preparedStatement.setString(2, player.teamUrl); 
            preparedStatement.setString(3, player.urlName); 
            System.out.println(preparedStatement.toString());
            try{
                preparedStatement.execute();    
            } catch (SQLException ex) {
                System.out.println("Игрок добавлен " + player + " " + ex.getLocalizedMessage());
                continue;
            }
            
        }
        connect.commit();
    }
    
    private void addActionGoal(long match_id, Action action, int time, int countAction) throws SQLException{
        String sql_dop;
        if(action.urlAssist == null){
            sql_dop = ", ?";
        } else {
            sql_dop = ", (select id from player where player_url = ?) ";
        }
        String sql = "call addActionGoal("
                + "? "
                + ",(select id from player where player_url = ?) "
                + sql_dop
                + ",?)";
        int index = time;
        for(int c = 0; c < countAction; c++){
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setLong(1, match_id);
            preparedStatement.setString(2, action.urlPlayer);
            if(action.urlAssist == null){
                preparedStatement.setNull(3, Types.INTEGER);
            } else {
                preparedStatement.setString(3, action.urlAssist);
            }
            String timeStr;
            if(action.time != null && !action.time.isBlank()){
                timeStr = action.time;
            } else {
                timeStr = String.valueOf(index + c + 1);
            }
            preparedStatement.setString(4, timeStr);
            try{
                System.out.println(preparedStatement.toString());
                preparedStatement.execute();
            } catch (SQLException ex) {
                System.out.println("Действие добавлено " + ex.getLocalizedMessage());
                continue;
            }
            
        }
        
    }
    
    private void addPlayerAction(ArrayList<Action> actions, long match_id) throws SQLException {
        int index = 0;
        for(Action action : actions) {
                if (action.action.equals("Гол") || action.action.equals("Стандарт")){
                    //Обрабатываем только гол
                    addActionGoal(match_id, action, index, action.countAction);
                    index += 2*action.countAction+1;
                } else {
                    String sql;
                    switch(action.action){
                        case "Пенальти": sql = "call addActionPenalty(?, (select id from player where player_url = ?), ?)"; break;
                        case "Автогол": sql = "call addActionOwnGoal(?, (select id from player where player_url = ?), ?)"; break;
                        case "Желтая карточка": sql = "call addActionYellowCard(?, (select id from player where player_url = ?), ?)"; break;
                        case "Красная карточка": sql = "call addActionRedCard(?, (select id from player where player_url = ?), ?)"; break;
                        case "Передача": sql = "call addAssist(?, (select id from player where player_url = ?), ?)"; break;
                        default: sql = "call addActionPenaltyOut(?, (select id from player where player_url = ?), ?)"; break;
                    }
                    for(int c = 0; c < action.countAction; c++){
                        index = index + c + 1;
                        PreparedStatement preparedStatement = connect.prepareStatement(sql);
                        preparedStatement.setLong(1, match_id);
                        preparedStatement.setString(2, action.urlPlayer);
                        String timeStr;
                        if(action.time != null && !action.time.isBlank()) {
                            timeStr = action.time;
                        }else {
                            timeStr = String.valueOf(index);
                        }
                        preparedStatement.setString(3, timeStr);
                        try{
                            System.out.println(preparedStatement.toString());
                            preparedStatement.execute();
                        } catch (SQLException ex) {
                            System.out.println("Действие уже добавлено " + ex.getLocalizedMessage());
                            continue;
                        }
                    } 
                    
                }
            index++;
        }
    }
}
