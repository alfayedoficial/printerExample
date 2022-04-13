package com.printer.example.activity;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.printer.example.R;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.FuncUtils;
import com.printer.example.utils.LogUtils;
import com.rt.printerlibrary.cmd.EscCmd;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdTestActivity extends Activity implements View.OnClickListener, PrinterObserver {

    private TextView tv_rev;
    private Button btn_clear_rev, btn_clear_send, btn_send, btn_send2, btn_read;
    private RadioGroup rg_sendtype;
    private EditText et_send;

    private RTPrinter rtPrinter = BaseApplication.getInstance().getRtPrinter();
    private byte btSendType = 0;//0=txt, 1=hex


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd_send_read);
        initView();
        addListener();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterObserverManager.getInstance().remove(this);
    }

    private void initView() {
        tv_rev = findViewById(R.id.tv_rev);
        btn_clear_rev = findViewById(R.id.btn_clear_rev);
        btn_clear_send = findViewById(R.id.btn_clear_send);
        btn_send = findViewById(R.id.btn_send);
        rg_sendtype = findViewById(R.id.rg_sendtype);
        et_send = findViewById(R.id.et_send);
        btn_send2 = findViewById(R.id.btn_send2);
        btn_read = findViewById(R.id.btn_read);
    }

    private void addListener() {
        btn_clear_rev.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        btn_send2.setOnClickListener(this);
        btn_clear_send.setOnClickListener(this);
        btn_read.setOnClickListener(this);

        rg_sendtype.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_text:
                        btSendType = 0;
                        et_send.setText("text");
                        break;
                    case R.id.rb_hex:
                        btSendType = 1;
                        et_send.setText("1254");
                        break;
                    default:
                        break;
                }
                et_send.setSelection(et_send.getText().toString().length());
            }
        });

        rg_sendtype.check(R.id.rb_hex);
    }

    private void init() {
        PrinterObserverManager.getInstance().add(this);//添加连接状态监听
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear_rev:
                tv_rev.setText("");
                break;
            case R.id.btn_clear_send:
                et_send.setText("");
                break;
            case R.id.btn_send:
                if (rtPrinter != null) {
                    byte[] btContent = null;
                    String str = et_send.getText().toString();
                    if (btSendType == 0) {//text
                        str = getStringTest(str);
                        try {
                            btContent = str.getBytes("GBK");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else if (btSendType == 1) {//hex
                        str = replaceBlank(str);
                        et_send.setText(str);
                        btContent = FuncUtils.HexToByteArr(str);
                    }
                    if (btContent != null) {
                        rtPrinter.writeMsgAsync(btContent);
                    }
                }
                break;
            case R.id.btn_send2:
                if (rtPrinter != null) {
                    rtPrinter.writeMsgAsync(getHeadCmd());
                }
                break;
            case R.id.btn_read:
                if (rtPrinter != null) {
                    byte[] btRes = rtPrinter.readMsg();
                    tv_rev.append(formatDate(System.currentTimeMillis()) + "_[Rev]: " + FuncUtils.ByteArrToHex(btRes) + "\n");
                }
                break;
            default:
                break;
        }
    }

    /**
     * cpcl韵达指令测试
     *
     * @param str
     * @return
     */
    @NonNull
    private String getStringTest(String str) {
        if (TextUtils.isEmpty(str)) {
            str = "! 0 200 200 1256 1\n" +
                    "PW 600\n" +
                    "LINE 4 207 576 207 1\n" +
                    "LINE 4 448 440 448 1\n" +
                    "LINE 4 512 440 512 1\n" +
                    "LINE 4 775 576 775 1\n" +
                    "LINE 4 1162 576 1162 1\n" +
                    "LINE 440 207 440 670 1\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 8 始发网点:  网点程序测试1\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 3 24 33 送达\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 3 24 68 地址\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 100 40 收件人:  吕祥\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 100 70 电话:  13818494834\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 100 105 收件地址:  上海市  (沪)市辖区\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 100 130   青浦区 青山路6669号\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 448 28 2018年01月24日\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 448 48 14:03:43\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 448 8 体积:\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 328 8 1.00KG\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 328 33 普通\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 16 182 集包地：上海分拨包\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 24 470 运单编号:  3101282113669\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 24 530 商品信息:  1\n" +
                    "B QR 32 246 M 2 U 6\n" +
                    "MA,3101282113669\n" +
                    "ENDQR\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 6 200 236 760\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 6 200 306 W028\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 6 200 376 07-\n" +
                    "VB 128 2 2 80 460 630 310128211366985642\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 600 收件人/代签人:\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 634 签收时间:      年    月    日\n" +
                    "B 128 1 2 50 23 690 3101282113669\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 23 750 3101282113669\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 782 发件人:  王华阳\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 802 电话:  17601349398\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 822 发件地址:  上海市  (沪)市辖区  青浦区 青\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 842 山路6669号\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 862 收件人:  吕祥\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 882 电话:  13818494834\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 902 收件地址:  上海市  (沪)市辖区  青浦区 青\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 922 山路6669号\n" +
                    "LINE 448 882 560 882 5\n" +
                    "LINE 448 922 560 922 5\n" +
                    "LINE 448 882 448 922 5\n" +
                    "LINE 560 882 560 922 5\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 470 892 已验视\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 964 发件人:王华阳\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 984 电话:  17601349398\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 1004 发件地址:  上海市  (沪)市辖区  青浦区 青\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 1024 山路6669号\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 1044 收件人:  吕祥\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 1064 电话:  13818494834\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 1084 收件地址:  上海市  (沪)市辖区  青浦区 青\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 32 1104 山路6669号\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 448 1055 1.00KG\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 448 1080 普通\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 448 1105 2018年01月24日\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 448 1120 14:03:43\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 24 0 32 1135 运单编号:  3101282113669\n" +
                    "UT 0\n" +
                    "SETBOLD 0\n" +
                    "IT 0\n" +
                    "TEXT 55 0 24 1166 官方网址: http://www.yundaex.com     客服热线:  95546   发货人联\n" +
                    "PR 0\n" +
                    "FORM\n" +
                    "PRINT\n";
        }
        return str;
    }

    public byte[] getHeadCmd() {
        //Head send
        byte[] btStart = {
                0x03, (byte) 0xff, 0x2f, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, (byte) 0xd3, 0x00, 0x00};
        LogUtils.e("Update", "Head|" + FuncUtils.ByteArrToHex(btStart));
        rtPrinter.writeMsg(btStart);
        EscCmd cmd = new EscCmd();
        rtPrinter.writeMsg(cmd.getLFCRCmd());
        return btStart;
    }

    @Override
    public void printerObserverCallback(PrinterInterface printerInterface, int i) {

    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, final byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_rev.append(formatDate(System.currentTimeMillis()) + "_[Rev]: " + FuncUtils.ByteArrToHex(bytes) + "\n");
            }
        });
    }

    public static String formatDate(long timeMillis) {
        String format = "HH:mm:ss.SSS";
        Date date = null;
        if (timeMillis > 0) {
            date = new Date(timeMillis);
        } else {
            date = new Date();
        }

        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
