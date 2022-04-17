package com.printer.example.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
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
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.enumerate.PrinterAskStatusEnum
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.utils.FuncUtils
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils
import java.io.UnsupportedEncodingException

const  val SP_KEY_PORT = "port"
const  val TAG = "TAG_TEST"
const val SP_KEY_IP = "ip"
const val REQUEST_CAMERA = 0

class KotlinMainActivity : BaseActivity() , PrinterObserver {

    private var dataBinder : ActivityKotlinMainBinding? = null
    private val needPermission = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
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

    override fun addListener() {

        radioButtonCheckListener() //single button listener

        dataBinder?.apply {
            rgCmdType.check(R.id.rb_cmd_esc)
            rgConnect.check(R.id.rb_connect_bluetooth)
        }
    }

    private fun radioButtonCheckListener() {
        dataBinder?.apply {
//            rgCmdType.setOnCheckedChangeListener { _, i ->
//                when (i) {
//                    R.id.rb_cmd_pin -> {
//                        BaseApplication.instance.currentCmdType = BaseEnum.CMD_PIN
//                        printerFactory = PinPrinterFactory()
//                        rtPrinter = printerFactory?.create()
//                        rtPrinter?.setPrinterInterface(curPrinterInterface)
//                        btnBarcodePrint.visibility = View.GONE
//                        btnLabelSetting.visibility = View.GONE
//                    }
//                    R.id.rb_cmd_esc -> {
//                        BaseApplication.instance.currentCmdType = BaseEnum.CMD_ESC
//                        printerFactory = ThermalPrinterFactory()
//                        rtPrinter = printerFactory?.create()
//                        rtPrinter?.setPrinterInterface(curPrinterInterface)
//                        btnBarcodePrint.visibility = View.VISIBLE
//                        btnLabelSetting.visibility = View.GONE
//                    }
//                    R.id.rb_cmd_tsc -> {
//                        BaseApplication.instance.currentCmdType = BaseEnum.CMD_TSC
//                        printerFactory = LabelPrinterFactory()
//                        rtPrinter = printerFactory?.create()
//                        rtPrinter?.setPrinterInterface(curPrinterInterface)
//                        btnBarcodePrint.visibility = View.VISIBLE
//                        btnLabelSetting.visibility = View.GONE
//                    }
//                    R.id.rb_cmd_cpcl -> {
//                        BaseApplication.instance.currentCmdType = BaseEnum.CMD_CPCL
//                        printerFactory = LabelPrinterFactory()
//                        rtPrinter = printerFactory?.create()
//                        rtPrinter?.setPrinterInterface(curPrinterInterface)
//                        btnBarcodePrint.visibility = View.VISIBLE
//                        btnLabelSetting.visibility = View.VISIBLE
//                    }
//                    R.id.rb_cmd_zpl -> {
//                        BaseApplication.instance.currentCmdType = BaseEnum.CMD_ZPL
//                        printerFactory = LabelPrinterFactory()
//                        rtPrinter = printerFactory?.create()
//                        rtPrinter?.setPrinterInterface(curPrinterInterface)
//                        btnBarcodePrint.visibility = View.VISIBLE
//                        btnLabelSetting.visibility = View.GONE
//                    }
//                }
//                BaseApplication.getInstance().rtPrinter = rtPrinter
//            }

            rgConnect.setOnCheckedChangeListener { _, i ->
                doDisConnect()
                when (i) {
                    R.id.rbConnectBluetooth -> checkedConType = BaseEnum.CON_BLUETOOTH
                }
            }
        }
    }

