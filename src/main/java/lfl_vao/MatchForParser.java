/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Action;
import com.mycompany.parsersiteucl.Player;
import java.util.ArrayList;

/**
 *
 * @author march
 */
public class MatchForParser {
    String url;
    long id;
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Action> actions = new ArrayList<>();

    public MatchForParser(String url, long id) {
        this.url = url;
        this.id = id;
    }
    
    
}
