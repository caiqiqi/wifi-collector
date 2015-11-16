/*
 * Wifi Connecter
 * 
 * Copyright (c) 2011 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/ 

package com.farproc.wifi.connecter;

import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.farproc.wifi.ui.Floating;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 当前连接到的网络
 *
 */
public class CurrentNetworkContent extends BaseContent {

	public final String TAG = "CurrentNetworkContent";
	private ScanResult mScanResult;
	int mConnectCount;
	
	public CurrentNetworkContent(Floating floating, WifiManager wifiManager, ScanResult scanResult) {
		super(floating, wifiManager, scanResult);
		//先设置那几个TextView为GONE
		findViewAndSetGone();
	
		this.mScanResult = scanResult;
		final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		final DhcpInfo dhcpInfo =mWifiManager.getDhcpInfo();
		
//		mConnectCount = getConnsCount();
		new GetCountTask().execute();
		
		setText(wifiInfo, dhcpInfo);
	}

	private void setText(final WifiInfo wifiInfo, final DhcpInfo dhcpInfo) {
		if(wifiInfo == null) {
			Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
		} else {
			//SupplicantState:客户端状态
			final SupplicantState state = wifiInfo.getSupplicantState();
			final NetworkInfo.DetailedState detailedState = WifiInfo.getDetailedStateOf(state);
			if(detailedState == NetworkInfo.DetailedState.CONNECTED
					|| (detailedState == NetworkInfo.DetailedState.OBTAINING_IPADDR && wifiInfo.getIpAddress() != 0)) {
				
				this.findViewAndSetText(wifiInfo, dhcpInfo);
				
			}
		}
	}

/**
 * 找到View并设置TextView的文本
 * @param wifiInfo
 */
//哦，呵呵，这个 SuppressWarnings("deprecation")很有意思嘛，虽然被废弃了，但是加上这个标志之后就还可以继续用哈哈
	@SuppressWarnings("deprecation")
	private void findViewAndSetText(final WifiInfo wifiInfo, final DhcpInfo dhcpInfo) {
		
		((TextView)mView.findViewById(R.id.Status_TextView)).setText(R.string.status_connected);
		((TextView)mView.findViewById(R.id.LinkSpeed_TextView)).setText(wifiInfo.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);
		((TextView)mView.findViewById(R.id.Frequency_TextView)).setText(this.mScanResult.frequency + " "+ "MHz");
		((TextView)mView.findViewById(R.id.MacAddress_TextView)).setText(wifiInfo.getBSSID());
		
		((TextView)mView.findViewById(R.id.IPAddress_TextView)).setText(Formatter.formatIpAddress(dhcpInfo.ipAddress));
		((TextView)mView.findViewById(R.id.Netmask_TextView)).setText(Formatter.formatIpAddress(dhcpInfo.netmask));
		((TextView)mView.findViewById(R.id.Gateway_TextView)).setText(Formatter.formatIpAddress(dhcpInfo.gateway));
		((TextView)mView.findViewById(R.id.DNS1_TextView)).setText(Formatter.formatIpAddress(dhcpInfo.dns1));
		((TextView)mView.findViewById(R.id.DNS2_TextView)).setText(Formatter.formatIpAddress(dhcpInfo.dns2));
//		((TextView)mView.findViewById(R.id.CrrntConnecsCnt_TextView)).setText("" + mConnectCount);
		
	}

	/**
	 * 找到View并将其设置为 "Gone"
	 */
	private void findViewAndSetGone() {
		mView.findViewById(R.id.Password).setVisibility(View.GONE);
	}
	
	@Override
	public int getButtonCount() {
		// No Modify button for open network.
		return mIsOpenNetwork ? 2 : 3;
	}

	@Override
	public OnClickListener getButtonOnClickListener(int index) {
		if(mIsOpenNetwork && index == 1) {
			// No Modify button for open network.
			// index 1 is Cancel(index 2).
			return mOnClickListeners[2];
		}
		return mOnClickListeners[index];
	}

//获得按钮上的文本
	@Override
	public CharSequence getButtonText(int index) {
		switch(index) {
		case 0:
			return mFloating.getString(R.string.forget_network);
		case 1:
			if(mIsOpenNetwork) {
				// No Modify button for open network.
				// index 1 is Cancel.
				return getCancelString();
			}
			return mFloating.getString(R.string.button_change_password);
		case 2:
			return getCancelString();
		default:
			return null;
		}
	}

//得到网络的标题，即SSID(网络名字)
	@Override
	public CharSequence getTitle() {
		return mScanResult.SSID;
	}
	
//“忘记网络”的按钮监听器
	private OnClickListener mForgetOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
			boolean result = false;
			if(config != null) {
				result = mWifiManager.removeNetwork(config.networkId)
					&& mWifiManager.saveConfiguration();
			}
			if(!result) {
				Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
			}
			
			mFloating.finish();
		}
	};
	
	
//三个按钮监听器的集合
	private OnClickListener mOnClickListeners[] = {mForgetOnClick, mChangePasswordOnClick, mCancelOnClick};
	

	private class GetCountTask extends AsyncTask<Void, Void, Integer>{

		int count;
		@Override
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader("/proc/net/arp"));
				String line;
				while ((line = br.readLine()) != null) {
					
					String[] splitted = line.split(" +");
					//split(" +") 按空格进行拆分（也就是说只有按空格键流出来的空白才会是拆分的一句）

					if ((splitted != null) && (splitted.length >= 4)) {
						count++;
					}
				}
			} catch (Exception e) {
				Log.e(this.getClass().toString(), e.toString());
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getMessage());
				}
			}
			return count;
		}

		protected void onPostExecute(Integer connectionCount){
			((TextView)mView.findViewById(R.id.CrrntConnecsCnt_TextView)).setText("" + connectionCount);
		}
	}
}
