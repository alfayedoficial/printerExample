package com.printer.example.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.printer.example.R;

/**
 * Created by Tony on 2017/12/3.
 */

public abstract class BaseActivity extends Activity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public abstract void initView();

    public abstract void addListener();

    public abstract void init();

    /**
     * 跳转到指定acitivity
     *
     * @param cls
     */
    public void turn2Activity(Class<?> cls) {
        Intent i = new Intent(this, cls);
        startActivity(i);
    }

    /**
     * 跳转到指定acitivity,带参数
     *
     * @param cls
     * @param bundle
     */
    public void turn2Activity(Class<?> cls, Bundle bundle) {
        Intent i = new Intent(this, cls);
        i.putExtras(bundle);
        startActivity(i);
    }

    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showAlertDialog(final String msg){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialog = new AlertDialog.Builder(BaseActivity.this);
                dialog.setTitle(R.string.dialog_tip);
                dialog.setMessage(msg);
                dialog.setNegativeButton(R.string.dialog_back, null);
                dialog.show();
            }
        });
    }

    public void showProgressDialog(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog == null){
                    progressDialog = new ProgressDialog(BaseActivity.this);
                }
                if(!TextUtils.isEmpty(str)){
                    progressDialog.setMessage(str);
                }else{
                    progressDialog.setMessage("Loading...");
                }
                progressDialog.show();
            }
        });

    }

    public void hideProgressDialog(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.hide();
                }
            }
        });

    }

}
