package com.printer.example.activity

import android.graphics.*
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.printer.example.R
import com.printer.example.activity.KotlinMainActivity.Companion.rtPrinterKotlin
import com.printer.example.app.BaseActivity
import com.printer.example.databinding.ActivityKotlinImageBinding
import com.printer.example.utils.ToastUtil
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.enumerate.BmpPrintMode
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting


class KotlinImageActivity : BaseActivity() {

    private var dataBinder : ActivityKotlinImageBinding? = null

    private var bmpPrintWidth = 40

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinder = DataBindingUtil.setContentView(this, R.layout.activity_kotlin_image)
        dataBinder?.activity = this
    }

    override fun initView() {

    }

    override fun addListener() {

    }

    override fun init() {

    }


    fun generate(barCode :String){
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(
                barCode,
                BarcodeFormat.CODE_128,
                dataBinder?.mgBarCode!!.width,
                dataBinder?.mgBarCode!!.height
            )
            val bitmap = Bitmap.createBitmap(dataBinder?.mgBarCode!!.width, dataBinder?.mgBarCode!!.height, Bitmap.Config.RGB_565)
            for (i in 0 until dataBinder?.mgBarCode!!.width) {
                for (j in 0 until dataBinder?.mgBarCode!!.height) {
                    bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                }
            }

            convert(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }catch (e :SdkException ){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @Throws(SdkException::class)
    private fun print(mBitmap : Bitmap?) {
        if (mBitmap == null) { //未选择图片
            ToastUtil.show(this, R.string.tip_upload_image)
            return
        }
        try {
            bmpPrintWidth = dataBinder?.etPicWidth!!.text.toString().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        escPrint(mBitmap)
    }

    @Throws(SdkException::class)
    private fun escPrint(mBitmap : Bitmap) {
        Thread {
            showProgressDialog("Loading...")
            val cmdFactory: CmdFactory = EscFactory()
            val cmd = cmdFactory.create()
            cmd.append(cmd.headerCmd)
            val commonSetting = CommonSetting()
            //  commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
            cmd.append(cmd.getCommonSettingCmd(commonSetting))
            val bitmapSetting = BitmapSetting()

            bitmapSetting.bmpPrintMode = BmpPrintMode.MODE_SINGLE_COLOR
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
            bitmapSetting.bimtapLimitWidth = bmpPrintWidth * 8
            try {
                cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap))
            } catch (e: SdkException) {
                e.printStackTrace()
            }
            cmd.append(cmd.lfcrCmd)
            cmd.append(cmd.lfcrCmd)
            cmd.append(cmd.lfcrCmd)
            cmd.append(cmd.lfcrCmd)
            cmd.append(cmd.lfcrCmd)
            cmd.append(cmd.lfcrCmd)
            if (rtPrinterKotlin != null) {
                rtPrinterKotlin?.writeMsg(cmd.appendCmds) //Sync Write
            }
            hideProgressDialog()
        }.start()


        //将指令保存到bin文件中，路径地址为sd卡根目录
//        final byte[] btToFile = cmd.getAppendCmds();
//        TonyUtils.createFileWithByte(btToFile, "Esc_imageCmd.bin");
//        TonyUtils.saveFile(FuncUtils.ByteArrToHex(btToFile), "Esc_imageHex");
    }

    fun convert(mBitmap: Bitmap) {

        val width = 500
        val height = 220
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)


        val startX: Int = (canvas.width - mBitmap.width) / 2 //for horisontal position
        val startY: Int = (canvas.height - mBitmap.height) / 2 //for vertical position

        canvas.drawBitmap(mBitmap , startX.toFloat(), 0f, paint)

        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.textSize = 25f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("Arial",Typeface.BOLD)
        canvas.drawText("512233111655", width / 2f, 150f, paint)
        canvas.drawText("name: Car", width / 2f, 180f, paint)
        canvas.drawText("250 L.E", width / 2f, 215f, paint)


        dataBinder?.mgBarCode2!!.setImageBitmap(bitmap)

        print(bitmap)
    }


    private fun stringToBitMap(encodedString: String?): Bitmap? {
        return try {

            val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
           val bitmap =  BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            Log.i(TAG, "stringToBitMap: $encodedString")
            Log.i(TAG, "encodeByte: $encodeByte")
            Log.i(TAG, "stringToBitMap: $bitmap")

            bitmap
        } catch (e: java.lang.Exception) {
            e.message
            Log.i(TAG, "stringToBitMap: $e")

            null
        }


    }




}