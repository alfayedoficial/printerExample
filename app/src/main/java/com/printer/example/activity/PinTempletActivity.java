package com.printer.example.activity;

import android.os.Bundle;
import android.view.View;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.TextSetting;

import java.io.UnsupportedEncodingException;

public class PinTempletActivity extends BaseActivity {

    private RTPrinter rtPrinter;
    private Cmd cmd;
    private String chartSetName = "GBK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_templet);
        initView();
        init();
        addListener();
    }

    @Override
    public void initView() {
        System.out.println("initView");
    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.instance.getRtPrinter();
        CmdFactory cmdFactory = new PinFactory();
        cmd = cmdFactory.create();
    }

    @Override
    public void addListener() {

    }

    public void onBtnClick(View view){
        switch (view.getId()) {
            case R.id.btn_template1:
                printTemplate1();
                break;
            default:
                break;
        }
    }

    /**
     * 模版1-打印
     */
    private void printTemplate1() {
        cmd.clear();
        cmd.append(cmd.getHeaderCmd());

        TextSetting textSetting = new TextSetting();
        textSetting.setDoubleWidth(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Enable);
        textSetting.setDoubleSizeChineseCharctor(SettingEnum.Enable);

        String title1 = "小陈服装批发销售单";
        String title2 = "商品信息";
        try {
            textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            cmd.append(cmd.getTextCmd(textSetting, title1, chartSetName));
            cmd.append(cmd.getLFCRCmd());
            textSetting.setAlign(CommonEnum.ALIGN_LEFT);
            cmd.append(cmd.getTextCmd(textSetting, title2, chartSetName));
            cmd.append(cmd.getLFCRCmd());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }
}
