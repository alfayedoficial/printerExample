package com.printer.example.app;

import android.app.Application;

import com.printer.example.utils.BaseEnum;
import com.rt.printerlibrary.printer.RTPrinter;

/**
 * Created by Tony on 2017/12/1.
 */

public class BaseApplication extends Application {

    public static BaseApplication instance = null;
    private RTPrinter rtPrinter;

    @BaseEnum.CmdType
    private int currentCmdType = BaseEnum.CMD_ESC;//默认为针打

    @BaseEnum.ConnectType
    private int currentConnectType = BaseEnum.NONE;//默认未连接

    public static final String SP_NAME_SETTING = "setting";

    public static String labelSizeStr = "80*40", labelWidth="80", labelHeight="40", labelSpeed="2", labelType="CPCL", labelOffset="0";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static BaseApplication getInstance() {
        return instance;
    }


    public RTPrinter getRtPrinter() {
        return rtPrinter;
    }

    public void setRtPrinter(RTPrinter rtPrinter) {
        this.rtPrinter = rtPrinter;
    }

    @BaseEnum.CmdType
    public int getCurrentCmdType() {
        return currentCmdType;
    }

    public void setCurrentCmdType(@BaseEnum.CmdType int currentCmdType) {
        this.currentCmdType = currentCmdType;
    }

    @BaseEnum.ConnectType
    public int getCurrentConnectType() {
        return currentConnectType;
    }

    public void setCurrentConnectType(@BaseEnum.ConnectType int currentConnectType) {
        this.currentConnectType = currentConnectType;
    }
}
