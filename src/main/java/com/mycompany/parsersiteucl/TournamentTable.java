/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

/**
 *
 * @author m.glushko
 */
public class TournamentTable {
    
    String nameDivision;
    //int position;
    Team team;
    int games;
    int wins;
    int draws;
    int losses;
    int goalsScored;
    int goalsConceded;
    int points;

    public TournamentTable(String nameDivision, Team team, int games, int wins, int draws, int losses, String balls, int points) {
        this.nameDivision = nameDivision;
        this.team = team;
        this.games = games;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        setGoals(balls);
        this.points = points;
    }

    public void setGoals(String balls) {
        String[] ball = balls.split("-");
        this.goalsScored = Integer.parseInt(ball[0]);
        this.goalsConceded = Integer.parseInt(ball[1]);
    }

    @Override
    public String toString() {
        return "TournamentTable{" + "nameDivision=" + nameDivision + ", team=" + team + ", games=" + games + ", wins=" + wins + ", draws=" + draws + 
                ", losses=" + losses + ", goalsScored=" + goalsScored + ", goalsConceded=" + goalsConceded + ", points=" + points + '}' + "\n";
    }
    
    
    
}
