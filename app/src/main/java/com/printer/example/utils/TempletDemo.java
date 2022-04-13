/**
 * 模板打印，都要移动此单元
 */



package com.printer.example.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.printer.example.R;
import com.rt.printerlibrary.bean.LableSizeBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscFactory;
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

import java.io.UnsupportedEncodingException;

public class TempletDemo {
    private RTPrinter rtPrinter;
    private Context context;
    String title, content_tel, content_email, barcode_str;
    private static  TempletDemo templetDemo;

    public TempletDemo(RTPrinter rtPrinter, Context context){
        this.rtPrinter = rtPrinter;
        this.context = context;
    }
    public  static TempletDemo getInstance(RTPrinter rtPrinter, Context context){
        if (templetDemo==null)
            templetDemo = new TempletDemo(rtPrinter,context);
         else{
            templetDemo.rtPrinter = rtPrinter;
            templetDemo.context = context;
        }
        templetDemo.initdata();
        return  templetDemo;
    }
    private void initdata(){
        title = context.getString(R.string.temp1_title1_printer_tech);
        content_tel = context.getString(R.string.temp1_content1_tel);
        content_email = context.getString(R.string.temp1_content2_email);
        barcode_str = "123456789";
    }


    public void tscPrint() throws SdkException {
        if (rtPrinter == null) {
            return;
        }

        CmdFactory tscFac = new TscFactory();
        Cmd cmd = tscFac.create();

        cmd.append(cmd.getHeaderCmd());

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLabelGap(3);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        commonSetting.setLableSizeBean(new LableSizeBean(80, 80));//label width = 80mm, label height = 80mm
        cmd.append(cmd.getCommonSettingCmd(commonSetting));

        //(int x0, int y0, int x1, int y1, int lineWidth)
        cmd.append(cmd.getDrawLine(50,50,180,50,2));//横线
        cmd.append(cmd.getDrawLine(50,100,50,200,2));//竖线

        TextSetting textSetting = new TextSetting();
        textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese);
        textSetting.setTxtPrintPosition(new Position(80, 80));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        try {
            cmd.append(cmd.getTextCmd(textSetting, title));
            textSetting.setTxtPrintPosition(new Position(120, 140));
            textSetting.setxMultiplication(1);
            textSetting.setyMultiplication(1);
            textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese);
            cmd.append(cmd.getTextCmd(textSetting, content_tel));
            textSetting.setTxtPrintPosition(new Position(80, 180));
            cmd.append(cmd.getTextCmd(textSetting, content_email));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(40);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm
        bitmapSetting.setPrintPostion(new Position(250, 220));
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);


        cmd.append(cmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));

        cmd.append(cmd.getLFCRCmd());//回车换行

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(180, 280));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(72);//accept value:1~255
        barcodeSetting.setBarcodeWidth(3);//accept value:2~6
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, barcode_str));

        cmd.append(cmd.getLFCRCmd());

        barcodeSetting.setPosition(new Position(220, 420));
        barcodeSetting.setQrcodeDotSize(5);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, content_email));


        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getPrintCopies(1));//TSC must add this function， TSC必须要加上这个方法，打印份数
        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());

    }
    public void EsctemplatePrint2()  {

        try {
            CommonSetting commonSetting = new CommonSetting();
            CmdFactory EscFac = new EscFactory();
            Cmd escCmd = EscFac.create();

            BarcodeSetting barcodeSetting = new BarcodeSetting();
            TextSetting textSetting = new TextSetting();
            //  版面居中
            commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

            //  第一个二维码
            barcodeSetting.setQrcodeDotSize(4); //  放大倍数为 4
            barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.M); //  纠错
            escCmd.append(escCmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "http://weixin.qq.com/r/MSqqsnjEoiXdrTzR938j"));
            escCmd.append(escCmd.getLFCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, "7FRESH-DZ-00188"));   //  后面不加 LF，保证后面的内容在同一行
            textSetting.setDoubleHeight(SettingEnum.Enable);    //  字体倍高
            escCmd.append(escCmd.getTextCmd(textSetting, "5925"));
            escCmd.append(escCmd.getLFCmd());   //  打印一行
            escCmd.append(escCmd.getLFCmd());   //  空一行

            textSetting.setDoubleHeight(SettingEnum.Enable);    //  倍高字体
            textSetting.setDoubleWidth(SettingEnum.Enable); //  倍宽字体
            escCmd.append(escCmd.getTextCmd(textSetting, "预发测试门店2"));
            escCmd.append(escCmd.getLFCmd());

            //  正常字体，打印一条水平线
            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());

            //  左对齐，倍高字体
            commonSetting.setAlign(CommonEnum.ALIGN_LEFT);
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
            textSetting.setDoubleHeight(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, "        观鹿台路区"));
            escCmd.append(escCmd.getLFCmd());

            //  水平线
            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());

            commonSetting.setAlign(CommonEnum.ALIGN_LEFT);
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
            textSetting.setDoubleHeight(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, "        4箱1件         |        1671  1671"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "                       |        1671  1671"));
            escCmd.append(escCmd.getLFCmd());

            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, "【测试】-saas化箱规商品999999999999"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "                                      1个"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "拣货员：dzzhuangss       打包员：dzzhuangss"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "打印时间：2019-06-05 10:31:24"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());

            //  居中对齐
            commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

            //  打印图标
            Bitmap bitLogo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.freshlogo);
            if (bitLogo != null) {
                BitmapSetting bitmapSetting = new BitmapSetting();
                bitmapSetting.setBimtapLimitWidth(208);
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                escCmd.append(escCmd.getBitmapCmd(bitmapSetting, bitLogo));
            }

            textSetting.setDoubleHeight(SettingEnum.Enable);
            textSetting.setDoubleWidth(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, "预发测试门店2"));
            escCmd.append(escCmd.getLFCmd());
            //escCmd.append(escCmd.getLFCmd());

            //  恢复条码默认字体
