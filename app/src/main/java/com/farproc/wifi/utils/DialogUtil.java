package com.farproc.wifi.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by caiqiqi on 2015/11/24.
 */
public class DialogUtil {

    /*带输入文本框的对话框*/
    public static void showAlertWithTextDialog(final Context context, String title, String message) {


        //文本输入框,并设置提示语,没有设置输入的限制
        final EditText edit_ip_port = new EditText(context);
        edit_ip_port.setHint("IP:PORT");
        edit_ip_port.setInputType(EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE);

        AlertDialog alertDialog = null;
        if (!((Activity) context).isFinishing()) {
            if (alertDialog == null) {
                alertDialog = new AlertDialog.Builder(context).create();
            }
            alertDialog.setTitle(title);
            alertDialog.setIcon(android.R.drawable.ic_dialog_info);
            alertDialog.setMessage(message);

            alertDialog.setView(edit_ip_port);

            //左边的OK按钮
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    String ip_port = edit_ip_port.getText().toString();
                    if (!StringUtil.isBlank(ip_port)) {

                        String[] strsContent = null;
                        try {
                            //用字符串分割工具将输入分割成两个字符串
                            strsContent = StringUtil.splitStr(ip_port);

                            if (strsContent != null) {
                                SharedPrefsUtil.setStringPrefrences(context, "IP", strsContent[0], SharedPrefsUtil.FILE_NAME);
                                SharedPrefsUtil.setIntPrefrences(context, "PORT", Integer.valueOf(strsContent[1]), SharedPrefsUtil.FILE_NAME);
                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            //输入的格式不正确
                            Toast.makeText(context, "Format wrong",Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }


                    } else {
                        //提示用户“输入为空”
                        Toast.makeText(context, "Input null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //右边的取消按钮
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }
    }

    /*带单选框的对话框*/
//    public static void showAlertWithCheckBoxDialog(Context context, String title, String message){
//
//        new AlertDialog.Builder(context)
//                .setTitle("请选择")
//                .setIcon(android.R.drawable.ic_dialog_info)
//                .setSingleChoiceItems(new String[] {"选项1","选项2","选项3","选项4"}, 0,
//                        new DialogInterface.OnClickListener() {
//
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }
//                )
//                .setNegativeButton("取消", null)
//                .show();
//    }
}
