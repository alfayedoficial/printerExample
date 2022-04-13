package com.printer.example.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.printer.example.R;

import java.util.List;

/**
 * Created by Administrator on 2015/7/1.
 */
public class UsbDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<UsbDevice> mList;
    private LayoutInflater mInflater;

    public UsbDeviceAdapter(Context context, List<UsbDevice> list) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView tvText;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.basic_dialog_item, null);
            holder = new ViewHolder();
            holder.tvText = (TextView) convertView.findViewById(R.id.tv_basic_dialog_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UsbDevice usbDevice = mList.get(position);
        holder.tvText.setText(mContext.getString(R.string.adapter_usbdevice) + "\n"
                        + "ProductID:" + usbDevice.getProductId() + "\n"
                        + "VendoID:" + usbDevice.getVendorId() + "\n"
//                + "DeviceClass:"+ usbDevice.getDeviceClass() + "\n"
//                        + "ProductName:" + usbDevice.getProductName() == null ? "null" : usbDevice.getProductName() + "\n"
        );
        return convertView;
    }

}
