/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Action;
import com.mycompany.parsersiteucl.Player;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class ParserLflHelper {

    final static int TYPE_SQUADS = 1;
    final static int TYPE_GOALS = 2;
    final static int TYPE_ASSISTENTS = 3;
    final static int TYPE_WARNINGS = 4;
    final static int TYPE_UNKNOWS = -1;
    
    static int typeNextBlock(String tag, String textTag) {
        if (textTag.equals("Составы:") && tag.equals("p")) {
            return TYPE_SQUADS;
        }
        if (textTag.equals("Голы:" ) && tag.equals("p")) {
            return TYPE_GOALS;
        }
        if (textTag.equals("Ассистенты:" ) && tag.equals("p")) {
            return TYPE_ASSISTENTS;
        }
        if (textTag.equals("Предупреждения:" ) && tag.equals("p")) {
            return TYPE_WARNINGS;
        }
        return TYPE_UNKNOWS;
    }

    static ArrayList<Action> parserActionGoals(Element divGoals){
        ArrayList<Action> actions = new ArrayList<>();
        Elements goals = divGoals.getElementsByTag("a");
        goals.forEach((goal) -> {
            String url = goal.attr("href");
            if(url.contains("player")) {
                String playerUrl = url;
                String countAction = goal.text().replaceAll("\\D+","");
                int cntActions = Integer.parseInt(countAction);
                String actionName = Action.getTypeGoal(goal.text());
                Action action = new Action(playerUrl, actionName, cntActions);
                System.out.println(action);
                actions.add(action);
            }
        });
        return actions;
    }
    
    static ArrayList<Action> parserActionAssists(Element divAssists){
        ArrayList<Action> actions = new ArrayList<>();
        Elements assists = divAssists.getElementsByTag("a");
        assists.forEach((assist) -> {
            String url = assist.attr("href");
            if(url.contains("player")) {
                String playerUrl = url;
                String countAction = assist.text().replaceAll("\\D+","");
                int cntActions = Integer.parseInt(countAction);
                String actionName = "Передача";
                Action action = new Action(playerUrl, actionName, cntActions);
                System.out.println(action);
                actions.add(action);
            }
        });
        return actions;
    }
    
    static ArrayList<Action> parserActionWarnings(Element divCards){
        ArrayList<Action> actions = new ArrayList<>();
        Elements cards = divCards.getElementsByTag("a");
        cards.forEach((card) -> {
            String url = card.attr("href");
            if(url.contains("player")) {
                String playerUrl = url;
                int cntActions = 1;
                String actionName = Action.getTypeCard(card.selectFirst("img").attr("src"));
                Action action = new Action(playerUrl, actionName, cntActions);
                System.out.println(action);
                actions.add(action);
            }
        });
        return actions;
    }
    
    
    static ArrayList<Player> parserProtocol(Element divProtocol) throws IOException {
        Element teamHome = divProtocol.selectFirst("a.match_members_club");
        String urlTeam = teamHome.attr("href");
        String teamName = teamHome.text();
        ArrayList<Player> players = new ArrayList<>();
        for(Element element : divProtocol.children()){
            String tagTeam = element.normalName();
            if(tagTeam.equals("p")) {
                //Строка команды
                Element team =  element.selectFirst("a.match_members_club");
                String urlTeamLocal = team.attr("href");
                if(urlTeam.equals(urlTeamLocal)){
                    System.out.println("----------" + teamName + "------------");
                   continue; 
                } else {
                    urlTeam = urlTeamLocal;
                    teamName = team.text();
                    System.out.println("----------" + teamName + "------------");
                }
            } else {
                //Строка игрока
                Element playerTag = element.selectFirst("a");
                String playerUrl = playerTag.attr("href");
                String playerName = playerTag.text();
                Player player = new Player();
                player.teamName = teamName;
                player.teamUrl = urlTeam;
                player.name = playerName;
                player.urlName = playerUrl;
                System.out.println(player);
                players.add(player);   
            }
        }
        return players;
}
    
}
