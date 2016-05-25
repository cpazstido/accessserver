package com.hy.bean;

/**
 * Created by cpazstido on 2016/5/24.
 */
public enum MessageTypeReq {

    LOGIN_REQ((byte) 0), LOGIN_INFO((byte) 1),
    HEARTBEAT_REQ((byte) 2), XML_CMD((byte) 3),
    INFO((byte)4);

    private byte value;

    private MessageTypeReq(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}
