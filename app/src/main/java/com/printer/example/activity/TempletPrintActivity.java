package com.printer.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.BaseEnum;
import com.printer.example.utils.FuncUtils;
import com.printer.example.utils.LogUtils;
import com.printer.example.utils.TempletDemo;
import com.printer.example.utils.ToastUtil;
import com.rt.printerlibrary.bean.LableSizeBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.bean.PrinterStatusBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.cmd.TscCmd;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.cmd.ZplFactory;
import com.rt.printerlibrary.enumerate.BarcodeStringPosition;
import com.rt.printerlibrary.enumerate.BarcodeType;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.CpclFontTypeEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.EscBarcodePrintOritention;
import com.rt.printerlibrary.enumerate.PrintDirection;
import com.rt.printerlibrary.enumerate.PrintRotation;
import com.rt.printerlibrary.enumerate.QrcodeEccLevel;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.enumerate.TscFontTypeEnum;
import com.rt.printerlibrary.enumerate.ZplFontTypeEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BarcodeSetting;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.rt.printerlibrary.utils.PrintListener;
import com.rt.printerlibrary.utils.PrintStatusCmd;
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class TempletPrintActivity extends BaseActivity {

    private Button btn_template_ESC_80,btn_template2,btn_template3,btn_template4;

    private RTPrinter rtPrinter;
    private Bitmap bmp;
    private String title, content_tel, content_email, barcode_str;
    private  int printTimes=0;
    private  int maxPrintCopys=1;
    private TempletDemo templetDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templet_print);
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {
        title = getString(R.string.temp1_title1_printer_tech);
        content_tel = getString(R.string.temp1_content1_tel);
        content_email = getString(R.string.temp1_content2_email);
        barcode_str = "123456789";

        btn_template_ESC_80 = findViewById(R.id.btn_template_ESC_80);
        btn_template2 = findViewById(R.id.btn_template2);
        btn_template3 = findViewById(R.id.btn_template3);
        btn_template4 = findViewById(R.id.btn_template4);
        
        btn_template_ESC_80.setVisibility(View.GONE);
        btn_template2.setVisibility(View.GONE);
        btn_template3.setVisibility(View.GONE);
        btn_template4.setVisibility(View.GONE);

        switch (BaseApplication.getInstance().getCurrentCmdType()){
            case BaseEnum.CMD_ESC:
                btn_template_ESC_80.setVisibility(View.VISIBLE);
                btn_template2.setVisibility(View.VISIBLE);
                break;
            case BaseEnum.CMD_TSC:
                btn_template2.setVisibility(View.VISIBLE);
                btn_template3.setVisibility(View.VISIBLE);
                btn_template4.setVisibility(View.VISIBLE);
                break;
            case BaseEnum.CMD_CPCL:
                btn_template2.setVisibility(View.VISIBLE);
                btn_template3.setVisibility(View.VISIBLE);
                break;
            case BaseEnum.CMD_ZPL:
                break;
        }
    }

    @Override
    public void addListener() {

    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
        templetDemo = TempletDemo.getInstance(rtPrinter,this);

    }

    public void onBtnClick(View v) {
        switch (v.getId()) {
            case R.id.btn_template1:
                try {
                    printTemplet();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_templateIndia:
//                printTempletIndiaTest();
                templetDemo.escTicketTemplet();
                break;
            case R.id.btn_template_ESC_80:
                templetDemo.esc80TempPrint();
                break;

            case R.id.btn_template2:
                try {
                    printTemplet2();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_template3:
                try {
                    printTemplet3();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                break;
             case R.id.btn_template4:
                 //连续打印，询问打印机状态后才打印，用于定制的RPP320
                 // Print continuously, ask the printer status before printing. use for for custom RPP320
                 TsctemplatePrint4();
                break;

            default:
                break;
        }
    }


    private void esc80TempPrint2() {
        CmdFactory escFac = new EscFactory();
        Cmd escCmd = escFac.create();
        escCmd.append(escCmd.getHeaderCmd());//初始化
        escCmd.setChartsetName("UTF-8");

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setAlign(CommonEnum.ALIGN_LEFT);
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

        TextSetting textSetting = new TextSetting();
        textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);
          try {
            String preBlank = "        ";
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Table No:6(0)     Date:09/04/18"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Kot No:5          Time:18:10"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "--------------------------------"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_B_9x24);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + preBlank + "Item" + preBlank + preBlank + "  Qty"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "--------------------------------"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setBold(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Cheese Pasta        1 No"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Red Sauce Pasta     1 No"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "JAIN BURGER         1 No"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setBold(SettingEnum.Disable);
            textSetting.setEscFontType(ESCFontTypeEnum.FONT_B_9x24);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "    no cheese"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);
            textSetting.setBold(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Masala Cheesy Gri   1 No"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setBold(SettingEnum.Disable);
            textSetting.setEscFontType(ESCFontTypeEnum.FONT_B_9x24);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "    Krispe"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "    Spicy"));
            escCmd.append(escCmd.getLFCRCmd());

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "--------------------------------"));
            escCmd.append(escCmd.getLFCRCmd());
            textSetting.setBold(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "         Total       4"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }




    private void printTempletIndiaTest() {
        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrintIndiaTest();
                break;
            case BaseEnum.CMD_TSC:
                break;
            case BaseEnum.CMD_CPCL:
                break;
            case BaseEnum.CMD_ZPL:
                break;
            case BaseEnum.CMD_PIN:
                break;
            default:
                break;
        }
    }

    private void escPrintIndiaTest() {
        if (rtPrinter == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    escTempletIndiaTest();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void printTemplet() throws UnsupportedEncodingException, SdkException {
        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            case BaseEnum.CMD_TSC:
                templetDemo.tscPrint();
                break;
            case BaseEnum.CMD_CPCL:
                templetDemo.cpclPrint();
                break;
            case BaseEnum.CMD_ZPL:
                zplPrint();
                break;
            case BaseEnum.CMD_PIN:
                pinPrint();
                break;
            default:
                break;
        }
    }

    private void pinPrint() throws UnsupportedEncodingException, SdkException {
        new Thread(new Runnable() {
            @Override
            public void run() {

                showProgressDialog(null);

                if (rtPrinter == null) {
                    return;
                }
                CmdFactory fac = new PinFactory();
                Cmd cmd = fac.create();

                TextSetting textSetting = new TextSetting();
                textSetting.setBold(SettingEnum.Enable);//加粗
                textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
                textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
                textSetting.setDoublePrinting(SettingEnum.Disable);//重叠打印
//        textSetting.setPinPrintMode(CommonEnum.PIN_PRINT_MODE_Bidirectional);
                textSetting.setUnderline(SettingEnum.Disable);//下划线
                textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);

                cmd.append(cmd.getHeaderCmd());//初始化
                try {
                    cmd.append(cmd.getTextCmd(textSetting, title, "GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());//换行

                textSetting.setBold(SettingEnum.Disable);//取消加粗
                textSetting.setDoubleHeight(SettingEnum.Disable);//取消倍高
                textSetting.setDoubleWidth(SettingEnum.Disable);//取消倍宽
                try {
                    cmd.append(cmd.getTextCmd(textSetting, content_tel, "GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());//换行
                try {
                    cmd.append(cmd.getTextCmd(textSetting, content_email, "GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());//换行

                cmd.append(cmd.getEndCmd());//退纸

                rtPrinter.writeMsg(cmd.getAppendCmds());
            }
        }).start();

    }

    public void zplPrint() throws UnsupportedEncodingException, SdkException {
        if (rtPrinter == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog(null);
                try {
                    templetDemo.zplPrint();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                hideProgressDialog();
            }
        }).start();

    }




    private void escPrint() throws UnsupportedEncodingException, SdkException {
        if (rtPrinter == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    templetDemo.escTemplet();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }




    private void escTempletIndiaTest() throws UnsupportedEncodingException, SdkException {
        CmdFactory escFac = new EscFactory();
        Cmd escCmd = escFac.create();
        escCmd.append(escCmd.getHeaderCmd());//初始化
        escCmd.setChartsetName("UTF-8");

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(48 * 8);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm
        if (bmp == null) {
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bill_bmptest);
        }
        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
        escCmd.append(escCmd.getLFCRCmd());


        TextSetting textSetting = new TextSetting();
        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//对齐方式-左对齐，居中，右对齐
        textSetting.setBold(SettingEnum.Enable);//加粗
        textSetting.setUnderline(SettingEnum.Disable);//下划线
        textSetting.setIsAntiWhite(SettingEnum.Disable);//反白
        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
        textSetting.setItalic(SettingEnum.Disable);//斜体
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);//小字体


        escCmd.append(escCmd.getTextCmd(textSetting, "India Test 1"));
        escCmd.append(escCmd.getLFCRCmd());//回车换行

        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setBold(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Disable);
        textSetting.setDoubleWidth(SettingEnum.Disable);
        escCmd.append(escCmd.getTextCmd(textSetting, "India Test 2"));

        escCmd.append(escCmd.getLFCRCmd());
        textSetting.setUnderline(SettingEnum.Enable);
        escCmd.append(escCmd.getTextCmd(textSetting, "India Test 3"));

        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());

//        BarcodeSetting barcodeSetting = new BarcodeSetting();
//        barcodeSetting.setEscBarcodePrintOritention(EscBarcodePrintOritention.Rotate0);
//        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
//        barcodeSetting.setHeightInDot(72);//accept value:1~255
//        barcodeSetting.setBarcodeWidth(3);//accept value:2~6
//        escCmd.append(escCmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, barcode_str));
//        escCmd.append(escCmd.getLFCRCmd());
//
//        barcodeSetting.setQrcodeDotSize(5);//accept value: Esc(1~15), Tsc(1~10)
//        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
//        escCmd.append(escCmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, content_email));

        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());

        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
    }




    private void TsctemplatePrint(){

        final EditText inputServer = new EditText(this);
        inputServer.setText("5");
        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Print_copys)).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton(getString(R.string.dialog_cancel), null);
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                int  icopys = Integer.valueOf(inputServer.getText().toString());
                try {
                    DoTsctemplatePrint(icopys);
                } catch (SdkException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();


    }




    //Honeywell
    private void DoTsctemplatePrint(int icopys) throws SdkException {
        if (rtPrinter == null) {
            return;
        }
        int ileft=30;
        CmdFactory tscFac = new TscFactory();
        Cmd cmd = tscFac.create();

        cmd.append(cmd.getHeaderCmd());

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLabelGap(3);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        commonSetting.setLableSizeBean(new LableSizeBean(80, 120));//label width = 80mm, label height = 80mm
        cmd.append(cmd.getCommonSettingCmd(commonSetting));
        cmd.append(cmd.getReverse(10,70,45,560 ));//反白
        TextSetting textSetting = new TextSetting();
        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(105, 23));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*10 MIL C39*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPosition(new Position(115, 53));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(67);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(2);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(5);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE39, barcodeSetting, "10MIL C39"));
        cmd.append(cmd.getLFCRCmd());

        cmd.append(cmd.getDrawBox(81,131,548,500,8));//画框


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(321, 152));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "15 MIL C39"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(321, 187));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(83);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE39, barcodeSetting, "AA "));
        cmd.append(cmd.getLFCRCmd());

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(110, 300));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*15 MIL C128*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPosition(new Position(130, 334));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(84);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(9);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, "123456789"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese);
        textSetting.setTxtPrintPosition(new Position(122, 428));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(2);
        textSetting.setyMultiplication(2);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "HoneyWell Printer"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(105, 521));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "10 MIL QR"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(313, 518));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "15 MIL QR"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        barcodeSetting.setPosition(new Position(124, 545));
        barcodeSetting.setQrcodeDotSize(2);//accept value: Esc(1~15), Tsc(1~10) 宽度
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "10 MI QR*  12345678901234567890"));

        barcodeSetting.setPosition(new Position(333, 545));
        barcodeSetting.setQrcodeDotSize(3);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "*15 MI QR* 12345678901234567890"));

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(84, 633));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(67);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.UPC_A, barcodeSetting, "13827678947"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(291, 735));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*15 MIL Ean13*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(221, 758));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(70);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.EAN13, barcodeSetting, "1234567890128"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_12x20_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(45, 865));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_12x20_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(22, 893));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "abcdefghijklmnopqrstuvwxyz!@#$%^&*() +"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i=0; i<6; i++) {
            cmd.append(cmd.getLFCRCmd());
        }
        cmd.append(cmd.getPrintCopies(icopys));//TSC must add this function， TSC必须要加上这个方法，打印份数
        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());


    }

    private void TsctemplatePrint3(){

        final EditText inputServer = new EditText(this);
        inputServer.setText("5");
        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Print_copys)).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton(getString(R.string.dialog_cancel), null);
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
               int  icopys = Integer.valueOf(inputServer.getText().toString());
             try {
                    DoTsctemplatePrint3(icopys);
                } catch (SdkException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();


    }
    private void DoTsctemplatePrint3(int icopys) throws SdkException {
        if (rtPrinter == null) {
            return;
        }
        int ileft=30;
        CmdFactory tscFac = new TscFactory();
        Cmd cmd = tscFac.create();

        cmd.append(cmd.getHeaderCmd());

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLabelGap(3);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        commonSetting.setLableSizeBean(new LableSizeBean(80, 120));//label width = 80mm, label height = 80mm
        cmd.append(cmd.getCommonSettingCmd(commonSetting));

        cmd.append(cmd.getReverse(10,70,45,790 ));//反白

        TextSetting textSetting = new TextSetting();
        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(105, 23));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*10 MIL C39*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPosition(new Position(115, 53));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(67);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(2);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(5);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE39, barcodeSetting, "10MIL C39"));
        cmd.append(cmd.getLFCRCmd());

        cmd.append(cmd.getDrawBox(81,131,548,500,8));//画框


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(321, 152));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "15 MIL C39"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(321, 187));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(83);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE39, barcodeSetting, "AA "));
        cmd.append(cmd.getLFCRCmd());

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(110, 300));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*15 MIL C128*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPosition(new Position(130, 334));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(84);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(9);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, "123456789"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese);
        textSetting.setTxtPrintPosition(new Position(122, 428));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(2);
        textSetting.setyMultiplication(2);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "HoneyWell Printer"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(105, 521));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "10 MIL QR"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(313, 518));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "15 MIL QR"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        barcodeSetting.setPosition(new Position(124, 545));
        barcodeSetting.setQrcodeDotSize(2);//accept value: Esc(1~15), Tsc(1~10) 宽度
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "10 MI QR*  12345678901234567890"));

        barcodeSetting.setPosition(new Position(333, 545));
        barcodeSetting.setQrcodeDotSize(3);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "*15 MI QR* 12345678901234567890"));

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(84, 633));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(67);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.UPC_A, barcodeSetting, "13827678947"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(291, 735));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*15 MIL Ean13*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(221, 758));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(70);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.EAN13, barcodeSetting, "1234567890128"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_12x20_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(45, 865));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_12x20_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(22, 893));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "abcdefghijklmnopqrstuvwxyz!@#$%^&*() +"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i=0; i<6; i++) {
            cmd.append(cmd.getLFCRCmd());
        }
        cmd.append(cmd.getPrintCopies(icopys));//TSC must add this function， TSC必须要加上这个方法，打印份数
        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
        rtPrinter.setPrintListener(new PrintListener() {
            @Override
            public void onPrinterStatus(PrinterStatusBean StatusBean) {
                if (StatusBean.printStatusCmd==PrintStatusCmd.cmd_PrintFinish) { //打印结束的状态
                    if (StatusBean.blPrintSucc) {
                        printTimes++;
                        ToastUtil.show(TempletPrintActivity.this, "print ok");

                    }
                    else {
                        LogUtils.d("mydebug","print fail:"+PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
                        ToastUtil.show(TempletPrintActivity.this, PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
                    }
                }
        }}
        );
     }
    private void DoTsctemplatePrint4(int icopys) throws SdkException {
        if (rtPrinter == null) {
            return;
        }
        int ileft=30;
        CmdFactory tscFac = new TscFactory();
        Cmd cmd = tscFac.create();

        cmd.append(cmd.getHeaderCmd());

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLabelGap(3);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        commonSetting.setLableSizeBean(new LableSizeBean(80, 120));//label width = 80mm, label height = 80mm
        cmd.append(cmd.getCommonSettingCmd(commonSetting));

        cmd.append(cmd.getReverse(10,70,45,790 ));//反白

        TextSetting textSetting = new TextSetting();
        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(105, 23));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*10 MIL C39*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPosition(new Position(115, 53));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(67);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(2);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(5);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE39, barcodeSetting, "10MIL C39"));
        cmd.append(cmd.getLFCRCmd());

        cmd.append(cmd.getDrawBox(81,131,548,500,8));//画框


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(321, 152));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "15 MIL C39"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(321, 187));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(83);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE39, barcodeSetting, "AA "));
        cmd.append(cmd.getLFCRCmd());

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(110, 300));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*15 MIL C128*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPosition(new Position(130, 334));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(84);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(9);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, "123456789"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese);
        textSetting.setTxtPrintPosition(new Position(122, 428));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(2);
        textSetting.setyMultiplication(2);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "HoneyWell Printer"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(105, 521));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "10 MIL QR"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(313, 518));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "15 MIL QR"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        barcodeSetting.setPosition(new Position(124, 545));
        barcodeSetting.setQrcodeDotSize(2);//accept value: Esc(1~15), Tsc(1~10) 宽度
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "10 MI QR*  12345678901234567890"));

        barcodeSetting.setPosition(new Position(333, 545));
        barcodeSetting.setQrcodeDotSize(3);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "*15 MI QR* 12345678901234567890"));

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(84, 633));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(67);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.UPC_A, barcodeSetting, "13827678947"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_8x12_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(291, 735));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "*15 MIL Ean13*"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(221, 758));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(70);//accept value:1~255 设置条码高度
        barcodeSetting.setNarrowInDot(3);//窄条码比例因子(dot)
        barcodeSetting.setWideInDot(6);//宽条码比例因子(dot)
        cmd.append(cmd.getBarcodeCmd(BarcodeType.EAN13, barcodeSetting, "1234567890128"));
        cmd.append(cmd.getLFCRCmd());


        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_12x20_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(45, 865));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_12x20_For_English_Number);//Font_TSS24_BF2_For_Simple_Chinese
        textSetting.setTxtPrintPosition(new Position(22, 893));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, "abcdefghijklmnopqrstuvwxyz!@#$%^&*() +"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i=0; i<6; i++) {
            cmd.append(cmd.getLFCRCmd());
        }
        cmd.append(cmd.getPrintCopies(icopys));//TSC must add this function， TSC必须要加上这个方法，打印份数
        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }


    private void TsctemplatePrint4(){ //连续打印，询问打印机状态后才打印，Print continuously, ask the printer status before printing.

        final EditText inputServer = new EditText(this);
        inputServer.setText("5");
        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Print_copys)).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton(getString(R.string.dialog_cancel), null);
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                int  icopys = Integer.valueOf(inputServer.getText().toString());
                TscContinuousPrinting(icopys);

            }
        });
        builder.show();

    }

    private void  TscContinuousPrinting(int icopys){
        printTimes = 0;
        maxPrintCopys=icopys;
        rtPrinter.setPrintListener(new PrintListener() {
            @Override
            public void onPrinterStatus(PrinterStatusBean StatusBean) {
                if (StatusBean.printStatusCmd==PrintStatusCmd.cmd_Normal){
                    if (StatusBean.blPrintReady) { //准备就续
                      //  LogUtils.d("mydebug","Query printer is Ready");
                         try {
                              DoTsctemplatePrint4(1);
                           } catch (SdkException e) {
                              e.printStackTrace();
                          }


                    } else {
                         //   LogUtils.d("mydebug","Query printer status:"+PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
                            ToastUtil.show(TempletPrintActivity.this, PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AskPrint();
                    }

                } else  if (StatusBean.printStatusCmd==PrintStatusCmd.cmd_PrintFinish) { //打印结束的状态
                    if (StatusBean.blPrintSucc) {
                        printTimes++;
                        ToastUtil.show(TempletPrintActivity.this, String.format("print ok[%d]",printTimes));
                        if (printTimes<maxPrintCopys)
                            AskPrint();
                    }
                    else {
                     //   LogUtils.d("mydebug","print fail:"+PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
                        ToastUtil.show(TempletPrintActivity.this, PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
                        AskPrint();
                    }
                }




            }

        });
        AskPrint();

    }

    private void  AskPrint(){
        CmdFactory tscFac = new TscFactory();
        Cmd tscCmd = tscFac.create();
        tscCmd.append(tscCmd.getPrintStausCmd(PrintStatusCmd.cmd_Normal));
        byte[] bytes = tscCmd.getAppendCmds();
        rtPrinter.writeMsgAsync(bytes);
       // LogUtils.d("mydebug","Send query data:"+ FuncUtils.ByteArrToHex(bytes));
    }

   //黑块测试1
    private void cpclPrint2() throws UnsupportedEncodingException, SdkException {
        if (rtPrinter == null) {
            return;
        }

        final EditText inputServer = new EditText(this);
        inputServer.setText("1");
        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Print_copys)).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton(getString(R.string.dialog_cancel), null);
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                int  icopys = Integer.valueOf(inputServer.getText().toString());
                CmdFactory cpclFactory = new CpclFactory();
                Cmd cmd = cpclFactory.create();
                Bitmap mBitmap = getAssetsBitmap("hei1.png");
                cmd.append(cmd.getCpclHeaderCmd(80, 60, icopys, 0));
                BitmapSetting bitmapSetting = new BitmapSetting();
                bitmapSetting.setPrintPostion(new Position(20, 20));
                bitmapSetting.setBimtapLimitWidth(72 * 8);
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getEndCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());
                }

            }
        });
        builder.show();




    }

    public Bitmap getAssetsBitmap(String path){
        //AssetManager am = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = getResources().getAssets().open(path);
          //  inputStream = am.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    private void printTemplet2() throws UnsupportedEncodingException, SdkException {
        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_TSC:
                TsctemplatePrint();
                break;
            case BaseEnum.CMD_CPCL:
                cpclPrint2();
                break;
            case BaseEnum.CMD_ESC:
                templetDemo.EsctemplatePrint2();
                break;
            default:
                break;
        }
    }
    private void printTemplet3() throws UnsupportedEncodingException, SdkException {
        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_TSC:
                TsctemplatePrint3();
                break;
            case BaseEnum.CMD_CPCL:
                cpclPrint3();
                break;

            default:
                break;
        }
    }

    private void cpclPrint3() throws UnsupportedEncodingException, SdkException {
        if (rtPrinter == null) {
            return;
        }
        int interheight=40;
        String str1="%d-ABCDEFGHIJKLMN1234567890";//
        String str2="%d-abcdefghijklmn1234567890";

        CmdFactory cpclFac = new CpclFactory();
        Cmd cmd = cpclFac.create();

        cmd.append(cmd.getCpclHeaderCmd(80, 130, 1, 0));//初始化，标签宽度80mm, 长度80mm， 打印份数为1

        TextSetting textSetting = new TextSetting();
        // textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        String s="";
        try {
            Position txtpos = new Position(10,10);
            textSetting.setTxtPrintPosition(txtpos);

            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_0);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,0)));
            txtpos.y += interheight-20;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_0);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,0)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_1);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,1)));
            txtpos.y += 60;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_1);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,1)));


            txtpos.y += 60;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_2);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,2)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_2);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,2)));

            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_3);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,3)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_3);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,3)));

            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_4);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,4)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_4);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,4)));

            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_5);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,5)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_5);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,5)));

            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_6);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,6)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_6);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,6)));

            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_7);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,7)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_7);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,7)));


            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_Chinese_24x24);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,24)));
            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_Chinese_24x24);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,24)));

            txtpos.y += interheight;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_Chinese_16x16_custom);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str1,16)));
            txtpos.y += interheight-10;
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_Chinese_16x16_custom);
            cmd.append(cmd.getTextCmd(textSetting, String.format(str2,16)));
//            txtpos.y = 100;
//            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_3);
//            cmd.append(cmd.getTextCmd(textSetting, String.format(str,3)));
//
//            textSetting.setTxtPrintPosition(new Position(20, 25));
//            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_4);
//            cmd.append(cmd.getTextCmd(textSetting, String.format(str,4)));
            //  }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        cmd.append(cmd.getLFCRCmd());
//        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }




}
