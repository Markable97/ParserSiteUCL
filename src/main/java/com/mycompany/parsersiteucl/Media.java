/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class Media {
    String urlAlbum;
    String teamHome;
    String teamGuest;
    String tour;
    ArrayList<Image> images = new ArrayList<>();

    class Image {
        String name;
        String preview;
        String origin;

        public Image(String name, String preview, String origin) {
            this.name = name;
            this.preview = preview;
            this.origin = origin;
        }

        @Override
        public String toString() {
            return "Image{" + "name=" + name + ", preview=" + preview + ", origin=" + origin + '}';
        }
        
    }
    
    public Media() {
    }
    
    void setTeamsAndTour(String home, String guest, String tour){
        this.tour = tour;
        this.teamHome = home;
        this.teamGuest = guest;
    }
    
    void parserTeamNaming(String textTour, String parserTour){
        tour = parserTour.replace("(", "").replace(")", "").trim();
        String[] teams = textTour.replace(parserTour, "").trim().split(" - ");
        teamHome = teams[0].trim();
        teamGuest = teams[1].trim();
    }
    
    void parserPreviewImage(String albumUrl){
        urlAlbum = albumUrl;
        System.out.println(urlAlbum);
        try {
            Document doc = Jsoup.connect(urlAlbum).get();
            Elements imgs = doc.select("img.js-media-item");
            for(Element image : imgs){
                String img = image.attr("src");
                String format = ".jpg";
                String preview = img;
                String origin = image.attr("data-image");
                String name = img.split(format)[0].split("/impg/")[1] + format;
                images.add(new Image(name, preview, origin));
            }
        } catch (IOException ex) {
            Logger.getLogger(Media.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Bad parser media");
        }
    }

    @Override
    public String toString() {
        return "Media{" + "urlAlbum=" + urlAlbum + ", teamHome=" + teamHome + ", teamGuest=" + teamGuest + ", tour=" + tour + ", images=" + images.toString() + '}';
    }
    
    
}
