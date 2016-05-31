package com.hy.bean;

import com.hy.handler.FireServerHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by cpazstido on 2016/5/30.
 */
public class DeviceInfo {
    private String deviceId;
    private int ipcNum;
    private float softwareVersion;
    private byte data[];
    private FireServerHandler fsh;

    public FireServerHandler getFsh() {
        return fsh;
    }

    public void setFsh(FireServerHandler fsh) {
        this.fsh = fsh;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getIpcNum() {
        return ipcNum;
    }

    public void setIpcNum(int ipcNum) {
        this.ipcNum = ipcNum;
    }

    public float getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(float softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
