package com.hy.bean;

/**
 * Created by cpazstido on 2016/5/24.
 */
public enum MessageTypeReq {

    LOGIN_REQ((byte) 0),        //登录挑战请求（发送挑战随机码）
    LOGIN_INFO((byte) 1),       //登录回复信息（成功or失败）
    HEARTBEAT_REQ((byte) 2),    //心跳数据（主动向设备发送心跳数据）
    XML_CMD((byte) 3),          //想设备发送xml命令
    INFO((byte)4);              //向设备发送文本信息

    private byte value;

    private MessageTypeReq(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}