/*            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting, "  "));
            escCmd.append(escCmd.getLFCmd());*/

            //  128码
            barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
            barcodeSetting.setHeightInDot(120); //  码高 120 点
            barcodeSetting.setBarcodeWidth(2);   //  放大倍数 2
            escCmd.append(escCmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, "670006394135"));

            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);
            commonSetting.setAlign(CommonEnum.ALIGN_LEFT);
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, "订单编号：670006394135"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "客    户：姜萍"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "电    话：186****8804"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "地    址："));
            textSetting.setDoubleHeight(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, "南海子公园-观鹿台1123"));
            escCmd.append(escCmd.getLFCmd());
            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "品名                  数量                  合计"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "6415-【测试】-saas化箱规商品999999999999"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "                      1个                   1.5"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "------------------------------------------------"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "                                    商品件数：1"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, "温馨提示：如有差额退款，将会在签收后原路返还给账户，请注意查收。"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "客户备注：所购商品如遇缺货，您需要：其他商品继续配送(缺货商品退款)。"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());

            commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

            escCmd.append(escCmd.getTextCmd(textSetting, "7FRESH新版小程序上线啦，快来体验尝鲜！"));
            escCmd.append(escCmd.getLFCmd());

            //  二维码
            barcodeSetting.setQrcodeDotSize(4);
            barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.M);
            escCmd.append(escCmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, "http://weixin.qq.com/r/MSqqsnjEoiXdrTzR938j"));
            escCmd.append(escCmd.getLFCmd());

            escCmd.append(escCmd.getTextCmd(textSetting, "微信扫码关注7FRESH公众号"));
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "1/1"));
            escCmd.append(escCmd.getLFCmd());

            //  最后额外走纸，便于撕纸
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());
            escCmd.append(escCmd.getLFCmd());

            //  将所有指令发送到打印机，完成打印
            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (SdkException e) {
            e.printStackTrace();
        }

    }
    public void escTemplet() throws UnsupportedEncodingException, SdkException {
        CmdFactory escFac = new EscFactory();
        Cmd escCmd = escFac.create();
        escCmd.setChartsetName("UTF-8");
        TextSetting textSetting = new TextSetting();
        textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//对齐方式-左对齐，居中，右对齐
        textSetting.setBold(SettingEnum.Enable);//加粗
        textSetting.setUnderline(SettingEnum.Disable);//下划线
        textSetting.setIsAntiWhite(SettingEnum.Disable);//反白
        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
        textSetting.setItalic(SettingEnum.Disable);//斜体
        textSetting.setIsEscSmallCharactor(SettingEnum.Disable);//小字体
        escCmd.append(escCmd.getHeaderCmd());//初始化
        escCmd.append(escCmd.getTextCmd(textSetting, title));
        escCmd.append(escCmd.getLFCRCmd());//回车换行

        textSetting.setIsEscSmallCharactor(SettingEnum.Enable);
        textSetting.setBold(SettingEnum.Disable);
        textSetting.setDoubleHeight(SettingEnum.Disable);
        textSetting.setDoubleWidth(SettingEnum.Disable);
        escCmd.append(escCmd.getTextCmd(textSetting, content_tel));

        escCmd.append(escCmd.getLFCRCmd());
        textSetting.setUnderline(SettingEnum.Enable);
        escCmd.append(escCmd.getTextCmd(textSetting, content_email));

        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());

        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(40);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm

        Bitmap bmp  = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
        escCmd.append(escCmd.getLFCRCmd());

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setEscBarcodePrintOritention(EscBarcodePrintOritention.Rotate0);
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setHeightInDot(72);//accept value:1~255
        barcodeSetting.setBarcodeWidth(3);//accept value:2~6
        escCmd.append(escCmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, barcode_str));
        escCmd.append(escCmd.getLFCRCmd());

        barcodeSetting.setQrcodeDotSize(5);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        escCmd.append(escCmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, content_email));

        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());

        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
    }
    public void esc80TempPrint() {

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
            String preBlank = "";
            textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            textSetting.setBold(SettingEnum.Enable);
            textSetting.setUnderline(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "PROFORMA INVOICE"));
            escCmd.append(escCmd.getLFCRCmd());
            textSetting.setUnderline(SettingEnum.Disable);
            textSetting.setBold(SettingEnum.Disable);
            textSetting.setAlign(CommonEnum.ALIGN_LEFT);
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Hawkins Cookers Limited "));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "ROHTAK ROAD, PUNJABI BAGH,"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "NEW DELHI, DELHI- 110035"));
            escCmd.append(escCmd.getLFCRCmd());


            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "------------------------------------------------"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "D1rcd: 8888888"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "D1r Name: BK Sons & Crockery"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "so No: DLSO AWXTR10"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, "so Dt: ; 26 Apr 2019"));
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "------------------------------------------------"));
            escCmd.append(escCmd.getLFCRCmd());
            textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            textSetting.setBold(SettingEnum.Enable);
            textSetting.setUnderline(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting,  "PRODUCT DETAILS"));
            escCmd.append(escCmd.getLFCRCmd());
            textSetting.setAlign(CommonEnum.ALIGN_LEFT);
            textSetting.setUnderline(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting,  "   Product   MRP   Qty   DisC(Rs.)    Amt (Rs.)"));
            escCmd.append(escCmd.getLFCRCmd());
            textSetting.setBold(SettingEnum.Disable);
            textSetting.setAlign(CommonEnum.ALIGN_LEFT);
            escCmd.append(escCmd.getTextCmd(textSetting,  "Classic 3L  500   10      500         4500\n\r"));//也可以用  escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getTextCmd(textSetting,  "Classic 5L  1000  10      1000        9000\n\r"));
            textSetting.setBold(SettingEnum.Enable);
            escCmd.append(escCmd.getTextCmd(textSetting,  "           Total:  20     1500       13500\n\r"));
            textSetting.setBold(SettingEnum.Disable);
            escCmd.append(escCmd.getTextCmd(textSetting,  "------------------------------------------------"));
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

    public void escTicketTemplet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();
                    escCmd.append(escCmd.getHeaderCmd());//初始化
                    escCmd.setChartsetName("UTF-8");

                    CommonSetting commonSetting = new CommonSetting();
                    commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBimtapLimitWidth(28 * 8);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm
                    Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_rongta);
                    try {
                        escCmd.append(escCmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }
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

                    escCmd.append(escCmd.getTextCmd(textSetting, "The Red Rose"));
                    escCmd.append(escCmd.getLFCRCmd());//回车换行
                    textSetting.setBold(SettingEnum.Disable);
                    textSetting.setDoubleHeight(SettingEnum.Disable);//倍高
                    textSetting.setDoubleWidth(SettingEnum.Disable);//倍宽
                    textSetting.setIsEscSmallCharactor(SettingEnum.Enable);//小字体
                    escCmd.append(escCmd.getTextCmd(textSetting, "Indian Resturant"));
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getTextCmd(textSetting, "Noida, Noida"));
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getTextCmd(textSetting, "Website:http://www.xxx.com"));
                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getTextCmd(textSetting, "GSTIN No.:ROS2345ST"));
                    escCmd.append(escCmd.getLFCRCmd());
                    String line = "—————————————————————————————————————————";
                    textSetting.setBold(SettingEnum.Enable);
                    escCmd.append(escCmd.getTextCmd(textSetting, line));
                    escCmd.append(escCmd.getLFCRCmd());
                    textSetting.setBold(SettingEnum.Disable);
                    String bank = "                          ";
                    escCmd.append(escCmd.getTextCmd(textSetting, "Type:" + bank + "Table:2"));
                    escCmd.append(escCmd.getLFCRCmd());//回车换行
                    escCmd.append(escCmd.getTextCmd(textSetting, "Type:" + bank + "Table:2"));
                    escCmd.append(escCmd.getLFCRCmd());//回车换行
                    escCmd.append(escCmd.getTextCmd(textSetting, "Type:" + bank + "Table:2"));
                    escCmd.append(escCmd.getLFCRCmd());//回车换行
                    textSetting.setBold(SettingEnum.Enable);
                    escCmd.append(escCmd.getTextCmd(textSetting, line));
                    escCmd.append(escCmd.getLFCRCmd());

                    escCmd.append(escCmd.getTextCmd(textSetting, "Item Name      Qty       Rate      Amount\n"));

                    textSetting.setBold(SettingEnum.Enable);
                    escCmd.append(escCmd.getTextCmd(textSetting, line));
                    escCmd.append(escCmd.getLFCRCmd());

                    textSetting.setBold(SettingEnum.Disable);
                    escCmd.append(escCmd.getTextCmd(textSetting, "Item111        2.00      83.33    150.0000\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Item111        2.00      83.33    150.0000\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Item111        2.00      83.33    150.0000\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Item111        2.00      83.33    150.0000\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Item111        2.00      83.33    150.0000\n"));

                    textSetting.setBold(SettingEnum.Enable);
                    escCmd.append(escCmd.getTextCmd(textSetting, line));
                    escCmd.append(escCmd.getLFCRCmd());

                    textSetting.setBold(SettingEnum.Disable);
                    escCmd.append(escCmd.getTextCmd(textSetting, "            Sub Total             750.0000\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "            @Oval                        0\n"));

                    textSetting.setBold(SettingEnum.Enable);
                    escCmd.append(escCmd.getTextCmd(textSetting, line));
                    escCmd.append(escCmd.getLFCRCmd());

                    textSetting.setIsEscSmallCharactor(SettingEnum.Disable);//小字体
                    textSetting.setBold(SettingEnum.Enable);//加粗
                    escCmd.append(escCmd.getTextCmd(textSetting, "     Net Amount         2524.98\n"));

                    textSetting.setBold(SettingEnum.Enable);
                    textSetting.setIsEscSmallCharactor(SettingEnum.Enable);//小字体
                    escCmd.append(escCmd.getTextCmd(textSetting, line));
                    escCmd.append(escCmd.getLFCRCmd());

                    textSetting.setBold(SettingEnum.Disable);
                    textSetting.setIsEscSmallCharactor(SettingEnum.Enable);//小字体
                    escCmd.append(escCmd.getTextCmd(textSetting, "KOT(s): KOT_23,KOT_24,KOT_31               \n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Guest Signature:              ___________\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Authorised Signatory:         ___________\n"));
                    escCmd.append(escCmd.getTextCmd(textSetting, "Cashier:                                   \n"));

                    textSetting.setItalic(SettingEnum.Enable);
                    textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                    textSetting.setIsEscSmallCharactor(SettingEnum.Disable);//小字体
                    escCmd.append(escCmd.getTextCmd(textSetting, "Have a nice day.\nThank you visit again"));

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
        }).start();

    }
    public void cpclPrint() throws UnsupportedEncodingException, SdkException {
        if (rtPrinter == null) {
            return;
        }

        CmdFactory cpclFac = new CpclFactory();
        Cmd cmd = cpclFac.create();

        cmd.append(cmd.getCpclHeaderCmd(80, 80, 1, 0));//初始化，标签宽度80mm, 长度80mm， 打印份数为1

        TextSetting textSetting = new TextSetting();
        textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_3);
        textSetting.setTxtPrintPosition(new Position(80, 80));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(2);
        textSetting.setyMultiplication(2);
        try {
            cmd.append(cmd.getTextCmd(textSetting, title));
            textSetting.setTxtPrintPosition(new Position(120, 140));
            textSetting.setxMultiplication(1);
            textSetting.setyMultiplication(1);
            textSetting.setCpclFontTypeEnum(CpclFontTypeEnum.Font_2);
            cmd.append(cmd.getTextCmd(textSetting, content_tel));
            textSetting.setTxtPrintPosition(new Position(80, 180));
            cmd.append(cmd.getTextCmd(textSetting, content_email));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(40);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm
        bitmapSetting.setPrintPostion(new Position(250, 220));

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        cmd.append(cmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
        cmd.append(cmd.getLFCRCmd());

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setEscBarcodePrintOritention(EscBarcodePrintOritention.Rotate0);
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(180, 280));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(72);//accept value:1~255
        barcodeSetting.setBarcodeWidth(3);//accept value:2~6
        cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, barcode_str));
        cmd.append(cmd.getLFCRCmd());

        barcodeSetting.setPosition(new Position(220, 420));
        barcodeSetting.setQrcodeDotSize(5);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, content_email));

        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getLFCRCmd());


        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }

    public void zplPrint() throws UnsupportedEncodingException, SdkException {
        if (rtPrinter == null) {
            return;
        }

        CmdFactory fac = new ZplFactory();
        Cmd cmd = fac.create();

        cmd.append(cmd.getHeaderCmd());

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLabelGap(2);
        commonSetting.setPrintDirection(PrintDirection.REVERSE);//打印方向
        commonSetting.setLableSizeBean(new LableSizeBean(80, 80));//label width = 80mm, label height = 80mm
        cmd.append(cmd.getCommonSettingCmd(commonSetting));


        TextSetting textSetting = new TextSetting();
        textSetting.setZplFontTypeEnum(ZplFontTypeEnum.FONT_DOWNLOAD_FONT);
        textSetting.setTxtPrintPosition(new Position(130, 80));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setZplHeightFactor(64);// >10
        textSetting.setZplWidthFactor(64);//>10
        try {
            cmd.append(cmd.getTextCmd(textSetting, title));
            textSetting.setTxtPrintPosition(new Position(160, 140));
            textSetting.setZplHeightFactor(2);// 1~10
            textSetting.setZplWidthFactor(2);// 1~10
            textSetting.setZplFontTypeEnum(ZplFontTypeEnum.FONT_2);
            cmd.append(cmd.getTextCmd(textSetting, content_tel));
            textSetting.setTxtPrintPosition(new Position(70, 180));
            cmd.append(cmd.getTextCmd(textSetting, content_email));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BitmapSetting bitmapSetting = new BitmapSetting();
        bitmapSetting.setBimtapLimitWidth(40);//限制图片最大宽度 58打印机=48mm， 80打印机=72mm
        bitmapSetting.setPrintPostion(new Position(250, 220));

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        try {
            cmd.append(cmd.getBitmapCmd(bitmapSetting, Bitmap.createBitmap(bmp)));
        } catch (SdkException e) {
            e.printStackTrace();
        }
        cmd.append(cmd.getLFCRCmd());

        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPosition(new Position(120, 280));
        barcodeSetting.setPrintRotation(PrintRotation.Rotate0);
        barcodeSetting.setHeightInDot(72);//accept value:1~255
        barcodeSetting.setBarcodeWidth(3);//accept value:2~6
        try {
            cmd.append(cmd.getBarcodeCmd(BarcodeType.CODE128, barcodeSetting, barcode_str));
        } catch (SdkException e) {
            e.printStackTrace();
        }
        cmd.append(cmd.getLFCRCmd());

        barcodeSetting.setPosition(new Position(200, 420));
        barcodeSetting.setQrcodeDotSize(5);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setQrcodeEccLevel(QrcodeEccLevel.L);
        try {
            cmd.append(cmd.getBarcodeCmd(BarcodeType.QR_CODE, barcodeSetting, content_email));
        } catch (SdkException e) {
            e.printStackTrace();
        }

        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getLFCRCmd());

        try {
            cmd.append(cmd.getPrintCopies(1));//ZPL must add this function, print copies settings， ZPL必须要加上这个方法，打印份数
        } catch (SdkException e) {
            e.printStackTrace();
        }
        cmd.append(cmd.getEndCmd());
        String str = new String(cmd.getAppendCmds());
        Log.e("sss", str);
        rtPrinter.writeMsg(cmd.getAppendCmds());

    }



}
