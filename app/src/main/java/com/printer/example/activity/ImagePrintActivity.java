package com.printer.example.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.BaseEnum;
import com.printer.example.utils.FuncUtils;
import com.printer.example.utils.LogUtils;
import com.printer.example.utils.SaveMediaFileUtil;
import com.printer.example.utils.ToastUtil;
import com.printer.example.utils.TonyUtils;
import com.printer.example.view.BasicListDialog;
import com.rt.printerlibrary.bean.BitmapLimitSizeBean;
import com.rt.printerlibrary.bean.LableSizeBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscCmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.cmd.ZplFactory;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.PrintDirection;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.utils.BitmapConvertUtil;

import java.io.IOException;
import java.util.ArrayList;

public class ImagePrintActivity extends BaseActivity implements View.OnClickListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0xd0;
    private static final int ALBUM_IMAGE_ACTIVITY_REQUEST_CODE = 0xd1;

    private LinearLayout llUploadImage;
    private Button btn_print;
    private FrameLayout flContent;
    private ImageView ivImage;
    private EditText et_pic_width;

    private RTPrinter rtPrinter;
    private Uri imageUri;
    private Bitmap mBitmap, mTempBmp;
    private final static String TAG = "ImagePrint";
    private int bmpPrintWidth = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_print);
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {
        llUploadImage = findViewById(R.id.ll_print_image_upload_image);
        btn_print = findViewById(R.id.print);
        flContent = findViewById(R.id.fl_print_image_content);
        ivImage = findViewById(R.id.iv_print_image_image);
        et_pic_width = findViewById(R.id.et_pic_width);
    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
    }

    @Override
    public void addListener() {
        llUploadImage.setOnClickListener(this);
        btn_print.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print:
                try {
                    print();
//                    printForInd();//India Test
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ll_print_image_upload_image:
                showPictureModeChooseDialog();
                break;
            case R.id.fl_print_image_content:
                showPictureModeChooseDialog();
                break;
            default:
                break;
        }
    }

    private void printForInd() throws SdkException {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bill_bmptest);
        if (mBitmap == null) {//未选择图片
            ToastUtil.show(this, R.string.tip_upload_image);
            return;
        }

        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_PIN:
                pinPrint();
                break;
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

    private void print() throws SdkException {

        if (mBitmap == null) {//未选择图片
            ToastUtil.show(this, R.string.tip_upload_image);
            return;
        }
        try {
            bmpPrintWidth = Integer.parseInt(et_pic_width.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        switch (BaseApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_PIN:
                pinPrint();
                break;
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

    private void cpclPrint() throws SdkException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog(null);

                CmdFactory cpclFactory = new CpclFactory();
                Cmd cmd = cpclFactory.create();

                cmd.append(cmd.getCpclHeaderCmd(80, 60, 1, 0));
                BitmapSetting bitmapSetting = new BitmapSetting();
                bitmapSetting.setPrintPostion(new Position(20, 20));
                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getEndCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());
                }
                hideProgressDialog();
            }
        }).start();


    }

    private void tscPrint() throws SdkException {

        new Thread(new Runnable() {
            @Override
            public void run() {

                showProgressDialog(null);
                CmdFactory tscFac = new TscFactory();
                Cmd tscCmd = tscFac.create();

                tscCmd.append(tscCmd.getHeaderCmd());
                CommonSetting commonSetting = new CommonSetting();
                commonSetting.setLableSizeBean(new LableSizeBean(72, 100));
                commonSetting.setLabelGap(2);
                commonSetting.setPrintDirection(PrintDirection.NORMAL);
                tscCmd.append(tscCmd.getHeaderCmd());
                tscCmd.append(tscCmd.getCommonSettingCmd(commonSetting));


                BitmapSetting bitmapSetting = new BitmapSetting();
                bitmapSetting.setPrintPostion(new Position(10, 10));
                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                try {
                    tscCmd.append(tscCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    tscCmd.append(tscCmd.getPrintCopies(1));//Print Copies setting
                } catch (SdkException e) {
                    e.printStackTrace();
                }

                if (rtPrinter != null) {
                    LogUtils.d("mydebug","writeMsg-getIsPrinting="+String.valueOf(rtPrinter.getIsPrinting()));
                    rtPrinter.writeMsg(tscCmd.getAppendCmds());
                    if (rtPrinter.getIsPrinting()) {
                        LogUtils.d("mydebug","getIsPrinting=true-ok");
                    } else {
                        LogUtils.d("mydebug", "getIsPrinting=false-nook");
                    }
                    while (rtPrinter.getIsPrinting())  {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {

                        }
                    }
                    LogUtils.d("mydebug","print ok");
                   // rtPrinter.disConnect();


                }

                hideProgressDialog();
            }
        }).start();

    }

    private void zplPrint() throws SdkException {
        new Thread(new Runnable() {
            @Override
            public void run() {

                showProgressDialog(null);

                CmdFactory zplFac = new ZplFactory();
                Cmd zplCmd = zplFac.create();

                zplCmd.append(zplCmd.getHeaderCmd());
                CommonSetting commonSetting = new CommonSetting();
                commonSetting.setLableSizeBean(new LableSizeBean(80, 100));
                commonSetting.setLabelGap(3);
                commonSetting.setPrintDirection(PrintDirection.NORMAL);
                zplCmd.append(zplCmd.getHeaderCmd());
                zplCmd.append(zplCmd.getCommonSettingCmd(commonSetting));


                BitmapSetting bitmapSetting = new BitmapSetting();
                bitmapSetting.setPrintPostion(new Position(20, 20));
                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                try {
                    zplCmd.append(zplCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    zplCmd.append(zplCmd.getPrintCopies(1));
                } catch (SdkException e) {
                    e.printStackTrace();
                }

                zplCmd.append(zplCmd.getEndCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(zplCmd.getAppendCmds());
                }
                hideProgressDialog();
            }
        }).start();

    }

    /**
     * 图片测试
     */
    private void testPrint() {

        if (mBitmap == null) {//未选择图片
            return;
        }

        CmdFactory cmdFactory = new PinFactory();
        Cmd cmd = cmdFactory.create();


        byte[] toPrintBytes = new byte[]{0x00};//要发送的字节流数据
        cmd.append(toPrintBytes);

        rtPrinter.writeMsgAsync(cmd.getAppendCmds());

    }

    private void escPrint() throws SdkException {

        new Thread(new Runnable() {
            @Override
            public void run() {

                showProgressDialog("Loading...");

                CmdFactory cmdFactory = new EscFactory();
                Cmd cmd = cmdFactory.create();
                cmd.append(cmd.getHeaderCmd());
               CommonSetting commonSetting = new CommonSetting();
              //  commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                cmd.append(cmd.getCommonSettingCmd(commonSetting));

                BitmapSetting bitmapSetting = new BitmapSetting();

                /**
                 * MODE_MULTI_COLOR - 适合多阶灰度打印<br/> Suitable for multi-level grayscale printing<br/>
                 * MODE_SINGLE_COLOR-适合白纸黑字打印<br/>Suitable for printing black and white paper
                 */
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
//                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_MULTI_COLOR);


//                if (bmpPrintWidth > 72) {
//                    bmpPrintWidth = 72;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            et_pic_width.setText(bmpPrintWidth + "");
//                        }
//                    });
//                }
                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());//Sync Write
                }

                hideProgressDialog();
            }
        }).start();


        //将指令保存到bin文件中，路径地址为sd卡根目录
//        final byte[] btToFile = cmd.getAppendCmds();
//        TonyUtils.createFileWithByte(btToFile, "Esc_imageCmd.bin");
//        TonyUtils.saveFile(FuncUtils.ByteArrToHex(btToFile), "Esc_imageHex");

    }

    private void pinPrint() throws SdkException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog(null);
                CmdFactory cmdFactory = new PinFactory();
                Cmd cmd = cmdFactory.create();
