package com.printer.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.Toast;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.BaseEnum;
import com.printer.example.utils.FuncUtils;
import com.printer.example.utils.LogUtils;
import com.printer.example.utils.ToastUtil;
import com.rt.printerlibrary.bean.LableSizeBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.bean.PrinterStatusBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscCmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.PinCmd;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.cmd.ZplFactory;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.CpclFontTypeEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.PinLineSpaceEnum;
import com.rt.printerlibrary.enumerate.PrintDirection;
import com.rt.printerlibrary.enumerate.PrintRotation;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.enumerate.SpeedEnum;
import com.rt.printerlibrary.enumerate.TscFontTypeEnum;
import com.rt.printerlibrary.enumerate.ZplFontTypeEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.rt.printerlibrary.utils.PrintListener;
import com.rt.printerlibrary.utils.PrintStatusCmd;
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils;

import java.io.UnsupportedEncodingException;

/**
 * ESC->ESCTextPrintActivity,  [CPCL,TSC,ZPL,PIN]-->TextPrintActivity
 */
public class TextPrintActivity extends BaseActivity implements View.OnClickListener {

    private EditText et_text;
    private Button btn_txtprint, btn_select_chartsetname, btn_font_select;

    //Label[CPCL]
    private EditText et_cpcl_size, et_enlarge, et_spacing;
    private CheckBox ck_bold, ck_underline;
    private RadioGroup rg_align_group;
    private TableRow tr_size, tr_enlarge, tr_spacing, tr_bold_underline;


    private RTPrinter rtPrinter;
    private String printStr;
    private String mChartsetName = "GBK";
    private TscFontTypeEnum chooseTscFont = TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese;
    private CpclFontTypeEnum chooseCpclFont = CpclFontTypeEnum.Font_Chinese_24x24;
    private String[] fontChooseStrs;
    private int curCmdType;
    private int printTimes=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_print);
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {
        et_text = findViewById(R.id.et_text);
        btn_txtprint = findViewById(R.id.btn_txtprint);
        btn_select_chartsetname = findViewById(R.id.btn_select_chartsetname);
        btn_font_select = findViewById(R.id.btn_font_select);
        et_cpcl_size = findViewById(R.id.et_cpcl_size);
        et_enlarge = findViewById(R.id.et_enlarge);
        et_spacing = findViewById(R.id.et_spacing);
        ck_bold = findViewById(R.id.ck_bold);
        ck_underline = findViewById(R.id.ck_underline);
        rg_align_group = findViewById(R.id.rg_align_group);
        tr_size = findViewById(R.id.tr_size);
        tr_enlarge = findViewById(R.id.tr_enlarge);
        tr_spacing = findViewById(R.id.tr_spacing);
        tr_bold_underline = findViewById(R.id.tr_bold_underline);
    }

    @Override
    public void addListener() {
        btn_txtprint.setOnClickListener(this);
        btn_select_chartsetname.setOnClickListener(this);
        btn_font_select.setOnClickListener(this);
    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
        curCmdType = BaseApplication.getInstance().getCurrentCmdType();
        switch (curCmdType) {
            case BaseEnum.CMD_TSC:
                btn_font_select.setVisibility(View.VISIBLE);
                btn_font_select.setText(chooseTscFont.name());
                rg_align_group.setVisibility(View.GONE);
                tr_size.setVisibility(View.GONE);
                tr_enlarge.setVisibility(View.GONE);
                tr_spacing.setVisibility(View.GONE);
                tr_bold_underline.setVisibility(View.GONE);
                break;
            case BaseEnum.CMD_CPCL:
                btn_font_select.setVisibility(View.VISIBLE);
                btn_font_select.setText(chooseCpclFont.name());
                rg_align_group.setVisibility(View.VISIBLE);
                tr_size.setVisibility(View.VISIBLE);
                tr_enlarge.setVisibility(View.VISIBLE);
                tr_spacing.setVisibility(View.VISIBLE);
                tr_bold_underline.setVisibility(View.VISIBLE);
                break;
            default:
                btn_font_select.setVisibility(View.GONE);
                rg_align_group.setVisibility(View.GONE);
                tr_size.setVisibility(View.GONE);
                tr_enlarge.setVisibility(View.GONE);
                tr_spacing.setVisibility(View.GONE);
                tr_bold_underline.setVisibility(View.GONE);
                break;
        }
    }

    private void textPrint() throws UnsupportedEncodingException {
        printStr = et_text.getText().toString();

        if (TextUtils.isEmpty(printStr)) {
            printStr = "Hello Printer";
        }

        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_PIN:
                pinTextPrint();
                break;
            case BaseEnum.CMD_CPCL:
                cpclPrint();
                break;
            case BaseEnum.CMD_TSC:
                tscPrint();
                break;
            case BaseEnum.CMD_ZPL:
                zplTextPrint();
                break;
            default:
                break;
        }
    }


    private void tscPrint() throws UnsupportedEncodingException {
        if (rtPrinter == null) {
            return;
        }

        CmdFactory tscFac = new TscFactory();
        Cmd tscCmd = tscFac.create();

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLableSizeBean(new LableSizeBean(80, 40));
        commonSetting.setLabelGap(3);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        tscCmd.append(tscCmd.getHeaderCmd());
        tscCmd.append(tscCmd.getCommonSettingCmd(commonSetting));


        TextSetting textSetting = new TextSetting();
        //textSetting.setTscFontTypeEnum(TscFontTypeEnum.Font_TSS24_BF2_For_Simple_Chinese);
        textSetting.setTscFontTypeEnum(chooseTscFont);
        //textSetting.setBold(SettingEnum.Enable);//增加加粗 2.0.29暂时为定制机子能用

        int x = 80;
        int y = 80;
        Position position = new Position(x, y);


        textSetting.setTxtPrintPosition(position);
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        textSetting.setxMultiplication(1);
        textSetting.setyMultiplication(1);
        tscCmd.append(tscCmd.getTextCmd(textSetting, printStr, mChartsetName));
//        position.y = position.y + 30;
//        textSetting.setTxtPrintPosition(position);
//        textSetting.setxMultiplication(2);
//        textSetting.setyMultiplication(2);
//        tscCmd.append(tscCmd.getTextCmd(textSetting, printStr, mChartsetName));

        Log.e("Fuuu", "TextPrint:" + FuncUtils.ByteArrToHex(tscCmd.getTextCmd(textSetting, printStr, mChartsetName)));
        try {
            tscCmd.append(tscCmd.getPrintCopies(1));
        } catch (SdkException e) {
            e.printStackTrace();
        }
        tscCmd.append(tscCmd.getEndCmd());
        rtPrinter.writeMsgAsync(tscCmd.getAppendCmds());
        rtPrinter.setPrintListener(new PrintListener() {
            @Override
            public void onPrinterStatus(PrinterStatusBean StatusBean) {
                if (StatusBean.printStatusCmd==PrintStatusCmd.cmd_PrintFinish) { //打印结束的状态
                    if (StatusBean.blPrintSucc)
                        ToastUtil.show(getBaseContext(), String.format("print ok"));
                     else
                     ToastUtil.show(getBaseContext(), PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));

            }
        }
        });

