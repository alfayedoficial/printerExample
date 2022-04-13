package com.printer.example.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.BaseEnum;
import com.printer.example.utils.ToastUtil;

public class LabelSettingActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout label_setting_type, label_setting_size, label_setting_offset, label_setting_sd;
    private LinearLayout lin_saveset, lin_back;
    private TextView tv_size, tv_offset, tv_speed, tv_type;

    public static final String SP_KEY_LABEL_OFFSET = "labelOffset";
    public static final String SP_KEY_LABEL_SIZE = "labelSize";
    private static final String SP_KEY_LABEL_GAP = "labelGap";//For TSC
    private static final String SP_KEY_LABEL_SPEED = "labelSpeed";
    private static final String SP_KEY_LABEL_TYPE = "labelType";
    private String labelSize;
    private SharedPreferences sp;
    private Context mContext;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_setting);
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {
        mContext = this;

        label_setting_type = findViewById(R.id.label_setting_type);
        label_setting_size = findViewById(R.id.label_setting_size);
        label_setting_offset = findViewById(R.id.label_setting_offset);
        label_setting_sd = findViewById(R.id.label_setting_sd);
        lin_saveset = findViewById(R.id.lin_saveset);
        lin_back = findViewById(R.id.lin_back);

        tv_type = findViewById(R.id.tv_type);
        tv_size = findViewById(R.id.tv_size);
        tv_offset = findViewById(R.id.tv_offset);
        tv_speed = findViewById(R.id.tv_speed);
    }

    @Override
    public void addListener() {
        label_setting_type.setOnClickListener(this);
        label_setting_size.setOnClickListener(this);
        label_setting_offset.setOnClickListener(this);
        label_setting_sd.setOnClickListener(this);
        lin_saveset.setOnClickListener(this);
        lin_back.setOnClickListener(this);
    }

    @Override
    public void init() {
        inflater = LayoutInflater.from(this);
        sp = getSharedPreferences(BaseApplication.SP_NAME_SETTING, Context.MODE_PRIVATE);
        labelSize = sp.getString(SP_KEY_LABEL_SIZE, getResources().getStringArray(R.array.label_setting_size)[0]);
        String[] temp = labelSize.split("\\*");
        BaseApplication.labelWidth = temp[0];
        BaseApplication.labelHeight = temp[1];

        BaseApplication.labelSpeed = sp.getString(SP_KEY_LABEL_SPEED, "2");
        BaseApplication.labelOffset = sp.getString(SP_KEY_LABEL_OFFSET, "0");

        tv_speed.setText(BaseApplication.labelSpeed);
        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_CPCL:
                tv_type.setText("CPCL");
                break;
            default:
                tv_type.setText("CPCL");
                break;
        }
        tv_size.setText(labelSize);
        tv_speed.setText(BaseApplication.labelSpeed);
        tv_offset.setText(BaseApplication.labelOffset);
    }

    private void popup(String title, final String[] data, final TextView onclikedTextView) {
        View contentView = inflater.inflate(R.layout.popup_layout, null);
        TextView titleTv = (TextView) contentView.findViewById(R.id.popup_layout_title);
        titleTv.setText(title);
        ListView listView = (ListView) contentView.findViewById(R.id.popup_layout_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.popuplist_item_simple, R.id.pupoplist_item_tv);
        for (String e : data)
            adapter.add(e);
        listView.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(contentView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        View paretnView = inflater.inflate(R.layout.activity_label_setting, null);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.showAtLocation(paretnView, Gravity.CENTER, 0, 0);
        TextView quxiaoTv = (TextView) contentView.findViewById(R.id.popup_layout_quxiao);
        quxiaoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                                    long id) {
                onclikedTextView.setText(data[position]);
                popupWindow.dismiss();
            }
        });
    }

    private void popupLabelSize(String title, final String[] data, final TextView onclikedTextView) {
        View contentView = inflater.inflate(R.layout.popup_layout_custom, null);
        TextView titleTv = (TextView) contentView.findViewById(R.id.popup_layout_title);
        titleTv.setText(title);
        ListView listView = (ListView) contentView.findViewById(R.id.popup_layout_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.popuplist_item_simple, R.id.pupoplist_item_tv);
        for (String e : data)
            adapter.add(e);
        listView.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(contentView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        View paretnView = inflater.inflate(R.layout.activity_label_setting, null);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.showAtLocation(paretnView, Gravity.CENTER, 0, 0);

        final EditText et_lb_width = (EditText) contentView.findViewById(R.id.et_lb_width);
        final EditText et_lb_height = (EditText) contentView.findViewById(R.id.et_lb_height);
        et_lb_width.setText(onclikedTextView.getText().toString().split("\\*")[0]);
        et_lb_height.setText(onclikedTextView.getText().toString().split("\\*")[1]);
        TextView quxiaoTv = (TextView) contentView.findViewById(R.id.popup_layout_quxiao);
        quxiaoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        TextView okTv = (TextView) contentView.findViewById(R.id.popup_layout_ok);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclikedTextView.setText(et_lb_width.getText().toString() + "*" + et_lb_height.getText().toString());
                popupWindow.dismiss();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                                    long id) {
                String temp = data[position];
                et_lb_width.setText(temp.split("\\*")[0]);
                et_lb_height.setText(temp.split("\\*")[1]);
            }
        });
    }

    /**
     * 保存设置[本地保存]
     */
    private void saveSetting() {
        labelSize = tv_size.getText().toString();
        System.out.println("labelSize" + labelSize);
        BaseApplication.labelSizeStr = labelSize.replaceAll("(\\d+)", "$1mm");
        String[] temp = labelSize.split("\\*");
        BaseApplication.labelWidth = temp[0];
        BaseApplication.labelHeight = temp[1];
        BaseApplication.labelSpeed = tv_speed.getText().toString();
        BaseApplication.labelType = tv_type.getText().toString();
        BaseApplication.labelOffset = tv_offset.getText().toString();
        sp.edit().putString(SP_KEY_LABEL_SIZE, labelSize)
                .putString(SP_KEY_LABEL_SPEED, BaseApplication.labelSpeed)
                .putString(SP_KEY_LABEL_TYPE, BaseApplication.labelType)
                .putString(SP_KEY_LABEL_OFFSET, BaseApplication.labelOffset)
                .apply();
        ToastUtil.show(this, getString(R.string.save_setting));
        //   mSettingChanged = false;
        // initPrinter();
    }

    private void showOffsetSettingDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.view_edittext, null);
        final EditText et_text_num = view.findViewById(R.id.et_text_num);
        et_text_num.setText(tv_offset.getText().toString());
        dialog.setTitle(R.string.title_offset);
        dialog.setView(view);
        dialog.setNegativeButton(R.string.dialog_cancel, null);
        dialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                tv_offset.setText(et_text_num.getText().toString());
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        String[] data = null;
        switch (v.getId()) {
            case R.id.label_setting_type:

                break;
            case R.id.label_setting_size:
                data = getResources().getStringArray(R.array.label_setting_size);
                popupLabelSize(getResources().getString(R.string.title_size), data, tv_size);
                break;
            case R.id.label_setting_offset:
                showOffsetSettingDialog();
                break;
            case R.id.label_setting_sd:
                data = getResources().getStringArray(R.array.label_setting_sd);
                popup(getResources().getString(R.string.title_speed), data, tv_speed);
                break;
            case R.id.lin_saveset:
                saveSetting();
                break;
            case R.id.lin_back:
                finish();
                break;
            default:
                break;
        }
    }

}
