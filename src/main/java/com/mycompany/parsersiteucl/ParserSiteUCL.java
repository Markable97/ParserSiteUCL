/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
    
    public static void main(String[] args) throws IOException, SQLException, InterruptedException{
        System.out.println("Начало парсинга");
        parsingCalendar();
        //parsingTournamenttable();
        //parsingPlayersTeam();
        //System.out.println(divisions.toString());
        //DBRequest dbr = new DBRequest();
        //dbr.insertTeamAndPlayerAndTournamentUpdate(divisions);
        //System.out.println(teamsAll.toString());
        /*parsingMatches();
        dbr.insertMatchesAndDoingPlayers(matches);*/
        //downloadPictures();
    }
      
    static void parsingCalendar() throws IOException{
        File input = new File("D:\\Загрузки\\calendar.html");
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements schedules = doc.select("div.schedule__unit.js-calendar-matches-header");
        System.out.println(schedules.size());
        ArrayList<MatchLocal> mathes = new ArrayList<>();
        for(Element schedule : schedules) {
            String date = schedule.selectFirst("div.schedule__head").text();
            String dateStr =  parserTime(date);
            System.out.println("time = " + dateStr);
            Elements matchLi = schedule.select("li.schedule__matches-item.js-calendar-match");
            for(Element match : matchLi){
                MatchLocal matchLocal = parserMatch(match);
                System.out.println(matchLocal.toString());
                matchLocal.date = dateStr;
                mathes.add(matchLocal);
            }
        }
        DBRequest dbr = new DBRequest();
        dbr.addedMatches(mathes);
        
       
    }
    
    static class MatchLocal{
        String date;
        String time;
        String teamHome;
        String teamGuest;
        String tour;

        public MatchLocal(String time, String teamHome, String teamGuest, String tour) {
            this.time = time;
            this.teamHome = teamHome;
            this.teamGuest = teamGuest;
            this.tour = tour;
        }

        @Override
        public String toString() {
            return "MatchLocal{" + "time=" + time + ", teamHome=" + teamHome + ", teamGuest=" + teamGuest + ", tour=" + tour + '}';
        }
       
    }
    
    static MatchLocal parserMatch(Element match){
        String time = match.selectFirst("span.schedule__time").text();
        String teamhome = match.selectFirst("a.schedule__team-1").text();
        String teamGuest = match.selectFirst("a.schedule__team-2").text();
        String tour = match.selectFirst("span.schedule__tour-main").text();
        MatchLocal parserMatch = new MatchLocal(time, teamhome, teamGuest, tour);
        return parserMatch;
    }
    
