package com.printer.example.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.printer.example.R;
import com.printer.example.adapter.WifiListAdapter;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.ToastUtil;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.enumerate.WiFiModeEnum;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.utils.WiFiSettingUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WifiSettingActivity extends BaseActivity {

    private static final String[] WIFI_MMODE = {"STA", "AP"};//spinner的资源
    private WiFiModeEnum wifi_mode = WiFiModeEnum.STA;//wifi模式，0--STA,1--AP

    private LinearLayout back;//返回
    private Spinner sp_wifi_set;
    private WifiManager wifiManager;
    private List<ScanResult> scanResults;// 拿到扫描周围wifi结果
    private ListView lv_set_wifi;
    private WifiListAdapter wifi_adapter;// listview的适配器
    private ComparatoLevel comparatoLevel = new ComparatoLevel();
    private RTPrinter rtPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wifi);
        init();
        initData();
        initView();
        addListener();
    }

    public void init() {
        // TODO Auto-generated method stub
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
        scanResults = sortScanWifi();
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
    }

    private void initData() {
    }

    public void initView() {
        back = findViewById(R.id.back);
        sp_wifi_set = findViewById(R.id.sp_wifi_set);
        lv_set_wifi = findViewById(R.id.lv_set_wifi);
        wifi_adapter = new WifiListAdapter(this, scanResults);
        lv_set_wifi.setAdapter(wifi_adapter);
        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, WIFI_MMODE);
        sp_wifi_set.setAdapter(spinner_adapter);
        sp_wifi_set.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        wifi_mode = WiFiModeEnum.STA;
                        break;
                    case 1:
                        wifi_mode = WiFiModeEnum.AP;
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setListener();
    }

    @Override
    public void addListener() {

    }

    private void setListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv_set_wifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScanResult scanResult = scanResults.get(position);
                initSetWiFi(scanResult);
            }
        });
    }

    /**
     * .拿到扫描的到的指定wifi信息,进行分析
     *
     * @param scanResult
     */
    private void initSetWiFi(ScanResult scanResult) {

        showConfirmDialog(scanResult);
    }

    /**
     * 弹出确认连接wifi的dialog
     *
     * @param scanResult
     */
    private void showConfirmDialog(final ScanResult scanResult) {
        int WIFIType = 0;
        if (scanResult.capabilities.contains("WPA2-PSK")) {//加密模式WPA-PSK
            WIFIType = 1;
        } else if (scanResult.capabilities.contains("WPA-PSK")) {//加密模式WPA2-PSK
            WIFIType = 1;
        } else if (scanResult.capabilities.contains("WEP")) {//加密模式WEP
            WIFIType = 2;
        } else {//开放无密码
            WIFIType = 0;
        }

        LayoutInflater inflater = getLayoutInflater();
        View inflate = inflater.inflate(R.layout.wifi_confirm_dialog, null);
        TextView tx_wifi_name = (TextView) inflate.findViewById(R.id.tx_wifi_name);
        final LinearLayout ll_wifi_pd = (LinearLayout) inflate.findViewById(R.id.ll_wifi_pd);
        final EditText et_wifi_pd = (EditText) inflate.findViewById(R.id.et_wifi_pd);
        tx_wifi_name.setText(scanResult.SSID);
        if (WIFIType == 0) {
            ll_wifi_pd.setVisibility(View.GONE);
        } else {
            ll_wifi_pd.setVisibility(View.VISIBLE);
        }
        new AlertDialog.Builder(this).setTitle(R.string.confirm_set_this_wifi).setView(inflate).setCancelable(false).setPositiveButton(getResources().getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (rtPrinter == null || rtPrinter.getConnectState() != ConnectStateEnum.Connected) {
                    ToastUtil.show(WifiSettingActivity.this, R.string.pls_connect_printer_first);
                    return;
                }
                //write the wifi infos to the printer.
                rtPrinter.writeMsgAsync(WiFiSettingUtil.getInstance().setWiFiParam(scanResult, et_wifi_pd.getText().toString(), wifi_mode));
            }
        }).setNegativeButton(getResources().getText(R.string.dialog_cancel), null).show();

    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_wifi_refresh:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.show();
                scanResults.clear();
                wifiManager.startScan();
                scanResults.addAll(sortScanWifi());
                wifi_adapter.notifyDataSetChanged();
                progressDialog.cancel();
                break;
        }
    }

    /**
     * 整理扫描wifi获得的list
     */
    private List<ScanResult> sortScanWifi() {
        List<ScanResult> scanR = wifiManager.getScanResults();
        int size = scanR.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.isEmpty(scanR.get(i).SSID)) {
                scanR.remove(i);
                size--;//因为集合删除元素, 长度减了
            }
        }
        Collections.sort(scanR, comparatoLevel);
        return scanR;
    }

    private class ComparatoLevel implements Comparator {
        public int compare(Object arg0, Object arg1) {
            ScanResult user0 = (ScanResult) arg0;
            ScanResult user1 = (ScanResult) arg1;
            int flag = user1.level - (user0.level);
            if (flag == 0) {
                return user1.SSID.compareTo(user0.SSID);
            } else {
                return flag;
            }
        }
    }

}
