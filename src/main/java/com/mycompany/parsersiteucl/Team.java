/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.util.ArrayList;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author m.glushko
 */
public class Team {
    
    public int id;
    public String teamName;
    String nameDivision;
    public String urlTournament;
    public String urlName;
    public String urlImage;
    int countPlayers;
    public ArrayList<Player> players = new ArrayList<>();
    
    public Team(){
        
    }
    
    public Team(String nameDivision, String teamName,String urlName){
        this.nameDivision = nameDivision;
        this.teamName = teamName;
        this.urlName = urlName;
    }

    public Team(String teamName, String urlName, int countPlayers) {
        this.teamName = teamName;
        this.urlName = urlName;
        this.countPlayers = countPlayers;
    }

    @Override
    public String toString() {
        return "Team{" + "teamName=" + teamName + ", nameDivision=" + nameDivision + ", urlName=" + urlName + ", urlImage=" + urlImage + ", countPlayers=" + countPlayers + ", players=" + players + '}';
    }


    public boolean isTeam(){
        return teamName != null && urlName != null && urlImage != null;
    }
    
    public void parserTeamLfl(Element tr){
        Element tdLogo = tr.selectFirst("td.logo-cell");
        Element tdName = tr.selectFirst("td.club-name-cell");
        if(tdLogo != null && tdName != null){
            teamName = tdName.text();
            urlName = tdLogo.selectFirst("a").attr("href");
            urlImage = tdLogo.selectFirst("img").attr("src");
        }
        
    }
    
    
    
    
}
