package com.hy.test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 *
 * Created by cpazstido on 2016/6/21.
 */
public class SocketTest {
    private static int socketNum = 80000;
    private static Socket socketList[];
    public static void main(String[] args) {
        socketList = new Socket[socketNum];
        for(int i=0;i<socketNum;i++)
        {
            try {
                socketList[i] = new Socket("172.16.16.115",9000);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socketList[i].getOutputStream());
                bufferedOutputStream.write((""+i).getBytes());
                bufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(i);
        }
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