//                cmd.append(cmd.getHeaderCmd());

                int limitDots = Integer.parseInt(et_pic_width.getText().toString())*8;
                BitmapSetting bitmapSetting = new BitmapSetting();
                CommonSetting commonSetting = new CommonSetting();



                commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//Left, Middle, Right
                cmd.append(cmd.getCommonSettingCmd(commonSetting));

                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());


                bitmapSetting.setBimtapLimitWidth(limitDots);



                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getEndCmd());

                rtPrinter.writeMsg(cmd.getAppendCmds());
                hideProgressDialog();
                //将指令保存到bin文件中，路径地址为sd卡根目录
//                final byte[] btToFile = cmd.getAppendCmds();
//                TonyUtils.createFileWithByte(btToFile, "imageCmd.bin");
//                TonyUtils.saveFile(FuncUtils.ByteArrToHex(btToFile), "Pin_imageHex");
            }
        }).start();


    }

    private void showPictureModeChooseDialog() {
        final BasicListDialog pictureModeChooseDialog = new BasicListDialog();
        ArrayList<String> contentList = new ArrayList<>();
        contentList.add(getString(R.string.picture));
        contentList.add(getString(R.string.album));
        Bundle args = new Bundle();
        args.putString(BasicListDialog.BUNDLE_KEY_TITLE, getString(R.string.choose_picture_position));
        args.putStringArrayList(BasicListDialog.BUNDLE_KEY_CONTENT_LIST, contentList);
        pictureModeChooseDialog.setArguments(args);
        pictureModeChooseDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getAdapter().getItem(position).toString().equals(getString(R.string.picture))) {
                    takeAPicture();
                } else {
                    openAlbum();
                }
                pictureModeChooseDialog.dismiss();
            }
        });
        pictureModeChooseDialog.show(getFragmentManager(), null);
    }

    private void takeAPicture() {
        if (!SaveMediaFileUtil.isExternalStorageWritable()) {
            ToastUtil.show(this, R.string.insert_sdcard_tip);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = SaveMediaFileUtil.getOutputMediaFileUri(getApplicationContext(), SaveMediaFileUtil.DIR_CAPTURE, SaveMediaFileUtil.MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ALBUM_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    showImage(imageUri);
                }
                SimpleCursorTreeAdapter adapter;
                break;
            case ALBUM_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    imageUri = data.getData();
                    showImage(imageUri);
                }
                break;
        }
    }

    private void showImage(Uri uri) {
        llUploadImage.setVisibility(View.GONE);
        flContent.setVisibility(View.VISIBLE);
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (BaseApplication.getInstance().getCurrentCmdType() == BaseEnum.CMD_ESC) {
            if (mBitmap.getWidth() > 48 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(ImagePrintActivity.this, uri, 48 * 8, 4000);
            }
        } else if (BaseApplication.getInstance().getCurrentCmdType() == BaseEnum.CMD_PIN) {
            if (mBitmap.getWidth() > 210 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(ImagePrintActivity.this, uri, 210 * 8, 4000);
            }
        } else {
            if (mBitmap.getWidth() > 72 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromUri(ImagePrintActivity.this, uri, 72 * 8, 4000);
            }
        }

        Log.d(TAG, "mBitmap getWidth = " + mBitmap.getWidth());
        Log.d(TAG, "mBitmap getHeight = " + mBitmap.getHeight());
        ivImage.setImageBitmap(mBitmap);
    }
}
