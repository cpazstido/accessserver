package com.hy.device;

/**
 * Created by cpazstido on 2016/6/1.
 */
public class DeviceMain {
    public static void main(String[] args) throws InterruptedException {
        DeviceThread device = new DeviceThread("172.16.16.112", 9001, "HY_OLMS_000000138");
        device.start();
    }
}
