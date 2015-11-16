package com.farproc.wifi.connecter;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.farproc.wifi.utils.Constants;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketIntentService extends IntentService {

	public static final String TAG = "SocketIntentService";
	
	private static final String SERVER_IP = Constants.SERVER_IP;
	private static final int SERVER_PORT = Constants.SERVER_PORT;
	
	
	private Socket s;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "SocketIntentService started");
		
	}

	public SocketIntentService() {
		super(TAG);
	}
	
	
	private void initSocket() {
		
		try {
			
			s = new Socket(SERVER_IP, SERVER_PORT);
		} catch (UnknownHostException e) {
			
			Toast.makeText(this, "Hostname can not be resolved!",Toast.LENGTH_SHORT ).show();
			e.printStackTrace();
		} catch (IOException e) {
			
			Toast.makeText(this, "Socket error!",Toast.LENGTH_SHORT ).show();
			e.printStackTrace();
		}
	}


	@Override
	protected void onHandleIntent(Intent intent) {

		Log.d(TAG, "onHandleIntent");
		
		initSocket();
		SocketProcessor processor = new SocketProcessor(this.s, intent);
		
		if (isOnline()) {
			Log.d(TAG, "start processSocket");
			processor.processSocket();
		}
		
	}
	
	/**
	 * 下面不用管，不是核心方法
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Toast.makeText(this, "SocketIntentService starting", Toast.LENGTH_SHORT).show();
	    return super.onStartCommand(intent,flags,startId);
	}
	
	
	/**
	 * 判断网络连接是否畅通
	 */
	private boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		return (networkInfo != null && networkInfo.isConnected());
	}

	/**
	 * 判断Wifi是否处于连接状态
	 */
	private boolean isWifiConnected() {
		return ((ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}
	
}
