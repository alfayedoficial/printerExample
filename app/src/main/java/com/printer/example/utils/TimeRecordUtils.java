package com.printer.example.utils;

import static android.service.controls.ControlsProviderService.TAG;

import android.util.Log;

/**
 * Project: PrintSet0517<br/>
 * Created by Tony on 2018/6/25.<br/>
 * Description:
 */

public class TimeRecordUtils {

    public synchronized static void record(String describe, long timemills){
        Log.e("TAG_TEST", timemills + "\t" + describe);
    }

}
