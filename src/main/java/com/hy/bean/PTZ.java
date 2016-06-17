package com.hy.bean;

/**
 * Created by cpazstido on 2016/6/16.
 */
public class PTZ {
    public static final int MOVE_UP = 0x0402;
    public static final int MOVE_L_UP = 0x0702;
    public static final int MOVE_R_UP = 0x0802;
    public static final int MOVE_DOWN = 0x0404;
    public static final int MOVE_L_DOWN = 0x0704;
    public static final int MOVE_R_DOWN = 0x0804;
    public static final int MOVE_L = 0x0504;
    public static final int MOVE_R = 0x0502;
    public static final int MOVE_STOP = 0x0901;
    public static final int VISU_CAM_SWITH = 0x0902; //可见光摄像机开关
    public static final int IR_CAM_SWITH = 0x0903; //红外摄像机开关
    public static final int A9_POWER_OFF = 0x0904; //A9断电
    public static final int VISU_CAM_FOCUS = 0x0905; //可见光焦距
    public static final int VISU_CAM_FOCUS_STOP = 0x0906; //可见光调焦停止
    public static final int ZOOM_IN = 0x0302;
    public static final int ZOOM_OUT = 0x0304;
    public static final int FOCUS_IN = 0x0202;
    public static final int FOCUS_OUT = 0x0204;

}
