package com.printer.example.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;

import com.rt.printerlibrary.printer.RTPrinter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Tony on 2017/8/18.
 */

public class TonyUtils {

    /**
     * 质量压缩方法
     * Reduce picture quality
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image, int file_kb) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;

        while (baos.toByteArray().length / 1024 > file_kb && options > 0) { // 循环判断如果压缩后图片是否大于50kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static void showProgress(Context context, String msg, long showMillSec) {
        final ProgressDialog progressDialog = new ProgressDialog(context, AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage(msg);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //两秒后执行
                progressDialog.dismiss();
            }
        }, showMillSec);
    }

    /**
     * get App versionName
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    /**
     * 根据byte数组生成文件
     *
     * @param bytes 生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes, String fileName) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(Environment.getExternalStorageDirectory(),
                fileName);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public static void saveFile(String str, String fileName) {
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) { // SD卡根目录的hello.text
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + fileName + ".txt";
        } else  // 系统下载缓存根目录的hello.text
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + fileName + ".txt";

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] WakeUpPritner() {
        byte[] b = {'\0', '\0', '\0'};
        return b;
    }

    public static byte[] InitPrinter() {
        byte[] combyte = new byte[]{0x1B, 0x40};
        return combyte;
    }

    public static void Tsc_InitLabelPrint(RTPrinter rtPrinter) {
//        labelPrint.SetSize(RTApplication.labelWidth, RTApplication.labelHeight);
        byte[] btSize = SetSize("80", "40").getBytes();
        byte[] btGap = SetGAP("3", "0").getBytes();
        byte[] btDir = SetDIRECTION("0").getBytes();
        String str = "CLS\r\n";
        byte[] btCLS = str.getBytes();

        rtPrinter.writeMsg(btSize);
        rtPrinter.writeMsg(btGap);
        rtPrinter.writeMsg(btDir);
        rtPrinter.writeMsg(btCLS);

    }


    private static String SetSize(String width, String length) {
        int mIndex = 0;
        String cmdName = "SIZE";
        StringBuffer sb = new StringBuffer();
        String[] cmdArray = new String[1024];
        cmdArray[mIndex++] = cmdName;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = width;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = "mm";
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = length;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = "mm";
        cmdArray[mIndex++] = "\r\n";
        for (int i = 0; i < mIndex; i++)  //字符串数组转字符串，只能通过循环的方法
        {
            sb.append(cmdArray[i]);
        }
        return sb.toString();
    }

    private static String SetGAP(String height, String labelLength) {
        int mIndex = 0;
        String cmdName = "GAP";
        String cmd;
        StringBuffer sb = new StringBuffer();
        String[] cmdArray = new String[1024];
        cmdArray[mIndex++] = cmdName;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = height;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = "mm";
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = labelLength;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = "mm";
        cmdArray[mIndex++] = "\r\n";
        for (int i = 0; i < mIndex; i++)  //字符串数组转字符串，只能通过循环的方法
        {
            sb.append(cmdArray[i]);
        }
        cmd = sb.toString();
        return cmd;
    }

    private static String SetDIRECTION(String dir) {
        if (dir.equals(""))
            dir = "0";
        int mIndex = 0;
        String cmdName = "DIRECTION";
        String cmd;
        StringBuffer sb = new StringBuffer();
        String[] cmdArray = new String[1024];
        cmdArray[mIndex++] = cmdName;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = dir;
        cmdArray[mIndex++] = "\r\n";
        for (int i = 0; i < mIndex; i++)  //字符串数组转字符串，只能通过循环的方法
        {
            sb.append(cmdArray[i]);
        }
        cmd = sb.toString();
        return cmd;
    }

    public static String printText(String X, String Y, String font, String rotation, String x_multi, String y_multi, String content) {
        int mIndex = 0;
        String cmd;
        StringBuffer sb = new StringBuffer();
        String[] cmdArray = new String[1024];
        String cmdName = "TEXT";
        cmdArray[mIndex++] = cmdName;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = X;
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = Y;
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = "\"";
        cmdArray[mIndex++] = font;
        cmdArray[mIndex++] = "\"";
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = rotation;
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = x_multi;
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = y_multi;
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = "\"";
        cmdArray[mIndex++] = content.replaceAll(" ", "\b");
        cmdArray[mIndex++] = "\"";
        cmdArray[mIndex++] = "\r\n";
        for (int i = 0; i < mIndex; i++)  //字符串数组转字符串，只能通过循环的方法
        {
            sb.append(cmdArray[i]);
        }
        cmd = sb.toString();
        return cmd;
    }

    public static String setPRINT(String setM, String copyN) {
        int mIndex = 0;
        String cmdName = "PRINT";
        String cmd;
        StringBuffer sb = new StringBuffer();
        String[] cmdArray = new String[1024];
        cmdArray[mIndex++] = cmdName;
        cmdArray[mIndex++] = " ";
        cmdArray[mIndex++] = setM;
        cmdArray[mIndex++] = ",";
        cmdArray[mIndex++] = copyN;
        cmdArray[mIndex++] = "\r\n";
        for (int i = 0; i < mIndex; i++)  //字符串数组转字符串，只能通过循环的方法
        {
            sb.append(cmdArray[i]);
        }
        cmd = sb.toString();
        return cmd;
    }

}
