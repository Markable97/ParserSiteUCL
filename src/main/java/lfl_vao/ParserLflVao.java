/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Player;
import com.mycompany.parsersiteucl.Team;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
    
    
    public static void main(String[] args) throws IOException, SQLException, InterruptedException{
        parserAllMatch(TYPE_ACTION_SCHEDULE);
        //parserTeamSquad();
           //parserTeam();
    }
    
    /**
    * typeAction:
    * 1 - Добавление просто матчей
    * 2 - Добавление календаря
    * 3 - Добавление счета
    **/
    private static void parserAllMatch(int typeAction) throws IOException{
        DBRequest db = new DBRequest();
        ArrayList<String> tournamentsWithTours = db.getTournamnetWithCountTours();
        for(String tournament : tournamentsWithTours){
            System.out.println("-----------------------" + tournament + "------------------------------");
            String idTournamnet = tournament.replaceAll("\\D+","");
            String urlParser = "https://lfl.ru/?ajax=1&method=tournament_calendar_table&tournament_id=" +idTournamnet+ "&limit=400";
            Document doc = SSLHelper.getConnection(urlParser).get();
            Element tbody = doc.selectFirst("tbody");
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
                    db.addMatchWithResult(tournament, matches);
                    break;
            }
            System.out.println("-----------------------------------------------------");
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
