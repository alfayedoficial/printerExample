package com.printer.example.activity;

import android.os.Bundle;
import androidx.annotation.IdRes;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.BaseEnum;
import com.rt.printerlibrary.bean.LableSizeBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.cmd.ZplFactory;
import com.rt.printerlibrary.enumerate.BarcodeStringPosition;
import com.rt.printerlibrary.enumerate.BarcodeType;
import com.rt.printerlibrary.enumerate.ESCBarcodeFontTypeEnum;
import com.rt.printerlibrary.enumerate.PrintDirection;
import com.rt.printerlibrary.enumerate.PrintRotation;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BarcodeSetting;
import com.rt.printerlibrary.setting.CommonSetting;

public class BarcodePrintActivity extends BaseActivity implements View.OnClickListener {

    public static final String BUNDLE_KEY_BARCODE_TYPE = "barcodeType";
    private View back;
    private TextView tv_barcodetype, tv_error_tip;
    private EditText et_barcode_content;
    private RadioGroup rg_print_barcode_orientation;
    private Button btn_print;
    private CheckBox ck_show_text;

    private RTPrinter rtPrinter;
    private Bundle mBundle;
    private BarcodeType barcodeType;
    @BaseEnum.CmdType
    private int curCmdType;
    private String barcodeContent;
    private PrintRotation printRotation = PrintRotation.Rotate0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_print);
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {
        back = findViewById(R.id.back);
        tv_barcodetype = findViewById(R.id.tv_barcodetype);
        et_barcode_content = findViewById(R.id.et_barcode_content);
        rg_print_barcode_orientation = findViewById(R.id.rg_print_barcode_orientation);
        btn_print = findViewById(R.id.btn_print);
        tv_error_tip = findViewById(R.id.tv_error_tip);
        ck_show_text = findViewById(R.id.ck_show_text);
    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
        curCmdType = BaseApplication.getInstance().getCurrentCmdType();
        mBundle = getIntent().getExtras();
        barcodeType = Enum.valueOf(BarcodeType.class, mBundle.getString(BUNDLE_KEY_BARCODE_TYPE));
        tv_barcodetype.setText(barcodeType.name());
        initBarcodeCheck();

        if (curCmdType == BaseEnum.CMD_ESC) {
            rg_print_barcode_orientation.setVisibility(View.GONE);
        }
        if (curCmdType == BaseEnum.CMD_CPCL) {
            ck_show_text.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void addListener() {
        back.setOnClickListener(this);
        btn_print.setOnClickListener(this);
        rg_print_barcode_orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.rb_print_barcode_orientation_left:
                        printRotation = PrintRotation.Rotate270;
                        break;
                    case R.id.rb_print_barcode_orientation_normal:
                        printRotation = PrintRotation.Rotate0;
                        break;
                    case R.id.rb_print_barcode_orientation_right:
                        printRotation = PrintRotation.Rotate90;
                        break;
                    default:
                        printRotation = PrintRotation.Rotate0;
                        break;
                }
            }
        });
    }


    private void initBarcodeCheck() {
        String inputTip = null;
        switch (barcodeType) {
            case UPC_A:
                inputTip = getString(R.string.tip_barcode_text_UPC_A);
                et_barcode_content.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String regex = "[\\D]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, new InputFilter.LengthFilter(11)});
                et_barcode_content.setText("12345678901");
                break;
            case UPC_E:
    /*                inputTip = getString(R.string.tip_barcode_text_UPC_E);
                    etInput.setInputType(InputType.TYPE_CLASS_NUMBER);*/
                break;
            case EAN13:
                inputTip = getString(R.string.tip_barcode_text_EAN13);
                et_barcode_content.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[\\D]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, new InputFilter.LengthFilter(12)});
                et_barcode_content.setText("123456789012");
                break;
            case EAN8:
                inputTip = getString(R.string.tip_barcode_text_EAN8);
                et_barcode_content.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[\\D]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, new InputFilter.LengthFilter(7)});
                et_barcode_content.setText("1234567");
                break;
            case CODE39:
                inputTip = getString(R.string.tip_barcode_text_CODE39);
                et_barcode_content.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[^a-zA-Z\\p{Digit} \\$%\\+\\-\\./]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, new InputFilter.AllCaps(), new InputFilter.LengthFilter(30)});
                et_barcode_content.setText("123456789");
                break;
            case ITF:
                et_barcode_content.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                InputFilter.LengthFilter lengthFilter = null;
                if (curCmdType == BaseEnum.CMD_ESC) {
                    inputTip = getString(R.string.tip_barcode_text_ITF);
                    lengthFilter = new InputFilter.LengthFilter(30);
                } else {
                    inputTip = getString(R.string.tip_barcode_text_ITF14);
                    lengthFilter = new InputFilter.LengthFilter(14);
                }

                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[\\D]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, lengthFilter});
                et_barcode_content.setText("12345678901234");
                break;
            case CODABAR:
                inputTip = getString(R.string.tip_barcode_text_CODABAR);
                et_barcode_content.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[^0-9a-dA-D\\$\\+\\-\\./:]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, new InputFilter.AllCaps(), new InputFilter.LengthFilter(30)});
                et_barcode_content.setText("A123456789B");
                break;
            case CODE93:
                 /* inputTip = getString(R.string.tip_barcode_text_CODE93);
                    etInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
	                etInput.setFilters(new InputFilter[]{new InputFilter() {

	                    @Override
	                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

	                        String regex = "[^1-9a-dA-D \\$\\+\\-\\./:]";
	                        String s = source.toString().replaceAll(regex,"");
	                        return s;
	                    }
	                }, new InputFilter.AllCaps()});*/
                break;
            case CODE128:
                inputTip = getString(R.string.tip_barcode_text_CODE128);
                et_barcode_content.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[^\\p{ASCII}]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }, new InputFilter.AllCaps(), new InputFilter.LengthFilter(42)});
                et_barcode_content.setText("code128");
                break;
            case QR_CODE:
                inputTip = getString(R.string.tip_barcode_text_QR_CODE);
                et_barcode_content.setRawInputType(InputType.TYPE_CLASS_TEXT);
                et_barcode_content.setFilters(new InputFilter[]{new InputFilter() {

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                        String regex = "[^\\p{ASCII}]";
                        String s = source.toString().replaceAll(regex, "");
                        return s;
                    }
                }});
                et_barcode_content.setText("QRCode");
                break;
        }

        if (!TextUtils.isEmpty(inputTip)) {
            tv_error_tip.setText(inputTip);
            tv_error_tip.setVisibility(View.VISIBLE);
        } else {
            tv_error_tip.setVisibility(View.GONE);
        }
    }

    private void print() throws SdkException {
        barcodeContent = et_barcode_content.getText().toString();
        if (TextUtils.isEmpty(barcodeContent)) {
            showToast("Barcode data is empty");
        }

        if (rtPrinter == null) {
            showToast(getString(R.string.tip_pls_connect_device));
            return;
        }


        switch (curCmdType) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            case BaseEnum.CMD_TSC:
                tscPrint();
                break;
            case BaseEnum.CMD_CPCL:
                cpclPrint();
                break;
            case BaseEnum.CMD_ZPL:
                zplPrint();
                break;
            default:
                break;
        }
    }

    private void tscPrint() throws SdkException {
        int labelWidth = 80;
        int labelHeight = 40;

        CmdFactory tscFac = new TscFactory();
        Cmd tscCmd = tscFac.create();

        tscCmd.append(tscCmd.getHeaderCmd());
        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLableSizeBean(new LableSizeBean(labelWidth, labelHeight));
        commonSetting.setLabelGap(3);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        tscCmd.append(tscCmd.getCommonSettingCmd(commonSetting));
        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setNarrowInDot(2);//narrow bar setting, bar width
        barcodeSetting.setWideInDot(4);
        barcodeSetting.setHeightInDot(48);//bar height setting
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPrintRotation(printRotation);
        int x = 30, y = 80;
        switch (printRotation) {
            case Rotate0:
                x = 30;
                y = 80;
                break;
            case Rotate90:
                x = (labelWidth * 8) / 2;
                y = 20;
                break;
            case Rotate270:
                x = (labelWidth * 8) / 2;
                y = (labelHeight * 8) - 20;
                break;
            default:
                break;
        }
        barcodeSetting.setPosition(new Position(x, y));
        byte[] barcodeCmd = tscCmd.getBarcodeCmd(barcodeType, barcodeSetting, barcodeContent);
        tscCmd.append(barcodeCmd);

        tscCmd.append(tscCmd.getPrintCopies(1));
        tscCmd.append(tscCmd.getEndCmd());
        if (rtPrinter != null) {
            rtPrinter.writeMsgAsync(tscCmd.getAppendCmds());
        }
    }

    private void zplPrint() throws SdkException {
        int labelWidth = 80;
        int labelHeight = 40;

        CmdFactory zplFac = new ZplFactory();
        Cmd zplCmd = zplFac.create();

        zplCmd.append(zplCmd.getHeaderCmd());
        CommonSetting commonSetting = new CommonSetting();
        commonSetting.setLableSizeBean(new LableSizeBean(labelWidth, labelHeight));
        commonSetting.setLabelGap(2);
        commonSetting.setPrintDirection(PrintDirection.NORMAL);
        zplCmd.append(zplCmd.getCommonSettingCmd(commonSetting));
        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setHeightInDot(48);
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setPrintRotation(printRotation);
        int x = 10, y = 80;
        switch (printRotation) {
            case Rotate0:
                x = 10;
                y = 10;
                break;
            case Rotate90:
                x = (labelWidth * 8) / 2;
                y = 20;
                break;
            case Rotate270:
                x = (labelWidth * 8) / 2;
                y = (labelHeight * 8) - 20;
                break;
            default:
                break;
        }
        barcodeSetting.setPosition(new Position(x, y));
        byte[] barcodeCmd = zplCmd.getBarcodeCmd(barcodeType, barcodeSetting, barcodeContent);
        zplCmd.append(barcodeCmd);

        zplCmd.append(zplCmd.getPrintCopies(1));
        zplCmd.append(zplCmd.getEndCmd());
        if (rtPrinter != null) {
            rtPrinter.writeMsgAsync(zplCmd.getAppendCmds());
        }
    }

    private void cpclPrint() throws SdkException {
        CmdFactory cpclFac = new CpclFactory();
        Cmd cmd = cpclFac.create();
        cmd.append(cmd.getCpclHeaderCmd(Integer.parseInt(BaseApplication.labelWidth), Integer.parseInt(BaseApplication.labelHeight), 1, Integer.parseInt(BaseApplication.labelOffset)));
        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.NONE);
        barcodeSetting.setPrintRotation(printRotation);
        barcodeSetting.setNarrowInDot(2);//narrow bar width
        barcodeSetting.setBarcodeStringPosition(ck_show_text.isChecked() ? BarcodeStringPosition.BELOW_BARCODE : BarcodeStringPosition.NONE);
        if (printRotation == PrintRotation.Rotate0) {
            barcodeSetting.setPosition(new Position(10, 20));//bar height setting
        } else {
            barcodeSetting.setPosition(new Position(300, 300));//bar height setting
        }
        barcodeSetting.setHeightInDot(48);
        byte[] barcodeCmd = cmd.getBarcodeCmd(barcodeType, barcodeSetting, barcodeContent);
        cmd.append(barcodeCmd);


        cmd.append(cmd.getEndCmd());
        if (rtPrinter != null) {
            rtPrinter.writeMsgAsync(cmd.getAppendCmds());
        }
    }

    private void escPrint() throws SdkException {
        CmdFactory cmdFactory = new EscFactory();
        Cmd escCmd = cmdFactory.create();
        escCmd.append(escCmd.getHeaderCmd());


        BarcodeSetting barcodeSetting = new BarcodeSetting();
        barcodeSetting.setBarcodeStringPosition(BarcodeStringPosition.BELOW_BARCODE);
        barcodeSetting.setHeightInDot(72);//accept value:1~255
        barcodeSetting.setBarcodeWidth(3);//accept value:2~6
        barcodeSetting.setQrcodeDotSize(5);//accept value: Esc(1~15), Tsc(1~10)
        barcodeSetting.setEscBarcodFont(ESCBarcodeFontTypeEnum.BARFONT_B_9x17);
        try {
            escCmd.append(escCmd.getBarcodeCmd(barcodeType, barcodeSetting, barcodeContent));
        } catch (SdkException e) {
            e.printStackTrace();
        }
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());
        escCmd.append(escCmd.getLFCRCmd());

        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.btn_print:
                try {
                    print();
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }


}
