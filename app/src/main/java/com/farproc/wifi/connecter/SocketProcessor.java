package com.farproc.wifi.connecter;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketProcessor {

	public static final String TAG = "SocketProcessor";
	
	private static final String KEY_PARCELABLE = "com.farproc.wifi.ui.WifiScanActivity";
	
	private Intent intent;
	
	private ArrayList<ScanResult> mList_scanResult;
	private List<String> mList_string;
	
	private Socket s;
	// 该线程所处理的Socket所对应的输入流
	private BufferedReader br;

	private OutputStream os;
	// 改用ObjectOutputstream
	private ObjectOutputStream oos;
	
	private Handler mHandler = new Handler();
	
	public SocketProcessor(Socket s, Intent intent){
		this.intent = intent;
		this.s = s;
	}
	
	void processSocket(){
		
		initIO();
		Bundle bundle = this.intent.getExtras();
		mList_scanResult = bundle.getParcelableArrayList(KEY_PARCELABLE);
		mList_string= convertScanResult(mList_scanResult);
		
		Runnable runnable  = new Runnable(){

			@Override
			public void run() {
				
				try {
					
					if (oos != null) {
						oos.writeObject(mList_string);
						oos.flush();
						Log.d(TAG, "已写入ObjectOutputStream");
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		};
		
		//每隔10秒
		mHandler.postDelayed(runnable, 5*1000);
	}
	
	private void initIO(){

		try {
			
			br = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
			os = this.s.getOutputStream();
			oos = new ObjectOutputStream(os);
		} catch (IOException e) {
			
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
