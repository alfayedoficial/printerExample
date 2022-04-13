//package com.printer.example.activity;
//
//import android.Manifest;
//import android.app.AlertDialog;
//import android.app.PendingIntent;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.hardware.usb.UsbDevice;
//import android.hardware.usb.UsbManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//
//import androidx.annotation.IdRes;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//
//import com.printer.example.R;
//import com.printer.example.app.BaseActivity;
//import com.printer.example.app.BaseApplication;
//import com.printer.example.dialog.BluetoothDeviceChooseDialog;
//import com.printer.example.dialog.UsbDeviceChooseDialog;
//import com.printer.example.utils.BaseEnum;
//import com.printer.example.utils.SPUtils;
//import com.printer.example.utils.TempletDemo;
//import com.printer.example.utils.TimeRecordUtils;
//import com.printer.example.utils.ToastUtil;
//import com.printer.example.utils.TonyUtils;
//import com.printer.example.view.FlowRadioGroup;
//import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
//import com.rt.printerlibrary.bean.PrinterStatusBean;
//import com.rt.printerlibrary.bean.UsbConfigBean;
//import com.rt.printerlibrary.bean.WiFiConfigBean;
//import com.rt.printerlibrary.cmd.Cmd;
//import com.rt.printerlibrary.cmd.CpclFactory;
//import com.rt.printerlibrary.cmd.EscCmd;
//import com.rt.printerlibrary.cmd.EscFactory;
//import com.rt.printerlibrary.cmd.PinFactory;
//import com.rt.printerlibrary.cmd.TscCmd;
//import com.rt.printerlibrary.cmd.TscFactory;
//import com.rt.printerlibrary.cmd.ZplFactory;
//import com.rt.printerlibrary.connect.PrinterInterface;
//import com.rt.printerlibrary.enumerate.CommonEnum;
//import com.rt.printerlibrary.enumerate.ConnectStateEnum;
//import com.rt.printerlibrary.enumerate.PrinterAskStatusEnum;
//import com.rt.printerlibrary.exception.SdkException;
//import com.rt.printerlibrary.factory.cmd.CmdFactory;
//import com.rt.printerlibrary.factory.connect.BluetoothFactory;
//import com.rt.printerlibrary.factory.connect.PIFactory;
//import com.rt.printerlibrary.factory.connect.UsbFactory;
//import com.rt.printerlibrary.factory.connect.WiFiFactory;
//import com.rt.printerlibrary.factory.printer.LabelPrinterFactory;
//import com.rt.printerlibrary.factory.printer.PinPrinterFactory;
//import com.rt.printerlibrary.factory.printer.PrinterFactory;
//import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
//import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
//import com.rt.printerlibrary.observer.PrinterObserver;
//import com.rt.printerlibrary.observer.PrinterObserverManager;
//import com.rt.printerlibrary.printer.RTPrinter;
//import com.rt.printerlibrary.utils.ConnectListener;
//import com.rt.printerlibrary.utils.FuncUtils;
//import com.rt.printerlibrary.utils.PrintListener;
//import com.rt.printerlibrary.utils.PrintStatusCmd;
//import com.rt.printerlibrary.utils.PrinterStatusPareseUtils;
//
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MainActivity extends BaseActivity implements View.OnClickListener, PrinterObserver {
//
//
//    private String[] NEED_PERMISSION = {
//            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//    private List<String> NO_PERMISSION = new ArrayList<String>();
//    private static final int REQUEST_CAMERA = 0;
//
//    private TextView tv_ver;
//    private RadioGroup rg_cmdtype;
//    private FlowRadioGroup rg_connect;
//    private Button btn_selftest_print, btn_txt_print, btn_img_print, btn_template_print, btn_barcode_print,
//            btn_beep, btn_all_cut, btn_cash_box, btn_wifi_setting, btn_wifi_ipdhcp, btn_cmd_test, btn_label_setting,
//            btn_print_status,btn_print_status2;
//    private Button btn_disConnect, btn_connect;
//    private TextView tv_device_selected;
//    private Button btn_connected_list;
//    private ProgressBar pb_connect;
//    private Button btn_test;
//
//    @BaseEnum.ConnectType
//    private int checkedConType = BaseEnum.CON_WIFI;
//    private RTPrinter rtPrinter = null;
//    private PrinterFactory printerFactory;
//    private final String SP_KEY_IP = "ip";
//    private final String SP_KEY_PORT = "port";
//    private Object configObj;
//    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
//    private PrinterInterface curPrinterInterface = null;
//    private BroadcastReceiver broadcastReceiver;//USB Attach-Deattached Receiver
//    private BluetoothAdapter mBluetoothAdapter;
//    private int iprintTimes=0;
//
//    private void CheckAllPermission() {
//        NO_PERMISSION.clear();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            for (int i = 0; i < NEED_PERMISSION.length; i++) {
//                if (checkSelfPermission(NEED_PERMISSION[i]) != PackageManager.PERMISSION_GRANTED) {
//                    NO_PERMISSION.add(NEED_PERMISSION[i]);
//                }
//            }
//            if (NO_PERMISSION.size() == 0) {
//                recordVideo();
//            } else {
//                requestPermissions(NO_PERMISSION.toArray(new String[NO_PERMISSION.size()]), REQUEST_CAMERA);
//            }
//        } else {
//            recordVideo();
//        }
//
//    }
//
//    private Handler handler=new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case 11:
//                    connectBluetoothByMac();
//            }
//        }
//    };
//
//
//    private void recordVideo() {
//        Log.d("MainActivity", "有权限了");
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        CheckAllPermission();
//        setContentView(R.layout.activity_main);
//        initView();
//        init();
//        addListener();
//       // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    }
//
//    public void initView() {
//        tv_ver = findViewById(R.id.tv_ver);
//        rg_cmdtype = findViewById(R.id.rg_cmdtype);
//        rg_connect = findViewById(R.id.rg_connect);
//        btn_selftest_print = findViewById(R.id.btn_selftest_print);
//        btn_txt_print = findViewById(R.id.btn_txt_print);
//        btn_img_print = findViewById(R.id.btn_img_print);
//        btn_connect = findViewById(R.id.btn_connect);
//        btn_disConnect = findViewById(R.id.btn_disConnect);
//        tv_device_selected = findViewById(R.id.tv_device_selected);
//        btn_template_print = findViewById(R.id.btn_template_print);
//        btn_barcode_print = findViewById(R.id.btn_barcode_print);
//        btn_connected_list = findViewById(R.id.btn_connected_list);
//        btn_cash_box = findViewById(R.id.btn_cash_box);
//        btn_all_cut = findViewById(R.id.btn_all_cut);
//        btn_beep = findViewById(R.id.btn_beep);
//        btn_wifi_setting = findViewById(R.id.btn_wifi_setting);
//        btn_wifi_ipdhcp = findViewById(R.id.btn_wifi_ipdhcp);
//        btn_cmd_test = findViewById(R.id.btn_cmd_test);
//        pb_connect = findViewById(R.id.pb_connect);
//        btn_test = findViewById(R.id.btn_test);
//        btn_label_setting = findViewById(R.id.btn_label_setting);
//        btn_print_status = findViewById(R.id.btn_print_status);
//        btn_print_status2 = findViewById(R.id.btn_print_status2);
//
//    }
//
//    public void init() {
//        //初始化为针打printer
//        btn_print_status2.setVisibility(View.GONE);
//        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ESC);
//        printerFactory = new UniversalPrinterFactory();
//        rtPrinter = printerFactory.create();
//
//        tv_ver.setText("PrinterExample Ver: v" + TonyUtils.getVersionName(this));
//        PrinterObserverManager.getInstance().add(this);//添加连接状态监听
//
//        if (BaseApplication.getInstance().getCurrentCmdType() == BaseEnum.CMD_PIN) {
//            btn_barcode_print.setVisibility(View.GONE);
//        } else {
//            btn_barcode_print.setVisibility(View.VISIBLE);
//        }
//
//        btn_label_setting.setEnabled(true);//TODO
//
//    }
//
//    public void addListener() {
//
//        btn_selftest_print.setOnClickListener(this);
//        btn_txt_print.setOnClickListener(this);
//        btn_img_print.setOnClickListener(this);
//        btn_connect.setOnClickListener(this);
//        btn_disConnect.setOnClickListener(this);
//        btn_template_print.setOnClickListener(this);
//        tv_device_selected.setOnClickListener(this);
//        btn_barcode_print.setOnClickListener(this);
//        btn_connected_list.setOnClickListener(this);
//        btn_cash_box.setOnClickListener(this);
//        btn_all_cut.setOnClickListener(this);
//        btn_beep.setOnClickListener(this);
//        btn_wifi_setting.setOnClickListener(this);
//        btn_wifi_ipdhcp.setOnClickListener(this);
//        btn_cmd_test.setOnClickListener(this);
//        btn_test.setOnClickListener(this);
//        btn_label_setting.setOnClickListener(this);
//        btn_print_status.setOnClickListener(this);
//        btn_print_status2.setOnClickListener(this);
//
//
//        radioButtonCheckListener();//single button listener
//
//        rg_cmdtype.check(R.id.rb_cmd_esc);
//        rg_connect.check(R.id.rb_connect_bluetooth);
//    }
//
//    private void radioButtonCheckListener() {
//        rg_cmdtype.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
//                switch (i) {
//                    case R.id.rb_cmd_pin://针打
//                        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_PIN);
//                        printerFactory = new PinPrinterFactory();
//
//                        rtPrinter = printerFactory.create();
//                        rtPrinter.setPrinterInterface(curPrinterInterface);
//                        btn_barcode_print.setVisibility(View.GONE);
//                        btn_label_setting.setVisibility(View.GONE);
//                        break;
//                    case R.id.rb_cmd_esc://esc
//                        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ESC);
//                        printerFactory = new ThermalPrinterFactory();
//                        rtPrinter = printerFactory.create();
//                        rtPrinter.setPrinterInterface(curPrinterInterface);
//                        btn_barcode_print.setVisibility(View.VISIBLE);
//                        btn_label_setting.setVisibility(View.GONE);
//                        break;
//                    case R.id.rb_cmd_tsc://tsc
//                        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_TSC);
//                        printerFactory = new LabelPrinterFactory();
//                        rtPrinter = printerFactory.create();
//                        rtPrinter.setPrinterInterface(curPrinterInterface);
//                        btn_barcode_print.setVisibility(View.VISIBLE);
//                        btn_label_setting.setVisibility(View.GONE);
//                        break;
//                    case R.id.rb_cmd_cpcl://cpcl
//                        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_CPCL);
//                        printerFactory = new LabelPrinterFactory();
//                        rtPrinter = printerFactory.create();
//                        rtPrinter.setPrinterInterface(curPrinterInterface);
//                        btn_barcode_print.setVisibility(View.VISIBLE);
//                        btn_label_setting.setVisibility(View.VISIBLE);
//                        break;
//                    case R.id.rb_cmd_zpl://zpl
//                        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ZPL);
//                        printerFactory = new LabelPrinterFactory();
//                        rtPrinter = printerFactory.create();
//                        rtPrinter.setPrinterInterface(curPrinterInterface);
//                        btn_barcode_print.setVisibility(View.VISIBLE);
//                        btn_label_setting.setVisibility(View.GONE);
//                        break;
//                }
//                BaseApplication.getInstance().setRtPrinter(rtPrinter);
//            }
//        });
//
//        rg_connect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
//                doDisConnect();//有切换的话就断开
//                switch (i) {
//                    case R.id.rb_connect_wifi://WiFi
//                        checkedConType = BaseEnum.CON_WIFI;
//                        break;
//                    case R.id.rb_connect_bluetooth://bluetooth
//                        checkedConType = BaseEnum.CON_BLUETOOTH;
//                        break;
////                    case R.id.rb_connect_bluetooth_ble://bluetooth_ble
//////                        checkedConType = BaseEnum.CON_BLUETOOTH_BLE;
////                        break;
//                    case R.id.rb_connect_usb://usb
//                        checkedConType = BaseEnum.CON_USB;
//                        break;
//                    case R.id.rb_connect_com://串口-AP02
//                        checkedConType = BaseEnum.CON_COM;
//                        break;
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.btn_selftest_print:
//                selfTestPrint();
//                break;
//            case R.id.btn_txt_print:
//                try {
//                    textPrint();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case R.id.btn_img_print:
//                imagePrint();
//                break;
//            case R.id.btn_disConnect:
//                doDisConnect();
//                break;
//            case R.id.btn_connect:
//                doConnect();
//                break;
//            case R.id.btn_template_print:
//                toTemplateActivity();
//                break;
//            case R.id.tv_device_selected:
//                showConnectDialog();
//                break;
//            case R.id.btn_barcode_print://条码打印
//                turn2Activity(BarcodeActivity.class);
//                break;
//            case R.id.btn_connected_list://显示多连接
//                showConnectedListDialog();
//                break;
//            case R.id.btn_beep://蜂鸣测试
//                beepTest();
//                break;
//            case R.id.btn_all_cut://切刀测试-全切
//                allCutTest();
//                break;
//            case R.id.btn_cash_box://钱箱测试
//                cashboxTest();
//                break;
//            case R.id.btn_wifi_setting://WiFi设置
//                turn2Activity(WifiSettingActivity.class);
//                break;
//            case R.id.btn_wifi_ipdhcp://IP/DHCP设置
//                turn2Activity(WifiIpDhcpSettingActivity.class);
//                break;
//            case R.id.btn_cmd_test:
//                turn2Activity(CmdTestActivity.class);
//                break;
//            case R.id.btn_test:
//                testTsc();
//                break;
//            case R.id.btn_label_setting:
//                toLabelSettingActivity();
//                break;
//            case R.id.btn_print_status:
//                getPrintStauts();
//                break;
//            case R.id.btn_print_status2:
//             //   getPrintStauts2();
//                break;
//            default:
//                break;
//        }
//    }
//
//
//
//    private void getPrintStauts()  {
//        switch (BaseApplication.getInstance().getCurrentCmdType()) {
//            case BaseEnum.CMD_TSC: { //For RPP320 customization
//                TscFactory cmdFactory = new TscFactory();
//                TscCmd cmd = cmdFactory.create();
//                cmd.append( cmd.getPrintStausCmd(PrintStatusCmd.cmd_Normal));
//                rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                rtPrinter.setPrintListener(new PrintListener() {
//                    @Override
//                    public void onPrinterStatus(PrinterStatusBean StatusBean) {
//                        ToastUtil.show(MainActivity.this,"print status："+PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
//                    }
//                });
//                break;
//            }
//
//            case BaseEnum.CMD_ESC:
//                if (BaseApplication.getInstance().getCurrentConnectType() == BaseEnum.CON_USB) {
//                    EscFactory cmdFactory = new EscFactory();
//                    EscCmd cmd = cmdFactory.create();
//                    cmd.append( cmd.getPrinterStatus(PrinterAskStatusEnum.Paper_status));
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                    byte[] msgbytes = rtPrinter.readMsg();
//                    if (msgbytes.length>0)
//                        ToastUtil.show(this, String.valueOf(msgbytes[0]));
//                    else
//                        ToastUtil.show(this, "fail");
//                }
//                break;
//        }
//
//
//
//
//    }
//
//
//    private CmdFactory getCmdFactory(@BaseEnum.CmdType int baseEnum) {
//        CmdFactory cmdFactory = null;
//        switch (baseEnum) {
//            case BaseEnum.CMD_PIN:
//                cmdFactory = new PinFactory();
//                break;
//            case BaseEnum.CMD_ESC:
//                cmdFactory = new EscFactory();
//                break;
//            case BaseEnum.CMD_TSC:
//                cmdFactory = new TscFactory();
//                break;
//            case BaseEnum.CMD_CPCL:
//                cmdFactory = new CpclFactory();
//                break;
//            case BaseEnum.CMD_ZPL:
//                cmdFactory = new ZplFactory();
//                break;
//            default:
//                break;
//        }
//        return cmdFactory;
//    }
////    private void getPrintStauts2()  {
////        turn2Activity(StatusTestActivity.class);
////    }
////        CmdFactory cmdFactory = getCmdFactory(BaseApplication.getInstance().getCurrentCmdType());
////        Cmd cmd = cmdFactory.create();
////        cmd.append( cmd.getPrintStausCmd(PrintStatusCmd.cmd_Normal));
////        byte[] bts = rtPrinter.getprinterStausMsg(cmd.getAppendCmds());
////        if (bts !=null && bts.length>0) {
////               PrinterStatusBean StatusBean = PrinterStatusPareseUtils.parsePrinterStatusResult(bts);
////               ToastUtil.show(MainActivity.this, "print status：" + PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
////          }
////   }
//
//    /**
//     * turn to Label Setting[CPCL]
//     */
//    private void toLabelSettingActivity() {
//        turn2Activity(LabelSettingActivity.class);
//    }
//
//    private void testTsc() {
//        if (rtPrinter == null) {
//            return;
//        }
//        TonyUtils.Tsc_InitLabelPrint(rtPrinter);
//        String strPrintTxt = TonyUtils.printText("80", "80", "TSS24.BF2", "0", "1", "1", "Hello,容大!");
//        String strPrint = TonyUtils.setPRINT("1", "1");
//
//        try {
//            rtPrinter.writeMsg(strPrintTxt.getBytes("GBK"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        rtPrinter.writeMsg(strPrint.getBytes());
//    }
//
//    private void initBroadcast() {
//        broadcastReceiver = new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // TODO Auto-generated method stub
//                String action = intent.getAction();
//                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                    //   ToastUtil.show(context,"接收到断开信息");
//                    if (BaseApplication.getInstance().getCurrentConnectType() == BaseEnum.CON_USB) {
//                        doDisConnect();//断开USB连接， Disconnect USB connection.
//                    }
//                }
//                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                    //    ToastUtil.show(context,"插入USB");
//                }
//            }
//
//        };
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(broadcastReceiver, intentFilter);
//    }
//
//    /**
//     * 蜂鸣测试
//     */
//    private void beepTest() {
//        switch (BaseApplication.getInstance().getCurrentCmdType()) {
//            case BaseEnum.CMD_ESC:
//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getBeepCmd());
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }
//                break;
//            default:
//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getBeepCmd());
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }
//                break;
//        }
//    }
//
//    /**
//     * 全切测试
//     */
//    private void allCutTest() {
//        switch (BaseApplication.getInstance().getCurrentCmdType()) {
//            case BaseEnum.CMD_ESC:
//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getAllCutCmd());
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }
//                break;
////            case BaseEnum.CMD_PIN:
////                if(rtPrinter != null){
////                    CmdFactory cmdFactory = new PinFactory();
////                    Cmd cmd = cmdFactory.create();
////                    CommonSetting commonSetting = new CommonSetting();
////                    commonSetting.setPageLengthEnum(PageLengthEnum.INCH_5_5);
////                    cmd.append(cmd.getCommonSettingCmd(commonSetting));
////                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
////                }
////                break;
//            default:
//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getAllCutCmd());
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }
//                break;
//        }
//    }
//
//    /**
//     * 钱箱测试
//     */
//    private void cashboxTest() {
//        switch (BaseApplication.getInstance().getCurrentCmdType()) {
//            case BaseEnum.CMD_ESC:
//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getOpenMoneyBoxCmd());//Open cashbox use default setting[0x00,0x20,0x01]
//
//                    //or custom settings
////                    byte drawNumber = 0x00;
////                    byte startTime = 0x05;
////                    byte endTime = 0x00;
////                    cmd.append(cmd.getOpenMoneyBoxCmd(drawNumber, startTime, endTime));
//                    Log.e("Fuuu", FuncUtils.ByteArrToHex(cmd.getAppendCmds()));
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }
//                break;
//            default://TSC, CPCL, ZPL
//                if (rtPrinter != null) {
//                    CmdFactory cmdFactory = new EscFactory();
//                    Cmd cmd = cmdFactory.create();
//                    cmd.append(cmd.getOpenMoneyBoxCmd());//Open cashbox
//                    rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//                }
//                break;
//        }
//    }
//
//    /**
//     * 显示已连接设备窗口
//     */
//    private void showConnectedListDialog() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle(R.string.dialog_title_connected_devlist);
//        String[] devList = new String[printerInterfaceArrayList.size()];
//        for (int i = 0; i < devList.length; i++) {
//            devList[i] = printerInterfaceArrayList.get(i).getConfigObject().toString();
//        }
//        if (devList.length > 0) {
//            dialog.setItems(devList, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    PrinterInterface printerInter = printerInterfaceArrayList.get(i);
//                    tv_device_selected.setText(printerInter.getConfigObject().toString());
//                    rtPrinter.setPrinterInterface(printerInter);//设置连接方式 Connection port settings
//                    tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
//                    curPrinterInterface = printerInter;
//                  //  BaseApplication.getInstance().setRtPrinter(rtPrinter);//设置全局RTPrinter
//                    if (printerInter.getConnectState() == ConnectStateEnum.Connected) {
//                        setPrintEnable(true);
//                    } else {
//                        setPrintEnable(false);
//                    }
//                }
//            });
//        } else {
//            dialog.setMessage(R.string.pls_connect_printer_first);
//        }
//        dialog.setNegativeButton(R.string.dialog_cancel, null);
//        dialog.show();
//    }
//
//    private void doConnect() {
//        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) {//未选择设备
//            showAlertDialog(getString(R.string.main_pls_choose_device));
//            return;
//        }
//        pb_connect.setVisibility(View.VISIBLE);
//        switch (checkedConType) {
//            case BaseEnum.CON_WIFI:
//                WiFiConfigBean wiFiConfigBean = (WiFiConfigBean) configObj;
//                connectWifi(wiFiConfigBean);
//                break;
//            case BaseEnum.CON_BLUETOOTH:
//                TimeRecordUtils.record("RT连接start：", System.currentTimeMillis());
//                BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
//                iprintTimes = 0;
//                connectBluetooth(bluetoothEdrConfigBean);
////              connectBluetoothByMac();
//
//                break;
//            case BaseEnum.CON_USB:
//                UsbConfigBean usbConfigBean = (UsbConfigBean) configObj;
//                connectUSB(usbConfigBean);
//                break;
//            default:
//                pb_connect.setVisibility(View.GONE);
//                break;
//        }
//
//    }
//
//    private void connectBluetoothByMac() {
//        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("DC:0D:30:00:1F:79");//直接连接蓝牙mac 地址 00:00:0E:01:D0:32
//        BluetoothEdrConfigBean bluetoothEdrConfigBean = new BluetoothEdrConfigBean(device);
//        PIFactory piFactory = new BluetoothFactory();
//        PrinterInterface printerInterface = piFactory.create();
//        printerInterface.setConfigObject(bluetoothEdrConfigBean);
//        rtPrinter.setPrinterInterface(printerInterface);
//        rtPrinter.setConnectListener(new ConnectListener() { //必须在connect之后，设置监听
//                @Override
//                public void onPrinterConnected(Object configObj) {
//                    try {
//                        TempletDemo.getInstance (rtPrinter,MainActivity.this).escTemplet();
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    } catch (SdkException e) {
//                        e.printStackTrace();
//                    }
//                    iprintTimes++;
//                }
//
//                @Override
//                public void onPrinterDisconnect(Object configObj) {
//                if (iprintTimes<5){
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Message message=new Message();
//                            message.what=11;
//                            handler.sendMessage(message);
//                        }
//                    }).start();
//
//                }
//            }
//
//            @Override
//            public void onPrinterWritecompletion(Object configObj) { //写入完成时,断开,要改为收到打印完成状态
//                try {
//                    Thread.sleep(500);//根据打印内容的长短，来设置 Set according to the length of the printed content
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                rtPrinter.disConnect();
//            }
//        });
//        try {
//            rtPrinter.connect(bluetoothEdrConfigBean);
////            rtPrinter.setPrintListener(new PrintListener() {
////                @Override
////                public void onPrinterStatus(PrinterStatusBean StatusBean) {
////                    if (StatusBean.printStatusCmd == PrintStatusCmd.cmd_PrintFinish)
////                    {
////                        rtPrinter.disConnect();
////                        if (StatusBean.blPrintSucc) {
////                           // Log.e("printok","printok");
////                        }
////                    }
////
////
////                }
////            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
//        PIFactory piFactory = new BluetoothFactory();
//        PrinterInterface printerInterface = piFactory.create();
//        printerInterface.setConfigObject(bluetoothEdrConfigBean);
//        rtPrinter.setPrinterInterface(printerInterface);
//        try {
//            rtPrinter.connect(bluetoothEdrConfigBean);
//         } catch (Exception e) {
//            e.printStackTrace();
//           // Log.e("mao",e.getMessage());
//        } finally {
//
//        }
//    }
//
//    private void connectWifi(WiFiConfigBean wiFiConfigBean) {
//        PIFactory piFactory = new WiFiFactory();
//        PrinterInterface printerInterface = piFactory.create();
//        printerInterface.setConfigObject(wiFiConfigBean);
//        rtPrinter.setPrinterInterface(printerInterface);
//        try {
//            rtPrinter.connect(wiFiConfigBean);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//    }
//
//    private void connectUSB(UsbConfigBean usbConfigBean) {
//        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        PIFactory piFactory = new UsbFactory();
//        PrinterInterface printerInterface = piFactory.create();
//        printerInterface.setConfigObject(usbConfigBean);
//        rtPrinter.setPrinterInterface(printerInterface);
//
//        if (mUsbManager.hasPermission(usbConfigBean.usbDevice)) {
//            try {
//                rtPrinter.connect(usbConfigBean);
//                BaseApplication.instance.setRtPrinter(rtPrinter);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            mUsbManager.requestPermission(usbConfigBean.usbDevice, usbConfigBean.pendingIntent);
//        }
//
//    }
//
//    private void toTemplateActivity() {
//        turn2Activity(TempletPrintActivity.class);
//    }
//
//    private void DisconnectByATDISC() {//断开指令，定制客户测试用
//        CmdFactory cmdFactory = new EscFactory();
//        Cmd cmd = cmdFactory.create();
//        String stcCmds = "AT+DISC\r\n";
//        byte[] bytes = stcCmds.getBytes();
//        cmd.append(bytes);
//        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//    }
//
//
//    private void doDisConnect() {
//
//        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) {//未选择设备
////            showAlertDialog(getString(R.string.main_discon_click_repeatedly));
//            return;
//        }
//
//        if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
//            rtPrinter.disConnect();
//           // DisconnectByATDISC();
//        }
//        tv_device_selected.setText(getString(R.string.please_connect));
//        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
//        setPrintEnable(false);
//    }
//
//    private void showConnectDialog() {
//        switch (checkedConType) {
//            case BaseEnum.CON_WIFI:
//                showWifiChooseDialog();
//                break;
//            case BaseEnum.CON_BLUETOOTH:
//                showBluetoothDeviceChooseDialog();
//                break;
//            case BaseEnum.CON_USB:
//                showUSBDeviceChooseDialog();
//                break;
//            default:
//                break;
//        }
//    }
//
//    private void selfTestPrint() {
//
//        switch (BaseApplication.getInstance().getCurrentCmdType()) {
//            case BaseEnum.CMD_PIN:
//                pinSelftestPrint();
//                break;
//            case BaseEnum.CMD_ESC:
//                escSelftestPrint();
//                break;
//            case BaseEnum.CMD_TSC:
//                tscSelftestPrint();
//                break;
//            case BaseEnum.CMD_CPCL:
//                cpclSelftestPrint();
//                break;
//            case BaseEnum.CMD_ZPL:
//                zplSelftestPrint();
//                break;
//            default:
//                break;
//        }
//
//    }
//
//    private void cpclSelftestPrint() {
//        CmdFactory cmdFactory = new CpclFactory();
//        Cmd cmd = cmdFactory.create();
////        cmd.append(cmd.getCpclHeaderCmd(80,60,1));
//        cmd.append(cmd.getSelfTestCmd());
//        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//    }
//
//    private void zplSelftestPrint() {
//        CmdFactory cmdFactory = new ZplFactory();
//        Cmd cmd = cmdFactory.create();
//        cmd.append(cmd.getHeaderCmd());
//        cmd.append(cmd.getSelfTestCmd());
//        cmd.append(cmd.getEndCmd());
//        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//    }
//
//    private void tscSelftestPrint() {
//        CmdFactory cmdFactory = new TscFactory();
//        Cmd cmd = cmdFactory.create();
//        cmd.append(cmd.getHeaderCmd());
//        cmd.append(cmd.getLFCRCmd());
//        cmd.append(cmd.getLFCRCmd());
//        cmd.append(cmd.getSelfTestCmd());
//        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//    }
//
//    private void escSelftestPrint() {
//        CmdFactory cmdFactory = new EscFactory();
//        Cmd cmd = cmdFactory.create();
//        cmd.append(cmd.getHeaderCmd());
//        cmd.append(cmd.getLFCRCmd());
//        cmd.append(cmd.getSelfTestCmd());
//        cmd.append(cmd.getLFCRCmd());
//        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//    }
//
//    private void pinSelftestPrint() {
//        CmdFactory cmdFactory = new PinFactory();
//        Cmd cmd = cmdFactory.create();
//        cmd.append(cmd.getHeaderCmd());
//        cmd.append(cmd.getLFCRCmd());
//        cmd.append(cmd.getLFCRCmd());
//        cmd.append(cmd.getSelfTestCmd());
//        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
//    }
//
//    private void imagePrint() {
//        turn2Activity(ImagePrintActivity.class);
//    }
//
//    private void textPrint() throws UnsupportedEncodingException {
//        switch (BaseApplication.getInstance().getCurrentCmdType()) {
//            case BaseEnum.CMD_ESC:
//                turn2Activity(TextPrintESCActivity.class);
//                break;
//            default:
//                turn2Activity(TextPrintActivity.class);
//                break;
//        }
//    }
//
//    @Override
//    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                pb_connect.setVisibility(View.GONE);
//                switch (state) {
//                    case CommonEnum.CONNECT_STATE_SUCCESS:
//                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
//                        showToast(printerInterface.getConfigObject().toString() + getString(R.string._main_connected));
//                        tv_device_selected.setText(printerInterface.getConfigObject().toString());
//                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
//                        curPrinterInterface = printerInterface;//设置为当前连接， set current Printer Interface
//                        printerInterfaceArrayList.add(printerInterface);//多连接-添加到已连接列表
//                        rtPrinter.setPrinterInterface(printerInterface);
//                      //  BaseApplication.getInstance().setRtPrinter(rtPrinter);
//                        setPrintEnable(true);
//                        break;
//                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
//                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
//                            showToast(printerInterface.getConfigObject().toString() + getString(R.string._main_disconnect));
//                        } else {
//                            showToast(getString(R.string._main_disconnect));
//                        }
//                        TimeRecordUtils.record("RT连接断开：", System.currentTimeMillis());
//                        tv_device_selected.setText(R.string.please_connect);
//                        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
//                        curPrinterInterface = null;
//                        printerInterfaceArrayList.remove(printerInterface);//多连接-从已连接列表中移除
//                      //  BaseApplication.getInstance().setRtPrinter(null);
//                        setPrintEnable(false);
//
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//    }
//
//    @Override
//    public void printerReadMsgCallback(PrinterInterface printerInterface, final byte[] bytes) {
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                PrinterStatusBean StatusBean = PrinterStatusPareseUtils.parsePrinterStatusResult(bytes);
//                    if (StatusBean.printStatusCmd== PrintStatusCmd.cmd_PrintFinish){
//                    if (StatusBean.blPrintSucc){
//                        Log.e("mydebug","print ok");
//                        ToastUtil.show(MainActivity.this,"print ok");
//                    } else
//                    {
//                        ToastUtil.show(MainActivity.this,PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
//
//                    }
//
//
//                } else  if (StatusBean.printStatusCmd== PrintStatusCmd.cmd_Normal){
//                    ToastUtil.show(MainActivity.this,"print status："+PrinterStatusPareseUtils.getPrinterStatusStr(StatusBean));
//
//                }
//            }
//        });
//
//
//
//
//
//    }
//
//    /**
//     * wifi 连接信息填写
//     */
//    private void showWifiChooseDialog() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle(R.string.dialog_tip);
//
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_wifi_config, null);
//        final EditText et_wifi_ip = view.findViewById(R.id.et_wifi_ip);
//        final EditText et_wifi_port = view.findViewById(R.id.et_wifi_port);
//
//        String spIp = SPUtils.get(MainActivity.this, SP_KEY_IP, "192.168.").toString();
//        String spPort = SPUtils.get(MainActivity.this, SP_KEY_PORT, "9100").toString();
//
//        et_wifi_ip.setText(spIp);
//        et_wifi_ip.setSelection(spIp.length());
//        et_wifi_port.setText(spPort);
//
//        dialog.setView(view);
//        dialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                String ip = et_wifi_ip.getText().toString();
//                String strPort = et_wifi_port.getText().toString();
//                if (TextUtils.isEmpty(strPort)) {
//                    strPort = "9100";
//                }
//                if (!TextUtils.isEmpty(ip)) {
//                    SPUtils.put(MainActivity.this, SP_KEY_IP, ip);
//                }
//                if (!TextUtils.isEmpty(strPort)) {
//                    SPUtils.put(MainActivity.this, SP_KEY_PORT, strPort);
//                }
//                configObj = new WiFiConfigBean(ip, Integer.parseInt(strPort));
//                tv_device_selected.setText(configObj.toString());
//                tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
//                isConfigPrintEnable(configObj);
//            }
//        });
//        dialog.setNegativeButton(R.string.dialog_cancel, null);
//        dialog.show();
//
//    }
//
//    private void showBluetoothDeviceChooseDialog() {
//        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
//        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
//            @Override
//            public void onDeviceItemClick(BluetoothDevice device) {
//                if (TextUtils.isEmpty(device.getName())) {
//                    tv_device_selected.setText(device.getAddress());
//                } else {
//                    tv_device_selected.setText(device.getName() + " [" + device.getAddress() + "]");
//                }
//                configObj = new BluetoothEdrConfigBean(device);
//                tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
//                isConfigPrintEnable(configObj);
//            }
//        });
//        bluetoothDeviceChooseDialog.show(MainActivity.this.getFragmentManager(), null);
//    }
//
//    /**
//     * usb设备选择
//     */
//    private void showUSBDeviceChooseDialog() {
//        final UsbDeviceChooseDialog usbDeviceChooseDialog = new UsbDeviceChooseDialog();
//        usbDeviceChooseDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                UsbDevice mUsbDevice = (UsbDevice) parent.getAdapter().getItem(position);
//                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
//                        MainActivity.this,
//                        0,
//                        new Intent(MainActivity.this.getApplicationInfo().packageName),
//                        0);
//                tv_device_selected.setText(getString(R.string.adapter_usbdevice) + mUsbDevice.getDeviceId()); //+ (position + 1));
//                configObj = new UsbConfigBean(BaseApplication.getInstance(), mUsbDevice, mPermissionIntent);
//                tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
//                isConfigPrintEnable(configObj);
//                usbDeviceChooseDialog.dismiss();
//            }
//        });
//        usbDeviceChooseDialog.show(getFragmentManager(), null);
//    }
//
//    /**
//     * 设置是否可进行打印操作
//     *
//     * @param isEnable
//     */
//    private void setPrintEnable(boolean isEnable) {
//        btn_selftest_print.setEnabled(isEnable);
//        btn_txt_print.setEnabled(isEnable);
//        btn_img_print.setEnabled(isEnable);
//        btn_template_print.setEnabled(isEnable);
//        btn_barcode_print.setEnabled(isEnable);
//        btn_connect.setEnabled(!isEnable);
//        btn_disConnect.setEnabled(isEnable);
//        btn_beep.setEnabled(isEnable);
//        btn_all_cut.setEnabled(isEnable);
//        btn_cash_box.setEnabled(isEnable);
//        btn_wifi_setting.setEnabled(isEnable);
//        btn_wifi_ipdhcp.setEnabled(isEnable);
//        btn_cmd_test.setEnabled(isEnable);
//        btn_test.setEnabled(isEnable);
////        btn_label_setting.setEnabled(isEnable);
//        btn_print_status.setEnabled(isEnable);
//        btn_print_status2.setEnabled(isEnable);
//
//    }
//
//    private void isConfigPrintEnable(Object configObj) {
//        if (isInConnectList(configObj)) {
//            setPrintEnable(true);
//        } else {
//            setPrintEnable(false);
//        }
//    }
//
//    private boolean isInConnectList(Object configObj) {
//        boolean isInList = false;
//        for (int i = 0; i < printerInterfaceArrayList.size(); i++) {
//            PrinterInterface printerInterface = printerInterfaceArrayList.get(i);
//            if (configObj.toString().equals(printerInterface.getConfigObject().toString())) {
//                if (printerInterface.getConnectState() == ConnectStateEnum.Connected) {
//                    isInList = true;
//                    break;
//                }
//            }
//        }
//        return isInList;
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        System.exit(0);//完全退出应用，关闭进程
//    }
//
//
//
//}
