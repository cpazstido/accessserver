package com.hy.bean;

/**
 * Created by cpazstido on 2016/5/13.
 */
public class Header {
    private byte flag[];
    private byte version;
    private int index;
    private byte types;
    private int len;

    public byte[] getFlag() {
        return flag;
    }

    public void setFlag(byte []flag) {
        this.flag = flag;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getTypes() {
        return types;
    }

    public void setTypes(byte types) {
        this.types = types;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
