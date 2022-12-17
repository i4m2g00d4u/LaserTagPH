package com.playhills.lasertag.manager.sql;

import java.sql.*;

public class MariaDB {

    private String address;
    private int port;
    private String user;
    private String password;

    private String url;


    private Statement st;
    private Connection conn = null;


    public MariaDB(String address, int port, String user, String password) {
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
        url = "jdbc:mariadb://" + address + ":" + port + "/dev_user";
    }

    public void update(String query) {
        connect();
        try {
            st.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }

    public void disconnect() {
        try {
            st.close();
            conn.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet query(String query) {
        connect();
        try {
            return st.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
        return null;
    }

    public void setDatabase(String tabelle, String spalteKategorie, String SpalteInhalt, String WertKategories, Object WerInhalt) {
        update("INSERT INTO `" + tabelle + "` (`" + spalteKategorie + "`,`" + WertKategories + "`) VALUES ('" + SpalteInhalt + " , " + WerInhalt + "')");
    }

    public Object getDatabase(String Tabelle, String SpalteKategorie, String SpalteInhalt, String WertKategorie) {
        connect();
        Object out = null;
        try{
            ResultSet rs = query("SELECT `" + WertKategorie + "` FROM " + Tabelle + " WHERE " + SpalteKategorie + "='" + SpalteInhalt + "'");
            while (rs.next()){
                out = rs.getObject(1);
            }
        } catch (SQLException exception){
            exception.printStackTrace();
        }

        disconnect();
        return out;
    }


    public void connect() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            st = conn.createStatement();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        connect();
        try {
            st.execute("CREATE TABLE IF NOT EXISTS Stats (UUID text, Games INT, Wins INT, Loses INT, Kills INT, FinalKills INT, Deaths INT, FinalDeaths INT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }
}
