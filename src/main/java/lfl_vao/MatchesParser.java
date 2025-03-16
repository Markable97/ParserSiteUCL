/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lfl_vao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class MatchesParser {
    private final Connection dbConnect;

    public MatchesParser() {
        dbConnect = new DBConnection().connect;
    }
    
    void parserExider() throws IOException, SQLException {
        Document doc = SSLHelper.getConnection("https://exider.org/").get();
        ArrayList<MatchLocal> calendar = new ArrayList<>();
        Element divMain = doc.selectFirst("div.main");
        Elements intoMain = divMain.children();
        int countMainPlashka = 0;
        String classNameMainPlashka = "main-plashka";
        boolean isNeedContent = false;
        for(Element el : intoMain) {
            System.out.println("Ищем контент " + el.className());
            if(el.className().equals(classNameMainPlashka)) {
                System.out.println("Нащли плашку");
                countMainPlashka++;
                isNeedContent = countMainPlashka == 1;
                if(countMainPlashka > 1) {
                    System.out.println("Дальше все");
                    break;
                }
            }
            if(isNeedContent && !el.className().contains(classNameMainPlashka)) {
                System.out.println("Подходящитй контент");
                Elements liS = el.getElementsByTag("li");
                if(liS == null) {
                    System.out.println("Ошибка пропускаем элемент, не нашлись теги <li>");
                    continue;
                }
                if(liS.isEmpty()) {
                    System.out.println("Ошибка пропускаем элемент, не нашлись теги <li> [2]");
                    continue;
                }
                for(Element li : liS) {
                    Elements h2 = li.getElementsByTag("h2");
                    if(h2 == null) {
                        System.out.println("Ошибка пропускаем элемент, не нашелся заголовок <h2>");
                        continue;
                    }
                    if(h2.isEmpty()) {
                        System.out.println("Ошибка пропускаем элемент, не нашелся заголовок <h2> [2]");
                        continue;
                    }
                    String title = h2.first().text();
                    System.out.println("Заголовок = " + title);
                    String[] titleSplit = title.split("-");
                    if(titleSplit.length != 2) {
                        System.out.println("Предупреждение! Отсутсвует тур");
                    }
                    String divisionName = titleSplit[0].trim();
                    String tour = "";
                    if(titleSplit.length == 2) {
                        tour = titleSplit[1].trim();
                    }
                    Element table = li.selectFirst("table.on_main");
                    if(table == null) {
                        System.out.println("Ошибка пропускаем элемент, не нашелся тег table.on_mai");
                        continue;
                    }
                    Elements trS = table.select("tr");
                    boolean isHeader = true;
                    for(Element tr : trS) {
                        if(isHeader) {
                            isHeader = false;
                            continue;
                        }
                        MatchLocal match = new MatchLocal();
                        Elements td = tr.select("td");
                        String dateTime = td.get(0).text();
                        String teams = td.get(1).text();
                        String stadium = td.get(2).text();
                        System.out.println(dateTime + " " + teams + " " + stadium);
                        match.date = dateTime;
                        String[] teamSplit = teams.split("/");
                        match.teamHome = teamSplit[0].trim();
                        match.teamGuest = teamSplit[1].trim();
                        match.tour = tour.replaceAll("\\D+","");
                        match.stadium = stadium;
                        String url = getTournamentUrl(divisionName);
                        if(url == null) {
                            System.out.println("Ошибка пропускаем элемент, не нашелся tournamentUrl для " + divisionName);
                            continue;
                        }
                        match.url = url;
                        calendar.add(match);
                    }
                }
            }
        }
        deleteAndInsertExiderCalendarDB(calendar);
    }

    private String getTournamentUrl(String divisionName) {
        String url;
        switch(divisionName) {
            case "Высший дивизион": url = "/division1074"; break;
            case "Первый дивизион": url = "/tournament27995"; break;
            case "Второй дивизион A": url = "/tournament27996"; break;
            case "Второй дивизион B": url = "/tournament27997"; break;
            case "Кубок Востока": url = "/tournament28648"; break;
            default: url = null; break;
        }
        return url;
    }
    
    private Boolean isVaoID(String id) {
        String[] VAO = new String[] { "/division1074", "/tournament27995" , "/tournament27996", "/tournament27997", "/tournament28648"};
        return Arrays.asList(VAO).contains(id);
    }
    
    void parser(String id) throws IOException, SQLException {
        String urlResult;
        String urlCalendar;
        boolean isDivision;
        if (isVaoID(id)) {
            updateVaoFromExider(id);
        }
        if(id.contains("division")) {
            isDivision = true;
            urlResult = "https://lfl.ru/?ajax=1&method=tournament_resault_table&division_id=%s&limit=400";
            urlCalendar = "https://lfl.ru/?ajax=1&method=tournament_calendar_table&division_id=%s&limit=400";
        } else {
            isDivision = false;
            urlResult = "https://lfl.ru/?ajax=1&method=tournament_resault_table&tournament_id=%s&limit=400";
            urlCalendar = "https://lfl.ru/?ajax=1&method=tournament_calendar_table&tournament_id=%s&limit=400";
        }
        String resultAjax = String.format(urlResult, id.replaceAll("[^0-9]",""));
        String calendarAjax = String.format(urlCalendar, id.replaceAll("[^0-9]",""));
        System.out.println("----------" + id + "------------------------");
        System.out.println("----------RESULT------------------------");
        ArrayList<MatchLocal> results = parserResults(resultAjax, isDivision);
        System.out.println("----------CALENDAR------------------------");
        ArrayList<MatchLocal> calendar = parserResults(calendarAjax, isDivision);
        results.addAll(calendar);
        inserOrUpdateDB(results, id);
    }

    ArrayList<MatchLocal> parserResults(String url, boolean isDivision) throws IOException {
        Document doc = SSLHelper.getConnection(url).get();
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) {
            System.out.println(doc.text());
            return new ArrayList<>();
            //throw new NullPointerException("tbody not find");
        }
        Elements trs = tbody.select("tr");
        ArrayList<MatchLocal> matches = new ArrayList<>();
        for(Element tr : trs){
            if(tr.children().size() == 1) {
                //Строка тура
                System.out.println(tr.text());
            } else {
                MatchLocal match = new MatchLocal();
                if(isDivision) {
                    match.parserMatchInfoLflDivision(tr);
                } else {
                    match.parserMatchInfoLfl(tr);
                }
                matches.add(match);
            }
        }
        System.out.println(matches);
        return matches;
    }

    void deleteAndInsertExiderCalendarDB(ArrayList<MatchLocal> matches) throws SQLException {
        String sql = "delete from match_from_exider where id > 0";
        PreparedStatement ps = dbConnect.prepareStatement(sql);
        ps.execute();
        sql = "update tournament_info set league_name = 'ЛФЛ ВАО' where tournament_url in ('/division1074', '/tournament27995', '/tournament27996', '/tournament27997', '/tournament28648')";
        ps = dbConnect.prepareStatement(sql);
        ps.executeUpdate();
        sql = "insert into match_from_exider (tournament_url, date, stadium, tour, team_home, team_guest)"
                + "values (?, ?, ?, ?, ?, ?)";
        for(MatchLocal m : matches) {
            ps = dbConnect.prepareStatement(sql);
            ps.setString(1, m.url);
            ps.setString(2, m.date);
            ps.setString(3, m.stadium);
            ps.setString(4, m.tour);
            ps.setString(5, m.teamHome);
            ps.setString(6, m.teamGuest);
            ps.executeUpdate();
        }
        
    }
    
    void updateVaoFromExider(String tournamentUrl) throws SQLException {
        String sql = "update tournament_info set league_name = 'ЛФЛ Восток' where tournament_url = ?";
        PreparedStatement ps = dbConnect.prepareStatement(sql);
        ps.setString(1, tournamentUrl);
        ps.executeUpdate();
    }
    
    void inserOrUpdateDB(ArrayList<MatchLocal> matches, String toutnamentUrl) throws SQLException {
        String sql = "INSERT INTO match_v2 (tournament_url, match_url, tour, division_name, date, stadium_name, referee_name, team_home_url, team_home_image, team_home_name, "
                   + "team_home_goals, team_guest_goals, team_guest_name, team_guest_url, team_guest_image, photo_url, video_url)\n"
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) \n"
                   + "ON DUPLICATE KEY UPDATE tournament_url = VALUES(tournament_url), tour = VALUES(tour), division_name = VALUES(division_name), date = VALUES(date), "
                   + "stadium_name = VALUES(stadium_name), referee_name = VALUES(referee_name), team_home_url = VALUES(team_home_url), team_home_image = VALUES(team_home_image), team_home_name = VALUES(team_home_name), team_home_goals = VALUES(team_home_goals),"
                   + "team_guest_goals = VALUES(team_guest_goals), team_guest_name = VALUES(team_guest_name), team_guest_url = VALUES(team_guest_url), team_guest_image = VALUES(team_guest_image),"
                   + "photo_url = VALUES(photo_url), video_url = VALUES(video_url);";
        for(MatchLocal match : matches) {
            PreparedStatement ps = dbConnect.prepareStatement(sql);
            ps.setString(1, toutnamentUrl);
            ps.setString(2, match.url);
            ps.setString(3, match.tour);
            ps.setString(4, ""); //Дивизион проставлю через SQL
            ps.setString(5, match.date + " " + match.time);
            ps.setString(6, match.stadium);
            ps.setString(7, ""); //refere
            ps.setString(8, match.teamHomeUrl);
            ps.setString(9, match.teamHomeImage);
            ps.setString(10, match.teamHome);
            ps.setString(11, match.goalsHome);
            ps.setString(12, match.goalsGuest);
            ps.setString(13, match.teamGuest);
            ps.setString(14, match.teamGuestUrl);
            ps.setString(15, match.teamGuestImage);
            ps.setString(16, match.photoUrl);
            ps.setString(17, match.videoUrl);
            //System.out.println(ps);
            ps.executeUpdate();
        }
    }

}
