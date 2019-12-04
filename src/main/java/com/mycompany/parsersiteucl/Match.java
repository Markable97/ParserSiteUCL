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
public class Match {
    int idmatch;
    String urlMatch;
    String dateMatch;
    String division;
    int tour;
    String teamHome;
    int goalHome;
    int goalGuest;
    String teamGuest;
    ArrayList<Player> players = new ArrayList<>();
    String stadium;
    String refere;
    String matchTransfer;

    public Match(int idmatch, String urlMatch, String dateMatch) {
        this.idmatch = idmatch;
        this.urlMatch = urlMatch;
        this.dateMatch = dateMatch;
    }

    @Override
    public String toString() {
        return "Match{" + "idmatch=" + idmatch + ", urlMatch=" + urlMatch + ", dateMatch=" + dateMatch + ", division=" + division + ", tour=" + tour + 
                ", teamHome=" + teamHome + ", goalHome=" + goalHome + ", goalGuest=" + goalGuest + ", teamGuest=" + teamGuest + ", players=" + players + 
                ", stadium=" + stadium + ", refere=" + refere + '}' + "\n";
    }
    
    
}
