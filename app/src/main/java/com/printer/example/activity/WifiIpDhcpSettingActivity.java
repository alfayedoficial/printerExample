package com.printer.example.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.ToastUtil;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.utils.WiFiSettingUtil;

/**
 * Created by YL01 on 2016/5/10.
 */
public class WifiIpDhcpSettingActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private Context mContext;
    private byte NET_MODE = 0x00;//0是ip，1是dhcp
    private byte DHCP_STATE = 0x00;//0是disable,1是enable

    private LinearLayout back;//返回
    private RadioGroup rg_set_net_type;
    private EditText et_set_net_ip, et_set_net_mask, et_set_net_gateway;

    private RTPrinter rtPrinter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wifi_net);
        mContext = this;
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {
        back = findViewById(R.id.back);
        rg_set_net_type = findViewById(R.id.rg_set_net_type);
        et_set_net_ip = findViewById(R.id.et_set_net_ip);
        et_set_net_mask = findViewById(R.id.et_set_net_mask);
        et_set_net_gateway = findViewById(R.id.et_set_net_gateway);
    }

    @Override
    public void addListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rg_set_net_type.setOnCheckedChangeListener(this);
    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
    }


    @Override
    protected void onResume() {
        super.onResume();
        et_set_net_mask.setText("255.255.255.0");
        et_set_net_gateway.setText("192.168.1.1");
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_set_net_dhcp_dis:
                et_set_net_ip.setEnabled(true);
                et_set_net_mask.setEnabled(true);
                et_set_net_gateway.setEnabled(true);
                DHCP_STATE = 0x00;
                break;
            case R.id.rb_set_net_dhcp_en:
                et_set_net_ip.setEnabled(false);
                et_set_net_mask.setEnabled(false);
                et_set_net_gateway.setEnabled(false);
                DHCP_STATE = 0x01;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tx_set_net_save_change:

                if(rtPrinter != null && rtPrinter.getConnectState() == ConnectStateEnum.Connected){
                    if (DHCP_STATE == 0x00) {//DHCP disable
                        final String S_net_ip = et_set_net_ip.getText().toString();
                        final String S_net_mask = et_set_net_mask.getText().toString();
                        final String S_net_gateway = et_set_net_gateway.getText().toString();
                        String[] ip_split = S_net_ip.split("\\.");
                        String[] mask_split = S_net_mask.split("\\.");
                        String[] gateway_split = S_net_gateway.split("\\.");
                        int ip_length = ip_split.length;
                        int mask_length = mask_split.length;
                        int gateway_length = gateway_split.length;
                        if ((ip_length == 4) && (mask_length == 4) && (gateway_length == 4)){
                            byte[] btDisableDHCP = WiFiSettingUtil.getInstance().setDHCP(false);
                            byte[] btIPSetting = WiFiSettingUtil.getInstance().setStaticIP(S_net_ip, S_net_mask, S_net_gateway);
                            rtPrinter.writeMsgAsync(btDisableDHCP);
                            rtPrinter.writeMsgAsync(btIPSetting);
                        }else{
                            ToastUtil.show(WifiIpDhcpSettingActivity.this, "Please input correct ip,sub_mask or gateway.");
                        }
                    } else if (DHCP_STATE == 0x01) {//DHCP enable
                        byte[] btEnableDHCP = WiFiSettingUtil.getInstance().setDHCP(true);
                        rtPrinter.writeMsgAsync(btEnableDHCP);
                    }
                }

                break;
        }
    }
}
