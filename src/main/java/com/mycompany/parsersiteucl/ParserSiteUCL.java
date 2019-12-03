/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author m.glushko
 */
public class ParserSiteUCL {
    
    static ArrayList<Team> teamsAll = new ArrayList<>();
    static ArrayList<TournamentTable> divisions = new ArrayList<>();
    static ArrayList<Match> matches = new ArrayList<>();
    
    public static void main(String[] args) throws IOException{
        System.out.println("Начало парсинга");
        //parsingTournamenttable();
        //parsingPlayersTeam();
        //System.out.println(teamsAll.toString());
        parsingMatches();
    }
    
    static void parsingMatches() throws IOException{
        File input = new File("C:\\Users\\m.glushko\\Desktop\\Новая папка (2)\\test.html");
        //Document doc = Jsoup.connect("http://football.businesschampions.ru/osen-2019/raspisanie-chempionata/").get();
        Document doc = Jsoup.parse(input, "UTF-8");
        //Elements matches = doc.select("div.games-list__game-time");
        Elements trs = doc.getElementsByAttribute("data-match");
        System.out.println(trs.size());
        for(Element tr : trs){
            Element div = tr.selectFirst("div.games-list__game-time");
            Element a = div.select("a").first();
            int idMatch = Integer.parseInt(tr.attr("data-match")); 
            String urlMatch = a.attr("abs:href");
            String dateMatch = parsingDate(tr.selectFirst("td.schedule--date").text());
            Match match = new Match(idMatch, urlMatch, dateMatch);
            parsingMatchStatistic(match);
            matches.add(match);
            System.out.println("id = " + idMatch + " url = " + urlMatch);
            System.out.println(matches.toString());
            break;
        }
    }
    /*01.10, 22:10*/
    static String parsingDate(String str){
        String[] strMain = str.split(",");
        String str1 = strMain[0].trim(); //before ','
        String str2 = strMain[1].trim(); //after ','
        String[] date = str1.split("\\.");
        String year = "2019";
        String mounth = date[1];
        String day = date[0];
        return year + '-' + mounth + '-' + day + " " + str2;
    }
    
    static void parsingMatchStatistic(Match match) throws IOException{
        String teamHome, teamGuest, strDate, strScore, strInfo;
        Document doc = Jsoup.connect(match.urlMatch).get();
        Element divMain = doc.selectFirst("div.nogame-info.nogame-info__match-page");
        Element divLeft = divMain.selectFirst("div.nogame-info__left-part");
        teamHome = divLeft.selectFirst("div.nogame-info__team-name").text();
        Element divCentre = divMain.selectFirst("div.nogame-info__center-part");
        //strDate = divCentre.selectFirst("div.nogame-info__match-name").text();
        strInfo = divCentre.selectFirst("div.nogame-info__match-stadium").text();
        parsingInfoMatch(match, strInfo);
        strScore = divCentre.selectFirst("div.nogame-score").text();
        parsingScore(match, strScore);
        Element divRight = divMain.selectFirst("div.nogame-info__right-part");
        teamGuest = divRight.selectFirst("div.nogame-info__team-name").text();
        match.teamHome = teamHome;
        match.teamGuest = teamGuest;
        if(match.matchTransfer.isEmpty()){
           Element divTeams = doc.selectFirst("div.match-page__composition");
           Element divHome = divTeams.selectFirst("div.match-page__first-team-composition");
           Elements divsHomePlayer = divHome.select("div.first-team-composition__item");
           for(Element p : divsHomePlayer){
               
           }
           Element divGuest = divTeams.selectFirst("div.match-page__second-team-composition");
           Elements divsGuestPlayer = divGuest.select("div.first-team-composition__item");
        }
    }
    /*Дивизион "A". Тур 1. Поле №1.*/
    static void parsingInfoMatch(Match match, String str){
        String[] strMain = str.split("\\.");
        match.stadium = strMain[2].trim();
        match.tour = Integer.parseInt(strMain[1].replace("Тур ", "").trim());
        match.division = strMain[0];
    }
    /*0:5 T*/
    static void parsingScore(Match match, String str){
        String[] strMain = str.split(":");
        int goalHome = Integer.parseInt(strMain[0].trim());
        match.goalHome = goalHome;
        if (strMain[1].contains("T")){
            match.goalGuest = Integer.parseInt(strMain[1].replace("T", "").trim());
            match.matchTransfer = "Техническое поражение";
        }else{
            match.goalGuest = Integer.parseInt(strMain[1].trim());
        }
        
    }
    static void parsingPlayersTeam() throws IOException{
        for(Team t : teamsAll){
            ArrayList<Player> players = new ArrayList<>();
            Document doc = Jsoup.connect(t.urlName).get();
            Elements divsP = doc.select("div.team-list__player");
            for(Element divP : divsP){
                Element divInfoPlayer = divP.selectFirst("div.team-list__player__name");
                Element a = divInfoPlayer.selectFirst("a");
                String name = a.text();
                String urlName = a.attr("abs:href");
                Player player = new Player(t.teamName, name, urlName);
                players.add(player);
            }
            System.out.println("Team: " + t.teamName + " count: " + players.size());
            t.players.addAll(players);
        }
        
    }
    
    static void parsingTournamenttable() throws IOException{
        
        String url = "http://football.businesschampions.ru/osen-2019/turnirnye-tablitsy/";
        Document doc = Jsoup.connect(url).get();
        Elements tables = doc.select("table.tourment-table__in-tourment-tables");
        System.out.println("Кол-во таблиц = " + tables.size());
        for(Element t : tables){ 
            Element division = t.selectFirst("thead").selectFirst("tr.thead__tr").selectFirst("th.table-item__title.table-item__title_text-left");
            //System.out.println(division.html()); 
            String nameDivision = division.text();
            System.out.println("Дивизион - " + nameDivision);
            Element tbody = t.selectFirst("tbody");
            Elements trs = tbody.select("tr");
            for(Element tr : trs){
                //Данные о команде
                Element tdName = tr.selectFirst("td.team--name");
                String teamName = tdName.selectFirst("a").attr("title");
                String urlName = tdName.selectFirst("a").attr("abs:href");
                Team team = new Team(nameDivision, teamName, urlName);
                teamsAll.add(team);
                //Данные для турнирной таблицы
                int points = Integer.parseInt(tr.selectFirst("td.team--games").text());
                String balls = tr.selectFirst("td.team--balls").text();
                int games = 0, wins = 0, draws = 0, losses = 0;
                int i = 1;
                for(Element td : tr.select("td.game--result")){
                    switch(i){
                        case 1: games = Integer.parseInt(td.text()); break;
                        case 2: wins = Integer.parseInt(td.text()); break;
                        case 3: draws = Integer.parseInt(td.text()); break;
                        case 4: losses = Integer.parseInt(td.text()); break;
                    }
                    i++;
                }
                TournamentTable teamTable = new TournamentTable(nameDivision, team, games, wins, draws, losses, balls, points);
                divisions.add(teamTable);
            }
            
        }
        //System.out.println(teamsAll.toString());
    
    }
}