//    time = 10 декабря, суббота
//    time = 17 декабря, суббота
//    time = 24 декабря, суббота
//    time = 14 января 2023, суббота
//    time = 21 января 2023, суббота
//    time = 28 января 2023, суббота
//    time = 4 февраля 2023, суббота
//    time = 11 февраля 2023, суббота
//    time = 18 февраля 2023, суббота
//    time = 25 февраля 2023, суббота
//    time = 4 марта 2023, суббота
    
    static String parserTime(String time){
        String[] parts = time.split(",");
        String[] partsTime = parts[0].split(" ");
        String returnStr;
        if(partsTime.length == 2) {
            returnStr = partsTime[0] + getNumberMonth(partsTime[1]) + "2022";
        } else {
            returnStr = partsTime[0] + getNumberMonth(partsTime[1]) + partsTime[2];
        }
        return returnStr;
    }
    
    static String getNumberMonth(String month){
        String number;
        switch(month){
            case "декабря": number = ".12."; break;
            case "января": number = ".01."; break;
            case "февраля": number = ".02."; break;
            case "марта": number = ".03.";break;
            default: number = ""; break;
        };
        return number;
    }
    
    static void downloadPictures() throws InterruptedException{
        MyThreadPicture myThread = null;
        //ArrayList<MyThreadPicture> list = new ArrayList<>();
        int cnt = teamsAll.size();
        int i = 1;
        for(Team t : teamsAll){
            if(i >= cnt - 16){
                myThread = new MyThreadPicture(t);
                myThread.start();
                //list.add(myThread);
                //myThread.join();
                //Thread.sleep(1000);
            }
            i++;
        }
        myThread.join();
    }
    
    static void parsingMatches() throws IOException{
        File input = new File("C:\\Users\\march\\OneDrive\\Рабочий стол\\test.html");
        //Document doc = Jsoup.connect("http://football.businesschampions.ru/osen-2019/raspisanie-chempionata/").get();
        Document doc = Jsoup.parse(input, "UTF-8");
        //Elements matches = doc.select("div.games-list__game-time");
        Elements trs = doc.getElementsByAttribute("data-match");
        System.out.println(trs.size());
        int i = 1;
        for(Element tr : trs){
            //Element divInfoTeam = tr.selectFirst("td.schedule-game-score");
            Elements aTeam = tr.getElementsByTag("a");
            String teamHome = aTeam.get(0).text();
            String teamGuest = aTeam.get(2).text();
            Element div = tr.selectFirst("div.games-list__game-time");
            Element a = div.select("a").first();
            int idMatch = Integer.parseInt(tr.attr("data-match")); 
            String urlMatch = a.attr("abs:href");
            String dateMatch = parsingDate(tr.selectFirst("td.schedule--date").text());
            System.out.println("id = " + idMatch + " url = " + urlMatch);
            Match match = new Match(idMatch, urlMatch, dateMatch);
            match.teamHome = teamHome;
            match.teamGuest = teamGuest;
            parsingMatchStatistic(match);
            matches.add(match);
            /*if(i == 2){
                break;
            }*/
            i++;
        }
        System.out.println("Кол-во матчей" + matches.size());
        System.out.println(matches.toString());
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
        String teamHome, teamGuest, strScore, strInfo;
        ArrayList<Player> players = new ArrayList<>();
        Document doc = Jsoup.connect(match.urlMatch).get();
        Element divMain = doc.selectFirst("div.nogame-info.nogame-info__match-page");
        Element divLeft = divMain.selectFirst("div.nogame-info__left-part");
        teamHome = match.teamHome;//divLeft.selectFirst("div.nogame-info__team-name").text();
        Element divCentre = divMain.selectFirst("div.nogame-info__center-part");
        //strDate = divCentre.selectFirst("div.nogame-info__match-name").text();
        strInfo = divCentre.selectFirst("div.nogame-info__match-stadium").text();
        parsingInfoMatch(match, strInfo);
        strScore = divCentre.selectFirst("div.nogame-score").text();
        parsingScore(match, strScore);
        Element divRight = divMain.selectFirst("div.nogame-info__right-part");
        teamGuest = match.teamGuest;//divRight.selectFirst("div.nogame-info__team-name").text();
        //match.teamHome = teamHome;
        //match.teamGuest = teamGuest;
        if(match.matchTransfer.equals("")){
           Element divTeams = doc.selectFirst("div.match-page__composition");
           //Хозяева
           Element divHome = divTeams.selectFirst("div.match-page__first-team-composition");
           Elements divsHomePlayer = divHome.select("div.first-team-composition__item");
           parsingSquad(divsHomePlayer, teamHome, players);
           //Гости
           Element divGuest = divTeams.selectFirst("div.match-page__second-team-composition");
           Elements divsGuestPlayer = divGuest.select("div.first-team-composition__item");
           parsingSquad(divsGuestPlayer, teamGuest, players);
           //Дейсвтия игроков (Голы и Ассисты)
           Element divDoing = doc.selectFirst("div.match-page--doing__block");
           parsingDoingGoalAndAssist(divDoing, players, teamHome, teamGuest);
           //Действия игроков (Карточки)
           divDoing = doc.selectFirst("div.match-page--doing.match-page--doing__punishment");
           parsingDoingPunishment(divDoing, players);
           match.players.addAll(players);
        }
    }
    
    static void parsingDoingPunishment(Element divDoing, ArrayList<Player> players){
        if(divDoing != null){
            Element info = divDoing.selectFirst("div.match-page--doing__block");
            if(info != null){
                Element divHome = info.selectFirst("div.match-page--doing__items__left-part");
                Element divGuest = info.selectFirst("div.match-page--doing__items__right-part");
                //Хозяева
                Elements divItemHome = divHome.getElementsByClass("match-page--doing__item clearfix");
                for(Element div : divItemHome){
                   Element nameInfo = div.selectFirst("div.match-page--doing__item-name-1"); 
                    if(nameInfo != null){
                        Element n = nameInfo.getElementsByTag("a").first();
                        String urlName = n.attr("abs:href");
                        String name = n.text();
                        String type = div.selectFirst("div.match-page--doing__item--score").getElementsByTag("a").attr("title");
                        System.out.println(type + ": " + name);
                        findPlayer(players, urlName, type);
                    }
                }
                //Гости
                Elements divItemGuest = divGuest.getElementsByClass("match-page--doing__item match-page--doing__item__right clearfix");
                for(Element div : divItemGuest){
                   Element nameInfo = div.selectFirst("div.match-page--doing__item-name-1"); 
                    if(nameInfo != null){
                        Element n = nameInfo.getElementsByTag("a").first();
                        String urlName = n.attr("abs:href");
                        String name = n.text();
                        String type = div.selectFirst("div.match-page--doing__item--score").getElementsByTag("a").attr("title");
                        System.out.println(type + ": " + name);
                        findPlayer(players, urlName, type);
                    }
                }
            }
        }
    }
    
    static void parsingDoingGoalAndAssist(Element divDoing, ArrayList<Player> players, String teamHome, 
            String teamGuest){
        if(divDoing != null){
            Element divHome = divDoing.selectFirst("div.match-page--doing__items__left-part");
            Element divGuest = divDoing.selectFirst("div.match-page--doing__items__right-part");
            //Голы и Асисты
            //Хозяева
            Elements divItemHome = divHome.select("div.match-page--doing__item.clearfix");
            for(Element div : divItemHome){
                Element info = div.selectFirst("div.match-page--doing__item--names");
                Element goal = info.getAllElements().first().getElementsByTag("a").first();//selectFirst("div.match-page--doing__item-name-1 ");
                if (goal != null){
                    Element goalType = div.selectFirst("div.match-page--doing__item--score-image");
                    String type = goalType.selectFirst("a").attr("title");
                    String name = goal.text();
                    String urlName = goal.attr("abs:href");
                    if(type.equals("Автогол")){
                        urlName = "Автогол " + teamHome;
                    }
                    findPlayer(players, urlName, type);
                    System.out.print(type + " = " + goal.text());
                }
                Element assist = info.getAllElements().last().getElementsByTag("a").first();//selectFirst("div.match-page--doing__item-name-2");
                if (assist != null){
                    String name = assist.text();
                    String urlName = assist.attr("abs:href");
                    if(name.length() > 0){
                        findPlayer(players, urlName, "Асист");
                        System.out.println(" Assist = " + name);
                    }else{
                        System.out.println();
                    }
                }

            }
            //Гости
            Elements divItemGuest = divGuest.getElementsByClass("match-page--doing__item match-page--doing__item__right clearfix");
            for(Element div : divItemGuest){
                Element info = div.selectFirst("div.match-page--doing__item--names");
                Element goal = info.getAllElements().first().getElementsByTag("a").first();//selectFirst("div.match-page--doing__item-name-1 ");
                if (goal != null){
                    Element goalType = div.selectFirst("div.match-page--doing__item--score-image");
                    String type = goalType.selectFirst("a").attr("title");
                    String name = goal.text();
                    String urlName = goal.attr("abs:href");
                    if(type.equals("Автогол")){
                        urlName = "Автогол " + teamGuest;
                    }
                    findPlayer(players, urlName, type);
                    System.out.print(type + " = " + goal.text());
                }
                Element assist = info.getAllElements().last().getElementsByTag("a").first();//selectFirst("div.match-page--doing__item-name-2");
                if (assist != null){
                    String name = assist.text();
                    String urlName = assist.attr("abs:href");
                    if(name.length() > 0){
                        findPlayer(players, urlName, "Асист");
                        System.out.println(" Assist = " + name);
                    }else{
                        System.out.println();
                    } 
                }
            }
        }
    }
    
    static void findPlayer(ArrayList<Player> players, String urlName, String typeDoing){
        for(Player p : players){
            if(p.urlName.equals(urlName)){
                switch(typeDoing){
                    case "Гол": 
                        p.goal = p.goal + 1;
                        break;
                    case "Реализованный 10-ти метровый штрафной удар":
                        p.penalty = p.penalty + 1;
                        break;
                    case "Асист":
                        p.assist = p.assist + 1;
                        break;
                    case "Желтая карточка":
                        p.yellow = p.yellow + 1;
                        break;
                    case "Вторая желтая карточка":
                        p.yellow = p.yellow + 1;
                        break;
                    case "Красная карточка":
                        p.red = p.red + 1;
                        break;
                    case "Автогол":
                        p.ownGoal = p.ownGoal + 1;
                        break;
                    
                }
                break;
            }
        }
    }
    
    static void parsingSquad(Elements divsPlayer, String teamName, ArrayList<Player> players){
        String urlName, name;
        for(Element p : divsPlayer){
            Element playerInfo = p.selectFirst("div.first-team-composition__player-name");
            name = playerInfo.text();
            urlName = playerInfo.select("a").attr("abs:href");
            Player player = new Player(teamName, name, urlName);
            Element amplua = p.selectFirst("div.first-team-composition__player-role");
            Element a = amplua.getElementsByTag("a").first();
            if(a!=null){
                player.amplua = "Вратарь";
            }else{
                player.amplua = "Полевой игрок";
            }
            players.add(player);
        }
        players.add(new Player(teamName, "Автогол " + teamName, "Автогол " + teamName, "Автогол"));
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
            match.matchTransfer = "";
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
