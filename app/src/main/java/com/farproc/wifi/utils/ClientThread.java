package com.farproc.wifi.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientThread implements Runnable {

	public static String TAG = "ClientThread";

	private String SERVER_IP = Constants.SERVER_IP;
	private int SERVER_PORT = Constants.SERVER_PORT;

	private Context mContext;
	private Socket s;

	// 向UI线程发送消息的Handler
	private static Handler mHandler = new Handler();
	// 接收UI线程消息的Handler对象
	public Handler rcvHandler;

	// 该线程所处理的Socket所对应的输入流
	private BufferedReader br;

	private OutputStream os;
	// 改用ObjectOutputstream
	private ObjectOutputStream oos;


	public ClientThread(Context context) {
		this.mContext = context;
	}

	public void run() {

		Log.d(TAG, "ClientThread started");
		try {

			initSocket();
			// 为当前线程初始化Looper
			Looper.prepare();

			// 注意Handler要在onCreate中创建，而不是在Thread线程创建之后再创建
			// 创建rcvHandler对象
			rcvHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					if (msg.what == Constants.MESSAGE_TO_BE_SENT) {

						List<ScanResult> list_result = (List<ScanResult>) msg.obj;
						List<String> list_string = convertScanResult(list_result);

						try {

							if (oos != null) {
								oos.writeObject(list_string);
								oos.flush();
								Log.d(TAG, "已写入ObjectOutputStream");
							}

						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}

			};
			// 启动一条子线程来读取服务器响应的数据
			new Thread() {

				@Override
				public void run() {
					Log.d(TAG, "子线程开启");
					String content;
					// 不断读取Socket输入流中的内容

					try {
						while ((content = br.readLine()) != null) {
							Log.d(TAG, "子线程读取到消息");
							// 每当读取到来自服务器的数据(一行一行的)之后，
							// 发送消息通知主线程，更新界面
							// 显读到的数据
							Message msg = new Message();
							msg.what = Constants.MESSAGE_RECEIVED_FROM_SERVER;
							msg.obj = content;

							// ***主线程中的Handler会处理的
							mHandler.sendMessage(msg);
							Log.d(TAG, "子线程的mHandler已发送消息：" + Constants.MESSAGE_RECEIVED_FROM_SERVER);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			// 启动Looper
			// 注意:写在Looper.loop()之后的代码不会被执行,这个函数内部应该是一个循环
			Looper.loop();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initSocket(){

		try {

			s = new Socket(SERVER_IP, SERVER_PORT);
			Log.d(TAG,"Socket " + s + "创建成功");

			br = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
			os = this.s.getOutputStream();
			oos = new ObjectOutputStream(os);

		} catch (IOException e) {
			Log.e(TAG, "initSocket failed !");
			e.printStackTrace();
		}
	}
	
/** 由于ScanResult不能Serizilible，这里将其转换为String */
	private List<String> convertScanResult(List<ScanResult> list){
		Log.d(TAG, "convertScanResult");
		List<String> strList= new ArrayList<String>();
		String strScanResult;
		for (ScanResult scanResult: list){
			strScanResult = scanResult.toString();
			strList.add(strScanResult);
		}
		Log.d(TAG, "ScanResult对象转换成功");
		return strList;
		
	}
}