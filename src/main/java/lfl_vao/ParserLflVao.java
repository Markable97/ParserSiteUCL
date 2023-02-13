/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Action;
import com.mycompany.parsersiteucl.Player;
import com.mycompany.parsersiteucl.Team;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class ParserLflVao {
    
    private final static int TYPE_ACTION_INFO = 1;
    private final static int TYPE_ACTION_SCHEDULE = 2;
    private final static int TYPE_ACTION_RESULT = 3;
    
//    https://lfl.ru/?ajax=1&method=player_stats_table&tournament_id=0&player_id=11471&season_id=45
//    Штука через, которую будем проверять действия в матчах
    
    
    public static void main(String[] args) throws IOException, SQLException, InterruptedException{
//        updateUrlSquad();
        parserResultActions();
//        parserAllMatch(TYPE_ACTION_RESULT);
//        parserTeamSquad();
           //parserTeam();
    }
    
    /**
    * urlTournament - ссылка турнира, если отсутсвует, все матчи
    * https://lfl.ru/tournament18633 - Высший
    * https://lfl.ru/tournament18634 - Первый
    * https://lfl.ru/tournament18635 - 2А
    * https://lfl.ru/tournament18636 - 2В
    * https://lfl.ru/tournament18741 - Третий
    **/
    private static void parserResultActions(){
        DBRequest db = new DBRequest();
        String urlTournament = "/tournament18741";
        boolean isHighDivision = false;
        ArrayList<String> errorUrls = new ArrayList<>();
        ArrayList<MatchForParser> matches = db.getMatchesForParsingAction(urlTournament);
        //matches.add(new MatchForParser("/match3013562", 1));
        matches.forEach((match) -> {
            try {
                System.out.println("-----------------------" + match.url + "------------------------------");
                String urlParser = "https://lfl.ru" + match.url;
                Document doc = SSLHelper.getConnection(urlParser).get();
                Element divMatchRight = doc.selectFirst("div.match_right");
                Boolean isCheckTypeNextBlock = true;
                int type = ParserLflHelper.TYPE_UNKNOWS;
                ArrayList<Player> players = new ArrayList<>();
                ArrayList<Action> acctions = new ArrayList<>();
                for(Element element : divMatchRight.children()){
                    if(isCheckTypeNextBlock) {
                        String textElement = element.text();
                        String textTag = element.normalName();
                        type = ParserLflHelper.typeNextBlock(textTag, textElement);
                        isCheckTypeNextBlock = false;
                    } else {
                        isCheckTypeNextBlock = true;
                        switch(type){
                            case ParserLflHelper.TYPE_GOALS:
                                System.out.println("It is goals");
                                if(isHighDivision) {
                                    acctions.addAll(ParserLflHelper.parserActionsGoalsHighDivision(element));
                                } else {
                                    acctions.addAll(ParserLflHelper.parserActionGoals(element));
                                }
                                break;
                            case ParserLflHelper.TYPE_ASSISTENTS: 
                                System.out.println("It is assists");
                                acctions.addAll(ParserLflHelper.parserActionAssists(element));
                                break;
                            case ParserLflHelper.TYPE_SQUADS: 
                                System.out.println("It is squad");
                                if(isHighDivision) {
                                    players = ParserLflHelper.parserProtocolHighDivision(element);
                                } else {
                                    players = ParserLflHelper.parserProtocol(element);
                                }
                                break;
                            case ParserLflHelper.TYPE_WARNINGS: 
                                System.out.println("It is warnings");
                                if(isHighDivision) {
                                    acctions.addAll(ParserLflHelper.parserActionWarningsHighDivision(element));
                                } else {
                                    acctions.addAll(ParserLflHelper.parserActionWarnings(element));
                                }
                                break;
                            case ParserLflHelper.TYPE_PENALTY_OUT:
                                System.out.println("It is penalty out");
                                acctions.addAll(ParserLflHelper.parserActionPenaltyOut(element));
                                break;
                            default: 
                                System.out.println("It is unknows");
                                break;
                        }
                    }
                }
                match.players = players;
                match.actions = acctions;
                db.addMatchAction(match);
                System.out.println("Чутка подождем, чтобы не забанили");
                Thread.sleep(5000);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ParserLflVao.class.getName()).log(Level.SEVERE, null, ex);
                errorUrls.add(match.url);
            } 
            catch (SQLException ex) {
                errorUrls.add(match.url + " db");
                Logger.getLogger(ParserLflVao.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        System.out.println(errorUrls);
    }
    
    /**
    * typeAction:
    * 1 - Добавление просто матчей
    * 2 - Добавление календаря
    * 3 - Добавление счета
    **/
    private static void parserAllMatch(int typeAction) throws IOException{
        DBRequest db = new DBRequest();
        String method;
        switch(typeAction){
            case TYPE_ACTION_INFO:
                method = "";
                break;
            case TYPE_ACTION_SCHEDULE: 
                method = "tournament_calendar_table";
                break;
            case TYPE_ACTION_RESULT: 
                    method = "tournament_resault_table";
                break;
            default: 
                method = ""; 
                break;
        }
        ArrayList<String> tournamentsWithTours = db.getTournamnetWithCountTours();
        for(String tournament : tournamentsWithTours){
            System.out.println("-----------------------" + tournament + "------------------------------");
            String idTournamnet = tournament.replaceAll("\\D+","");
            String urlParser = "https://lfl.ru/?ajax=1&method="+method+"&tournament_id=" +idTournamnet+ "&limit=400";
            Document doc = SSLHelper.getConnection(urlParser).get();
            Element tbody = doc.selectFirst("tbody");
            if(tbody != null) {
                Elements trs = tbody.select("tr");
                ArrayList<MatchLocal> matches = new ArrayList<>();
                for(Element tr : trs){
                    if(tr.children().size() == 1) {
                        //Строка тура
                        System.out.println(tr.text());
                    } else {
                        MatchLocal match = new MatchLocal();
                        match.parserMatchInfoLfl(tr);
                        matches.add(match);
                        System.out.println(match);
                    }
                }
                System.out.println("Добавление в базу");
                switch(typeAction){
                    case TYPE_ACTION_INFO: 
                        db.addOnlyMatchesInfo(tournament, matches);
                        break;
                    case TYPE_ACTION_SCHEDULE: 
                        db.addSchedule(tournament, matches);
                        break;
                    case TYPE_ACTION_RESULT: 
                        db.updateScore(matches);
                        break;
                }
            }
            System.out.println("-----------------------------------------------------");
        }
    }
    
    static void getPersonInfoMainPage(Player player){
        String urlParser = "https://lfl.ru" + player.urlName;
        System.out.println("Parsing main page");
        try {
            Document doc = SSLHelper.getConnection(urlParser).get();
            Element logo = doc.selectFirst("div.player_logo").selectFirst("a");
            player.urlPictures = logo.attr("href");
            Elements info = doc.selectFirst("div.player_title").getElementsByTag("p");
            player.name = info.get(0).text().trim();
            player.birthday = info.get(2).text().replace("Дата рождения:", "").trim();
            player.amplua = info.get(3).selectFirst("a").attr("title");
        } catch (Exception ex) {
            Logger.getLogger(ParserLflVao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void updateUrlSquad() throws IOException {
        DBRequest db = new DBRequest();
        String urlTournament = "/tournament18741"; 
        ArrayList<Team> teams = db.getTeams(urlTournament);
        for(Team team : teams){
            String urlParser = "https://lfl.ru"+team.urlName;
            System.out.println(team.teamName);
            Document doc = SSLHelper.getConnection(urlParser).get();
            Elements divPlayers = doc.select("div.player");
            ArrayList<Player> players = new ArrayList<>();
            if(divPlayers != null) {
                for(Element divPlayer : divPlayers){
                    Player player = new Player();
                    Element a = divPlayer.selectFirst("a");
                    player.parserLflInMainPageTeam(a);
    //                System.out.println(player);
                    players.add(player);
                }
                db.updatePlayersUrl(team, players);
            }
            
        }
    }
    
    /**
    *
    * urlTournament - если null возьмет все команды для данной лиги, если указать, для конкретного дивизиона
    */
    private static void parserTeamSquad() throws IOException{
        DBRequest db = new DBRequest();
        String urlTournament = "/tournament18741"; 
        ArrayList<Team> teams = db.getTeams(urlTournament);
        for(Team team : teams){
            String urlParser = "https://lfl.ru"+team.urlName+"/players_list";
            Document doc = SSLHelper.getConnection(urlParser).get();
            Elements divPlayers = doc.select("div.cont.with_simple_text");
            ArrayList<Player> players = new ArrayList<>();
            for(Element divPlayer : divPlayers){
                if(divPlayer.selectFirst("div.player_title") != null){
                    Player player = new Player();
                    player.parserLfl(divPlayer);
                    System.out.println(player);
                    players.add(player);
                } else {
                  System.out.println("Не подходит первая фигня");
                }
            }
            db.addedPlayers(team.urlName, players);
        }
    }
    
    private static void parserTeam() throws IOException{
        DBRequest db = new DBRequest();
        ArrayList<String> urlsTournament = db.getTournamentUrl();
        for(String url : urlsTournament){
            ArrayList<Team> teams = new ArrayList<>();
            String urlParser = "https://lfl.ru/?ajax=1&method=tournament_progress_table&tournament_id=" + url.replaceAll("\\D+","");
            System.out.println(urlParser);
           
            Document doc = SSLHelper.getConnection(urlParser).get();
            Element table = doc.selectFirst("table");
            Elements trs = table.select("tr");
            for(Element tr : trs){
                Team team = new Team();
                team.parserTeamLfl(tr);
                if (team.isTeam()){
                    teams.add(team);
                }
            }
            System.out.println(teams);
            db.addTeams(teams, url);
        }
        db.close();
    }
 
}
