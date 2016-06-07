package com.hy.device;

/**
 * Created by cpazstido on 2016/6/3.
 */
public class SendFileMainTest {
    public static void main(String[] args) throws InterruptedException {
        SendFileThread sendFileThread = new SendFileThread("172.16.16.112",8080);
        sendFileThread.start();
    }
}
