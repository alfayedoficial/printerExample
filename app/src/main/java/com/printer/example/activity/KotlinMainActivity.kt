package com.printer.example.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.printer.example.R
import com.printer.example.app.BaseActivity
import com.printer.example.app.BaseApplication
import com.printer.example.databinding.ActivityKotlinMainBinding
import com.printer.example.dialog.BluetoothDeviceChooseDialog
import com.printer.example.utils.BaseEnum
import com.printer.example.utils.BaseEnum.ConnectType
import com.printer.example.utils.TimeRecordUtils
import com.printer.example.utils.ToastUtil
import com.printer.example.utils.TonyUtils
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.cmd.*
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.BmpPrintMode
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.enumerate.PrinterAskStatusEnum
import com.rt.printerlibrary.exception.SdkException
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.setting.BitmapSetting
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.utils.FuncUtils
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils
import java.io.UnsupportedEncodingException

const  val TAG = "TAG_TEST"
const val REQUEST_CAMERA = 0

class KotlinMainActivity : BaseActivity() , PrinterObserver {

    private var bmpPrintWidth = 40

    private var dataBinder : ActivityKotlinMainBinding? = null
    private val needPermission = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_PRIVILEGED,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_SCAN,

    )
    private val noPermission: ArrayList<String> = ArrayList()

    @ConnectType
    private var checkedConType = BaseEnum.CON_BLUETOOTH

    companion object{
        var rtPrinterKotlin: RTPrinter<BluetoothEdrConfigBean>? = null
    }
    private var printerFactory: PrinterFactory? = null
    private var configObj: Any? = null
    private val printerInterfaceArrayList = ArrayList<PrinterInterface<*>>()
    private var curPrinterInterface: PrinterInterface<*>? = null
    private var iPrintTimes = 0

    private fun checkAllPermission() {
        noPermission.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (i in needPermission.indices) {
                if (checkSelfPermission(needPermission[i]) != PackageManager.PERMISSION_GRANTED) {
                    noPermission.add(needPermission[i])
                }
            }
            if (noPermission.size == 0) {
                recordVideo()
            } else {
                requestPermissions(noPermission.toTypedArray(), REQUEST_CAMERA)
            }
        } else {
            recordVideo()
        }
    }

    private fun recordVideo() {
        Log.d(TAG, "recordVideo")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinder = DataBindingUtil.setContentView(this, R.layout.activity_kotlin_main)
        initView()
        init()
        dataBinder?.activity = this
        addListener()
        checkAllPermission()
    }

    override fun initView() {
        Log.i(TAG, "initView")
    }

    override fun addListener() {}

    override fun init() {
        dataBinder?.apply {

            //printer
            BaseApplication.instance.currentCmdType = BaseEnum.CMD_ESC
            printerFactory = ThermalPrinterFactory()
            rtPrinterKotlin = printerFactory?.create() as RTPrinter<BluetoothEdrConfigBean>?
            rtPrinterKotlin?.setPrinterInterface(curPrinterInterface)
            BaseApplication.getInstance().rtPrinter = rtPrinterKotlin;

            tvVer.text = "PrinterExample Ver: v" + TonyUtils.getVersionName(this@KotlinMainActivity)
            PrinterObserverManager.getInstance().add(this@KotlinMainActivity)

        }
    }

    override fun printerObserverCallback(printerInterface: PrinterInterface<*>, state: Int) {
        dataBinder?.apply {
            runOnUiThread {
                pbConnect.visibility = View.GONE
                when (state) {
                    CommonEnum.CONNECT_STATE_SUCCESS -> {
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis())
                        showToast(printerInterface.configObject.toString() + getString(R.string._main_connected))
                        tvDeviceSelected.text = printerInterface.configObject.toString()
                        tvDeviceSelected.tag = BaseEnum.HAS_DEVICE
                        curPrinterInterface = printerInterface //设置为当前连接， set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface) //多连接-添加到已连接列表
                        rtPrinterKotlin!!.setPrinterInterface(printerInterface)
                        //  BaseApplication.getInstance().setRtPrinter(rtPrinter);
                        setPrintEnable(true)
                    }
                    CommonEnum.CONNECT_STATE_INTERRUPTED -> {
                        if (printerInterface.configObject != null) {
                            showToast(printerInterface.configObject.toString() + getString(R.string._main_disconnect))
                        } else {
                            showToast(getString(R.string._main_disconnect))
                        }
                        TimeRecordUtils.record("RT连接断开：", System.currentTimeMillis())
                        tvDeviceSelected.setText(R.string.please_connect)
                        tvDeviceSelected.tag = BaseEnum.NO_DEVICE
                        curPrinterInterface = null
                        printerInterfaceArrayList.remove(printerInterface) //多连接-从已连接列表中移除
                        //  BaseApplication.getInstance().setRtPrinter(null);
                        setPrintEnable(false)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun printerReadMsgCallback(printerInterface: PrinterInterface<*>?, bytes: ByteArray?) {
        dataBinder?.apply {
            runOnUiThread {
                val statusBean = PrinterStatusPareseUtils.parsePrinterStatusResult(bytes)
                if (statusBean.printStatusCmd == PrintStatusCmd.cmd_PrintFinish) {
                    if (statusBean.blPrintSucc) {
                        Log.e("mydebug", "print ok")
                        ToastUtil.show(this@KotlinMainActivity, "print ok")
                    } else {
                        ToastUtil.show(this@KotlinMainActivity, PrinterStatusPareseUtils.getPrinterStatusStr(statusBean))
                    }
                } else if (statusBean.printStatusCmd == PrintStatusCmd.cmd_Normal) {
                    ToastUtil.show(this@KotlinMainActivity, "print status：" + PrinterStatusPareseUtils.getPrinterStatusStr(statusBean)
                    )
                }
            }

        }
    }


    /************* imagePrint ****************/
    fun imagePrint() {
//        turn2Activity(ImagePrintActivity::class.java)
        turn2Activity(KotlinImageActivity::class.java)
    }


    /*********************************************************************************************/
    /*************************************** Bluetooth Methods ***********************************/
    /*********************************************************************************************/

    /************* showConnectedListDialog ****************/
    fun showConnectedListDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(R.string.dialog_title_connected_devlist)
        val devList = arrayOfNulls<String>(printerInterfaceArrayList.size)
        for (i in devList.indices) {
            devList[i] = printerInterfaceArrayList[i].configObject.toString()
        }
        if (devList.isNotEmpty()) {
            dialog.setItems(devList) { _, i ->
                val printerInter = printerInterfaceArrayList[i]
                dataBinder?.tvDeviceSelected?.text = printerInter.configObject.toString()
                rtPrinterKotlin?.setPrinterInterface(printerInter) // Connection port settings
                dataBinder?.tvDeviceSelected?.tag = BaseEnum.HAS_DEVICE
                curPrinterInterface = printerInter
                //  BaseApplication.getInstance().setRtPrinter(rtPrinter);//设置全局RTPrinter
                if (printerInter.connectState == ConnectStateEnum.Connected) {
                    setPrintEnable(true)
                } else {
                    setPrintEnable(false)
                }
            }
        } else {
            dialog.setMessage(R.string.pls_connect_printer_first)
        }
        dialog.setNegativeButton(R.string.dialog_cancel, null)
        dialog.show()
    }

    /************* setPrintEnable ****************/
    private fun setPrintEnable(isEnable: Boolean) {
        dataBinder?.apply {
            btnConnect.isEnabled = !isEnable
            btnDisConnect.isEnabled = isEnable
        }
    }

    /************* connectBluetooth ****************/
    private fun connectBluetooth(bluetoothEdrConfigBean: BluetoothEdrConfigBean) {
        val piFactory: PIFactory = BluetoothFactory()
        val printerInterface = piFactory.create()
        printerInterface.configObject = bluetoothEdrConfigBean
        rtPrinterKotlin!!.setPrinterInterface(printerInterface)
        try {
            rtPrinterKotlin!!.connect(bluetoothEdrConfigBean)
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.let { Log.d(TAG, it) }
        } finally {
        }
    }

    /************* doConnect ****************/
    fun doConnect() {
        if (dataBinder?.tvDeviceSelected?.tag.toString().toInt() == BaseEnum.NO_DEVICE) { //未选择设备
            showAlertDialog(getString(R.string.main_pls_choose_device))
            return
        }
        dataBinder?.pbConnect?.visibility = View.VISIBLE
        when (checkedConType) {
            BaseEnum.CON_BLUETOOTH -> {
                TimeRecordUtils.record("start：", System.currentTimeMillis())
                val bluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean
                iPrintTimes = 0
                connectBluetooth(bluetoothEdrConfigBean)
            }
            else -> dataBinder?.pbConnect?.visibility = View.GONE
        }
    }

    /************* doDisConnect ****************/
    fun doDisConnect() {
        if (dataBinder?.tvDeviceSelected?.tag.toString().toInt() == BaseEnum.NO_DEVICE) { //未选择设备
            //  showAlertDialog(getString(R.string.main_discon_click_repeatedly));
            return
        }
        if (rtPrinterKotlin != null && rtPrinterKotlin!!.getPrinterInterface() != null) {
            rtPrinterKotlin!!.disConnect()
        }
        dataBinder?.tvDeviceSelected?.text = getString(R.string.please_connect)
        dataBinder?.tvDeviceSelected?.tag = BaseEnum.NO_DEVICE
        setPrintEnable(false)
    }

    /************* showConnectDialog ****************/
     fun showConnectDialog() {
        when (checkedConType) {
            BaseEnum.CON_BLUETOOTH -> showBluetoothDeviceChooseDialog()
            else -> {}
        }
    }

    /************* showConnectDialog ****************/
    @SuppressLint("MissingPermission")
    private fun showBluetoothDeviceChooseDialog() {
        val bluetoothDeviceChooseDialog = BluetoothDeviceChooseDialog()
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener { device ->
            if (TextUtils.isEmpty(device.name)) {
                dataBinder?.tvDeviceSelected?.text = device.address
            } else {
                dataBinder?.tvDeviceSelected?.text = device.name + " [" + device.address + "]"
            }
            configObj = BluetoothEdrConfigBean(device)
            dataBinder?.tvDeviceSelected?.tag = BaseEnum.HAS_DEVICE
            isConfigPrintEnable(configObj as BluetoothEdrConfigBean)
        }
        bluetoothDeviceChooseDialog.show(this.fragmentManager, null)
    }

    private fun isConfigPrintEnable(configObj: Any) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true)
        } else {
            setPrintEnable(false)
        }
    }

    private fun isInConnectList(configObj: Any): Boolean {
        var isInList = false
        for (i in printerInterfaceArrayList.indices) {
            val printerInterface = printerInterfaceArrayList[i]
            if (configObj.toString() == printerInterface.configObject.toString()) {
                if (printerInterface.connectState == ConnectStateEnum.Connected) {
                    isInList = true
                    break
                }
            }
        }
        return isInList
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
        }catch (e : SdkException){
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
        paint.typeface = Typeface.create("Arial", Typeface.BOLD)
        canvas.drawText("512233111655", width / 2f, 150f, paint)
        canvas.drawText("name: Car", width / 2f, 180f, paint)
        canvas.drawText("250 L.E", width / 2f, 215f, paint)


        dataBinder?.mgBarCode2!!.setImageBitmap(bitmap)

        print(bitmap)
    }


}