    override fun init() {
        dataBinder?.apply {

            //printer
            btnPrintStatus2.visibility = View.GONE
            BaseApplication.instance.currentCmdType = BaseEnum.CMD_ESC
            printerFactory = ThermalPrinterFactory()
            rtPrinterKotlin = printerFactory?.create() as RTPrinter<BluetoothEdrConfigBean>?
            rtPrinterKotlin?.setPrinterInterface(curPrinterInterface)
            btnBarcodePrint.visibility = View.VISIBLE
            btnLabelSetting.visibility = View.GONE
            BaseApplication.getInstance().rtPrinter = rtPrinterKotlin;

            tvVer.text = "PrinterExample Ver: v" + TonyUtils.getVersionName(this@KotlinMainActivity)
            PrinterObserverManager.getInstance().add(this@KotlinMainActivity)


            if (BaseApplication.getInstance().currentCmdType == BaseEnum.CMD_PIN) {
                btnBarcodePrint.visibility = View.GONE
            } else {
                btnBarcodePrint.visibility = View.VISIBLE
            }

            btnLabelSetting.isEnabled = true //TODO

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


    /*********** selfTestPrint ************/
    fun selfTestPrint() {
        when (BaseApplication.getInstance().currentCmdType) {
            BaseEnum.CMD_PIN -> pinSelfTestPrint()
            BaseEnum.CMD_ESC -> escSelfTestPrint()
            BaseEnum.CMD_TSC -> tscSelfTestPrint()
            BaseEnum.CMD_CPCL -> cpclSelfTestPrint()
            BaseEnum.CMD_ZPL -> zplSelfTestPrint()
            else -> {}
        }
    }

    private fun cpclSelfTestPrint() {
        val cmdFactory: CmdFactory = CpclFactory()
        val cmd = cmdFactory.create()
        // cmd.append(cmd.getCpclHeaderCmd(80,60,1));
        cmd.append(cmd.selfTestCmd)
        rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
    }

    private fun zplSelfTestPrint() {
        val cmdFactory: CmdFactory = ZplFactory()
        val cmd = cmdFactory.create()
        cmd.append(cmd.headerCmd)
        cmd.append(cmd.selfTestCmd)
        cmd.append(cmd.endCmd)
        rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
    }

    private fun tscSelfTestPrint() {
        val cmdFactory: CmdFactory = TscFactory()
        val cmd = cmdFactory.create()
        cmd.append(cmd.headerCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.selfTestCmd)
        rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
    }

    private fun escSelfTestPrint() {
        val cmdFactory: CmdFactory = EscFactory()
        val cmd = cmdFactory.create()
        cmd.append(cmd.headerCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.selfTestCmd)
        cmd.append(cmd.lfcrCmd)
        rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
    }

    private fun pinSelfTestPrint() {
        val cmdFactory: CmdFactory = PinFactory()
        val cmd = cmdFactory.create()
        cmd.append(cmd.headerCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.lfcrCmd)
        cmd.append(cmd.selfTestCmd)
        rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
    }

    /************* textPrint ****************/
    fun txtPrint() {
        try {
            textPrint()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun textPrint() {
        when (BaseApplication.getInstance().currentCmdType) {
            BaseEnum.CMD_ESC -> turn2Activity(
                TextPrintESCActivity::class.java
            )
            else -> turn2Activity(TextPrintActivity::class.java)
        }
    }


    /************* imagePrint ****************/
    fun imagePrint() {
//        turn2Activity(ImagePrintActivity::class.java)
        turn2Activity(KotlinImageActivity::class.java)
    }

    /************* toTemplateActivity ****************/
    fun toTemplateActivity() {
        turn2Activity(TempletPrintActivity::class.java)
    }

    /************* toBarcodeActivity ****************/
    fun toBarcodeActivity() {
        turn2Activity(BarcodeActivity::class.java)
    }

    /************* toWifiSettingActivity ****************/
    fun toWifiSettingActivity() {
        turn2Activity(WifiSettingActivity::class.java)
    }

    /************* toWifiIpDhcpSettingActivity ****************/
    fun toWifiIpDhcpSettingActivity() {
        turn2Activity(WifiIpDhcpSettingActivity::class.java)
    }

    /************* toBCmdTestActivity ****************/
    fun toBCmdTestActivity() {
        turn2Activity(CmdTestActivity::class.java)
    }

    /************* toLabelSettingActivity ****************/
    fun toLabelSettingActivity() {
        turn2Activity(LabelSettingActivity::class.java)
    }

    /************* testTsc ****************/
    fun testTsc() {
        if (rtPrinterKotlin == null) {
            return
        }
        TonyUtils.Tsc_InitLabelPrint(rtPrinterKotlin)
        val strPrintTxt = TonyUtils.printText("80", "80", "TSS24.BF2", "0", "1", "1", "Hello,容大!")
        val strPrint = TonyUtils.setPRINT("1", "1")
        try {
            rtPrinterKotlin!!.writeMsg(strPrintTxt.toByteArray(charset("GBK")))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        rtPrinterKotlin!!.writeMsg(strPrint.toByteArray())
    }

    /************* getPrintStatus ****************/
    fun getPrintStatus() {
        if(BaseApplication.getInstance().currentConnectType == BaseEnum.CON_USB) {
            val cmdFactory = EscFactory()
            val cmd = cmdFactory.create()
            cmd.append(cmd.getPrinterStatus(PrinterAskStatusEnum.Paper_status))
            rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            val msgBytes = rtPrinterKotlin!!.readMsg()
            if (msgBytes.isNotEmpty()) ToastUtil.show(this, msgBytes[0].toString()) else ToastUtil.show(this, "fail")
        }
    }

    /************* cashBoxTest ****************/
    fun cashBoxTest() {
        when (BaseApplication.getInstance().currentCmdType) {
            BaseEnum.CMD_ESC -> if (rtPrinterKotlin != null) {
                val cmdFactory: CmdFactory = EscFactory()
                val cmd = cmdFactory.create()
                cmd.append(cmd.openMoneyBoxCmd) //Open cashbox use default setting[0x00,0x20,0x01]

                //or custom settings
//                    byte drawNumber = 0x00;
//                    byte startTime = 0x05;
//                    byte endTime = 0x00;
//                    cmd.append(cmd.getOpenMoneyBoxCmd(drawNumber, startTime, endTime));
                Log.e("Fuuu", FuncUtils.ByteArrToHex(cmd.appendCmds))
                rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            }
            else -> if (rtPrinterKotlin != null) {
                val cmdFactory: CmdFactory = EscFactory()
                val cmd = cmdFactory.create()
                cmd.append(cmd.openMoneyBoxCmd) //Open cashbox
                rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            }
        }
    }

    /************* allCutTest ****************/
    fun allCutTest() {
        when (BaseApplication.getInstance().currentCmdType) {
            BaseEnum.CMD_ESC -> if (rtPrinterKotlin != null) {
                val cmdFactory: CmdFactory = EscFactory()
                val cmd = cmdFactory.create()
                cmd.append(cmd.allCutCmd)
                rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            }
            else -> if (rtPrinterKotlin != null) {
                val cmdFactory: CmdFactory = EscFactory()
                val cmd = cmdFactory.create()
                cmd.append(cmd.allCutCmd)
                rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            }
        }
    }

    /************* beepTest ****************/
    fun beepTest() {
        when (BaseApplication.getInstance().currentCmdType) {
            BaseEnum.CMD_ESC -> if (rtPrinterKotlin != null) {
                val cmdFactory: CmdFactory = EscFactory()
                val cmd = cmdFactory.create()
                cmd.append(cmd.beepCmd)
                rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            }
            else -> if (rtPrinterKotlin != null) {
                val cmdFactory: CmdFactory = EscFactory()
                val cmd = cmdFactory.create()
                cmd.append(cmd.beepCmd)
                rtPrinterKotlin!!.writeMsgAsync(cmd.appendCmds)
            }
        }
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
            btnSelfTestPrint.isEnabled = isEnable
            btnTxtPrint.isEnabled = isEnable
            btnImgPrint.isEnabled = isEnable
            btnTemplatePrint.isEnabled = isEnable
            btnBarcodePrint.isEnabled = isEnable
            btnConnect.isEnabled = !isEnable
            btnDisConnect.isEnabled = isEnable
            btnBeep.isEnabled = isEnable
            btnAllCut.isEnabled = isEnable
            btnCashBox.isEnabled = isEnable
            btnWifiSetting.isEnabled = isEnable
            btnWifiIpdhcp.isEnabled = isEnable
            btnCmdTest.isEnabled = isEnable
            btnTest.isEnabled = isEnable
            // btn_label_setting.setEnabled(isEnable);
            // btn_label_setting.setEnabled(isEnable);
            btnPrintStatus.isEnabled = isEnable
            btnPrintStatus2.isEnabled = isEnable
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


}