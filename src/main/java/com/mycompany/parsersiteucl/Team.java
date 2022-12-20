/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.util.ArrayList;

/**
 *
 * @author m.glushko
 */
public class Team {
    
    String teamName;
    String nameDivision;
    String urlName;
    int countPlayers;
    ArrayList<Player> players = new ArrayList<>();
    
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
        return "Team{" + "teamName=" + teamName + ", nameDivision=" + nameDivision + ", urlName=" + urlName + ", countPlayers=" + countPlayers + ", players=" + players + '}';
    }
    
    
    
}
