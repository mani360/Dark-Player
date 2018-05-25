package com.andygopu.androidantariksa.blackplay.model;

public class Album {

        private int id;
        private String year;
        private String name;
        private String art;

        public Album(int id, String year, String name, String art) {
            this.id = id;
            this.year = year;
            this.name = name;
            this.art = art;
        }

        public int getId() {
            return id;
        }

        public String getYear() {
            return year;
        }

        public String getName() {
            return name;
        }

        public String getArt() {
            return art;
        }


}
