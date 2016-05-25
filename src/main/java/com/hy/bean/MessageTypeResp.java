package com.hy.bean;

/**
 * Created by cpazstido on 2016/5/24.
 */
public enum  MessageTypeResp {

    LOGIN_Resp((byte) 0), HEARTBEAT_Resp((byte) 1), CMMD_RESP_RUNNING((byte) 2),
    CMMD_RESP_XML_RESULT((byte) 3),CMMD_RESP_TXT_RESULT((byte) 4),CMMD_RESP_ERROR((byte) 5);

    private byte value;

    private MessageTypeResp(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}