//        Log.e("Fuuu", new String(tscCmd.getAppendCmds(), mChartsetName));
    }

    private void cpclPrint() {
        if (rtPrinter == null) {
            return;
        }
        String enlarge = et_enlarge.getText().toString();
        String size = et_cpcl_size.getText().toString();
        String spacing = et_spacing.getText().toString();
        if (TextUtils.isEmpty(size)) {
            size = "0";
        }
        if (TextUtils.isEmpty(enlarge)) {
            enlarge = "1";
        }


        CmdFactory cpclFac = new CpclFactory();
        Cmd cmd = cpclFac.create();
        int printCopies = 1;
        cmd.append(cmd.getCpclHeaderCmd(Integer.parseInt(BaseApplication.labelWidth), Integer.parseInt(BaseApplication.labelHeight), printCopies, Integer.parseInt(BaseApplication.labelOffset)));//width, height, copies, offset

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setSpeedEnum(SpeedEnum.getEnumByString(BaseApplication.labelSpeed));
        cmd.append(cmd.getCommonSettingCmd(commonSetting));

        TextSetting textSetting = new TextSetting();
        textSetting.setCpclFontTypeEnum(chooseCpclFont);
        textSetting.setTxtPrintPosition(new Position(0, 0));
        textSetting.setPrintRotation(PrintRotation.Rotate0);


        textSetting.setxMultiplication(Integer.parseInt(enlarge));
        textSetting.setyMultiplication(Integer.parseInt(enlarge));
        textSetting.setCpclFontSize(Integer.parseInt(size));

        textSetting.setBold(ck_bold.isChecked() ? SettingEnum.Enable : SettingEnum.Disable);
        textSetting.setUnderline(ck_underline.isChecked() ? SettingEnum.Enable : SettingEnum.Disable);

        switch (rg_align_group.getCheckedRadioButtonId()) {
            case R.id.rb_align_left:
                textSetting.setAlign(CommonEnum.ALIGN_LEFT);
                break;
            case R.id.rb_align_middle:
                textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                break;
            case R.id.rb_align_right:
                textSetting.setAlign(CommonEnum.ALIGN_RIGHT);
                break;
            default:
                break;
        }

        if (TextUtils.isEmpty(spacing)) {
            textSetting.setCpclTextSpacing(-1);//-1 = No Setting
        } else {
            textSetting.setCpclTextSpacing(Integer.parseInt(spacing));
        }


        try {
            cmd.append(cmd.getTextCmd(textSetting, printStr));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        cmd.append(cmd.getEndCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }

    private void escPrint() throws UnsupportedEncodingException {
        if (rtPrinter != null) {
            CmdFactory escFac = new EscFactory();
            Cmd escCmd = escFac.create();
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial

            escCmd.setChartsetName(mChartsetName);
            TextSetting textSetting = new TextSetting();
            textSetting.setAlign(CommonEnum.ALIGN_LEFT);//对齐方式-左对齐，居中，右对齐
            textSetting.setBold(SettingEnum.Disable);
            textSetting.setUnderline(SettingEnum.Disable);
            textSetting.setIsAntiWhite(SettingEnum.Disable);
            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);

            escCmd.append(escCmd.getTextCmd(textSetting, printStr, mChartsetName));

            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial
            escCmd.append(escCmd.getLFCRCmd());

            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());

        }
    }

    private void pinTextPrint() throws UnsupportedEncodingException {

        if (rtPrinter == null) {
            return;
        }

        TextSetting textSetting = new TextSetting();
        textSetting.setBold(SettingEnum.Enable);//加粗
        textSetting.setAlign(CommonEnum.ALIGN_LEFT);
//        textSetting.setFontStyle(SettingEnum.FONT_STYLE_SHADOW);
//        textSetting.setItalic(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
        textSetting.setDoublePrinting(SettingEnum.Enable);//重叠打印
//        textSetting.setPinPrintMode(CommonEnum.PIN_PRINT_MODE_Bidirectional);
        textSetting.setUnderline(SettingEnum.Enable);//下划线

        CmdFactory cmdFactory = new PinFactory();
        Cmd cmd = cmdFactory.create();
        cmd.append(cmd.getHeaderCmd());//初始化
        cmd.append(cmd.getTextCmd(textSetting, printStr, mChartsetName));


        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        cmd.append(cmd.getTextCmd(textSetting, printStr, mChartsetName));
//        cmd.append(((PinCmd)cmd).getJumpingRow180thCmd(PrintDirection.NORMAL,(byte) 10));
//        cmd.append(((PinCmd)cmd).getAbsolutePrintPositiionCmd(50));
//

        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLableSizeBean(new LableSizeBean(80, 40));
        commonSetting.setPrintDirection(PrintDirection.REVERSE);


//        textSetting.setDoubleHeight(SettingEnum.Disable);//倍高
//        cmd.append(cmd.getTextCmd(textSetting, printStr, mChartsetName));



        cmd.append(cmd.getLFCRCmd());//换行
        cmd.append(cmd.getEndCmd());//退纸





       // rtPrinter.readMsg()
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }

    private void zplTextPrint() {

        CmdFactory zplFac = new ZplFactory();
        Cmd zplCmd = zplFac.create();

        zplCmd.append(zplCmd.getHeaderCmd());
        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLableSizeBean(new LableSizeBean(80, 40));
        commonSetting.setPrintDirection(PrintDirection.REVERSE);
        zplCmd.append(zplCmd.getHeaderCmd());
        zplCmd.append(zplCmd.getCommonSettingCmd(commonSetting));

        TextSetting textSetting = new TextSetting();
        textSetting.setZplFontTypeEnum(ZplFontTypeEnum.FONT_DOWNLOAD_FONT);
        textSetting.setZplHeightFactor(30);// >10//放大倍数
        textSetting.setZplWidthFactor(30);//>10
        textSetting.setTxtPrintPosition(new Position(80, 80));
        textSetting.setPrintRotation(PrintRotation.Rotate0);
        try {
            zplCmd.append(zplCmd.getTextCmd(textSetting, printStr, mChartsetName));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            zplCmd.append(zplCmd.getPrintCopies(1));
        } catch (SdkException e) {
            e.printStackTrace();
        }
        zplCmd.append(zplCmd.getEndCmd());
        if (rtPrinter != null) {
            rtPrinter.writeMsgAsync(zplCmd.getAppendCmds());
        }

    }

    private void showSelectChartsetnameDialog() {
        final String[] chartsetNameArray = new String[]{"UTF-8", "GBK", "BIG5"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.dialog_title_chartset_setting);
        dialog.setItems(chartsetNameArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                mChartsetName = chartsetNameArray[pos];
                btn_select_chartsetname.setText(mChartsetName);
            }
        });
        dialog.setNegativeButton(R.string.dialog_cancel, null);
        dialog.show();
    }

    /**
     * Font Choose [TSC]
     */
    private void showChooseFontDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.dialog_title_chartset_setting);
        if (curCmdType == BaseEnum.CMD_TSC) {
            int length = TscFontTypeEnum.values().length;
            fontChooseStrs = new String[length];
            for (int i = 0; i < length; i++) {
                fontChooseStrs[i] = TscFontTypeEnum.values()[i].name();
            }
            dialog.setItems(fontChooseStrs, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    chooseTscFont = TscFontTypeEnum.valueOf(fontChooseStrs[pos]);
                    btn_font_select.setText(fontChooseStrs[pos]);
                }
            });
        } else if (curCmdType == BaseEnum.CMD_CPCL) {
            int length = CpclFontTypeEnum.values().length;
            fontChooseStrs = new String[length];
            for (int i = 0; i < length; i++) {
                fontChooseStrs[i] = CpclFontTypeEnum.values()[i].name();
            }
            dialog.setItems(fontChooseStrs, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    chooseCpclFont = CpclFontTypeEnum.valueOf(fontChooseStrs[pos]);
                    btn_font_select.setText(fontChooseStrs[pos]);
                }
            });
        }
        dialog.setNegativeButton(R.string.dialog_cancel, null);
        dialog.show();
    }


    /**
     * Demo code for the line spacing setup test. If you turn off the printer,
     * the line spacing setting will return to the default value,
     * which only applies to the current state.
     * 行间距打印例子，仅当前打印有效，开关机之后恢复默认行距
     *
     * @throws UnsupportedEncodingException
     */
    private void lineSpaceTest() throws UnsupportedEncodingException {
        if (rtPrinter != null) {
            CmdFactory cmdFactory = new PinFactory();// PIN Printer must use  PinFactory to create Cmd object.
            Cmd cmd = cmdFactory.create();

            CommonSetting commonSetting = new CommonSetting();
            commonSetting.setAbsolutionPositionN(30);//设置打印绝对定点位置


            TextSetting textSetting = new TextSetting();
            textSetting.setDoubleWidth(SettingEnum.Enable);//TextSetting Double Width Enable
            textSetting.setDoubleHeight(SettingEnum.Enable);//TextSetting Double Height Enable
            commonSetting.setPinLineSpaceEnum(PinLineSpaceEnum.ONE_SIXTH_INCH, 0);//  六分之一英寸 line space:1/6
            cmd.append(cmd.getCommonSettingCmd(commonSetting));
            cmd.append(cmd.getTextCmd(textSetting, "132456789123456789"));
            cmd.append(cmd.getLFCRCmd());
            cmd.append(cmd.getTextCmd(textSetting, "132456789123456789"));
            cmd.append(cmd.getLFCRCmd());
            cmd.append(cmd.getTextCmd(textSetting, "132456789123456789"));
            cmd.append(cmd.getLFCRCmd());

            commonSetting.setPinLineSpaceEnum(PinLineSpaceEnum.N_180_INCH, 1);// 180分之1英寸   line space:1/180 inch
            cmd.append(cmd.getCommonSettingCmd(commonSetting));
            cmd.append(cmd.getTextCmd(textSetting, "132456789123456789"));
            cmd.append(cmd.getLFCRCmd());
            cmd.append(cmd.getTextCmd(textSetting, "132456789123456789"));
            cmd.append(cmd.getLFCRCmd());
            cmd.append(cmd.getTextCmd(textSetting, "132456789123456789"));
            cmd.append(cmd.getLFCRCmd());

            cmd.append(cmd.getEndCmd());

            rtPrinter.writeMsgAsync(cmd.getAppendCmds());//send to printer
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_txtprint:
                try {
                    textPrint();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_select_chartsetname:
                showSelectChartsetnameDialog();
                break;
            case R.id.btn_font_select:
                showChooseFontDialog();
                break;
            default:
                break;
        }
    }

}
