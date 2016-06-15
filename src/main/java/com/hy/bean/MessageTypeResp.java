package com.hy.bean;

/**
 * Created by cpazstido on 2016/5/24.
 */
public enum  MessageTypeResp {

    LOGIN_Resp((byte) 0),           //登录响应
    HEARTBEAT_Resp((byte) 1),       //心跳响应
    CMMD_RESP_RUNNING((byte) 2),    //命令收到，正在执行，结果将通过数据通道返回
    CMMD_RESP_XML_RESULT((byte) 3), //命令收到，且执行完毕，数据为执行结果(xml格式)
    CMMD_RESP_TXT_RESULT((byte) 4), //命令收到，且执行完毕，数据为执行结果(字符串格式)
    CMMD_RESP_ERROR((byte) 5);      //命令执行出错（数据为出错信息）

    private final byte value;

    private MessageTypeResp(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}
