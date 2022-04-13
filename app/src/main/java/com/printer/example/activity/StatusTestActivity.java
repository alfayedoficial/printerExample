package com.printer.example.activity;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.printer.example.R;
import com.printer.example.app.BaseActivity;
import com.printer.example.app.BaseApplication;
import com.printer.example.utils.BaseEnum;
import com.printer.example.utils.FuncUtils;
import com.printer.example.utils.ToastUtil;
import com.rt.printerlibrary.bean.PrinterStatusBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.cmd.ZplFactory;
import com.rt.printerlibrary.enumerate.Print80StatusCmd;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.utils.PrintStatusCmd;
import com.rt.printerlibrary.utils.PrinterStatusPareseUtils;

import java.io.UnsupportedEncodingException;

public class StatusTestActivity extends BaseActivity {
    private RTPrinter rtPrinter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statustest);
        initView();
        addListener();
        init();
    }

    @Override
    public void initView() {

    }

    @Override
    public void addListener() {

    }

    @Override
    public void init() {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
    }
    private CmdFactory getCmdFactory(@BaseEnum.CmdType int baseEnum) {
        CmdFactory cmdFactory = null;
        switch (baseEnum) {
            case BaseEnum.CMD_PIN:
                cmdFactory = new PinFactory();
                break;
            case BaseEnum.CMD_ESC:
                cmdFactory = new EscFactory();
                break;
            case BaseEnum.CMD_TSC:
                cmdFactory = new TscFactory();
                break;
            case BaseEnum.CMD_CPCL:
                cmdFactory = new CpclFactory();
                break;
            case BaseEnum.CMD_ZPL:
                cmdFactory = new ZplFactory();
                break;
            default:
                break;
        }
        return cmdFactory;
    }

 public void StatusTest(Print80StatusCmd statuscmd) {
     ToastUtil.show(this,"StatusTest");
     CmdFactory cmdFactory = getCmdFactory(BaseApplication.getInstance().getCurrentCmdType());
     Cmd cmd = cmdFactory.create();
     cmd.append(cmd.getPrint80StausCmd(statuscmd));
     byte[] cmddata = cmd.getAppendCmds();
   //  rtPrinter.setAlwaysReadInputStream(false);
     rtPrinter.writeMsgAsync(cmddata);
  byte[] bts = rtPrinter.getprinterStausMsg(cmddata);
     try {
         Thread.sleep(1000);
     } catch (InterruptedException e) {
         e.printStackTrace();
     }
   //  byte[] bts = new byte[0];
     try {
         bts = rtPrinter.readMsg();
     } catch (Exception e) {
         ToastUtil.show(this, e.getMessage());
         // 打印异常信息
         e.printStackTrace();
         Log.d("test", e.getMessage());
     }

     //  byte[] bts = rtPrinter.getprinterStausMsg(cmddata);

     if (bts == null || bts.length == 0) {
         if (statuscmd == Print80StatusCmd.cmd_Connect_status) {
             ToastUtil.show(this, "connect fail");
             return;
         }
// else if (statuscmd == Print80StatusCmd.cmd_IsPrinting) {
//             ToastUtil.show(this, "Is Printing");
//             return;
//         }
     }

         if (bts != null && bts.length > 0){
              ToastUtil.show(this,FuncUtils.ByteArrToHex(bts));
             int b = bts[0];
             switch (statuscmd) {
                 case cmd_Connect_status:
                     if (bts[0] == 0x1b && bts[1] == 0x46 && bts[2] == 0x01)
                         ToastUtil.show(this, "connect ok");//已连接
                     else
                         ToastUtil.show(this, "connect fail"); //连接失败
                     break;
                 case cmd_Opencover:
                     if ((b & 0x4) == 0x04)
                         ToastUtil.show(this, "Open cover");//开盖
                     else
                         ToastUtil.show(this, "no Open cover");
                     break;
                 case cmd_Exhausted_paper://纸用尽
                     if ((b & 0x60) == 0x60)
                         ToastUtil.show(this, "paper Exhausted");//纸用尽
                     else
                         ToastUtil.show(this, "Have paper"); //有纸
                     break;
                 case cmd_other_error:
                     boolean iserror = false;
                     if ((b & 0x08) == 0x8) {
                         ToastUtil.show(this, "Automatic paper cutting error");//自动切纸错误
                         iserror = true;
                     }
                     if ((b & 0x20) == 0x20) {
                         ToastUtil.show(this, "Unrecoverable error");//出现不可恢复的错误
                         iserror = true;
                     }
                     if ((b & 0x40) == 0x40) {
                         ToastUtil.show(this, "An automatic recovery error occurred");//出现可自动恢复的错误
                         iserror = true;
                     }
                     if (!iserror) {
                         ToastUtil.show(this, "No errors found");//未发现错误
                     }
                     break;
                 case cmd_outpaper:
                     if ((b & 0x20) == 0x20)
                         ToastUtil.show(this, "Out of paper");//缺纸
                     else
                         ToastUtil.show(this, "Have paper ok"); //有纸
                     break;
                 case cmd_IsPrinting:
                     if ((b & 0x60) == 0x60)
                         ToastUtil.show(this, "Is Printing");
                     else
                         ToastUtil.show(this, "Is not Printing");
                     break;
             }


         } else {
             ToastUtil.show(this, "无法获取数据");
         }
     }

    public void onBtnClick(View v) {

        switch (v.getId()) {
             case R.id.btn_connectStatus:
                StatusTest(Print80StatusCmd.cmd_Connect_status);
                break;
            case R.id.btn_Opencover://开盖
                StatusTest(Print80StatusCmd.cmd_Opencover);
                break;
            case R.id.btn_Exhausted_paper:
                StatusTest(Print80StatusCmd.cmd_Exhausted_paper);
                break;
            case R.id.btn_Other_error:
                StatusTest(Print80StatusCmd.cmd_other_error);
                break;
            case R.id.btn_Outpaper://缺纸
                StatusTest(Print80StatusCmd.cmd_outpaper);
                break;
            case R.id.btn_IsPrinting:
                StatusTest(Print80StatusCmd.cmd_IsPrinting);
                break;
            default:
                break;


        }
    }


}
