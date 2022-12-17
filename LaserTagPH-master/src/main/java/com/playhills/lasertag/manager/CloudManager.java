package com.playhills.lasertag.manager;

public class CloudManager {

    /**
     * forces the CloudSystem to start a server with the same group.
     */
    public static void requestNewServer() {
        System.out.println("cloudapi:status:serverIngame");
    }


    /**
     * terminates the server immediately, by killing the process
     */
    public static void killServerImmediately() {
        System.out.println("stopped responding");
    }

}
