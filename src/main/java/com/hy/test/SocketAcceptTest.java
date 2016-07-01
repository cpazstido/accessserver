package com.hy.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by cpazstido on 2016/6/23.
 */
public class SocketAcceptTest {
    private static ServerSocket  serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(90);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){

            Socket socket=null;
            try {
                socket = serverSocket.accept();
                System.out.println(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(socket);
        }
    }
}
