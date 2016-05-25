package com.hy.server;

/**
 * Created by cpazstido on 2016/5/23.
 */
public class ServerAgent {
    private String serverName;
    private int port;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void bind(){}
}
