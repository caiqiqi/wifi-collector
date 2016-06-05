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

package com.farproc.wifi.ui;


import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;

import com.farproc.wifi.connecter.ConfiguredNetworkContent;
import com.farproc.wifi.connecter.CurrentNetworkContent;
import com.farproc.wifi.connecter.NewNetworkContent;
import com.farproc.wifi.connecter.Wifi;

public class FloatingActivity extends Floating {

	public static final String parce_ScanResult = "ScanResult";

	/**
	 * 关于 ScanResult,它包括： 
	 * 1.SSID (The network name) 
	 * 2.BSSID (The address of the access point) 
	 * 3.capabilities (Describes the authentication, key management, and encryption schemes supported by the access point) 
	 * 4.level(The detected signal level in dBm) 
	 * 5.frequency (The frequency in MHz of the channel over which the client is communicating with the access point)
	 * 6.timestamp (timestamp in microseconds when this result was last seen)
	 */
	private ScanResult mScanResult;

	private Floating.Content mContent;
	private WifiManager mWifiManager;

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		// This activity has "singleInstance" launch mode.
		// Update content to reflect the newest intent.
		doNewIntent(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//注意super与requestFeature()调用的顺序
		super.onCreate(savedInstanceState);
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		
		doNewIntent(getIntent());
	}

	private void doNewIntent(final Intent intent) {
		// 得到该项AP的扫描结果
		mScanResult = intent.getParcelableExtra(parce_ScanResult);

		if (mScanResult == null) {
			Toast.makeText(this, "No data in Intent!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		constructNetworkContent();
		//把上面的工作做完之后，无论上面结果怎样，都setContent()，将结果显示在Activity中
		setContent(mContent);
	}

//判断是否是当前连接的网络
	private Boolean isCurrentNetwork() {

		final WifiInfo info = mWifiManager.getConnectionInfo();
		// 若WifiInfo不为空，SSID(wifi热点的名字)，BSSID(wifi热点的地址)都相符，则认为是同一个热点。。。
		if (info != null
				&& android.text.TextUtils.equals(info.getSSID(), mScanResult.SSID)
				&& android.text.TextUtils.equals(info.getBSSID(),mScanResult.BSSID)  ) {
			return true;
		}
		return false;
	}

	private Boolean isCurrentConfigurationStatus(String str_security, WifiConfiguration config) {
		if (config.status == WifiConfiguration.Status.CURRENT) {
			return true;
		}
		return false;
	}
	
	private void constructNetworkContent(){
		
				final String str_security = Wifi.ConfigSec .getScanResultSecurity(mScanResult);
				// 再将这个扫描结果配置成一个WifiConfiguration
				final WifiConfiguration config = Wifi.getWifiConfiguration( mWifiManager, mScanResult, str_security);
				
				if (config == null) {
					mContent = new NewNetworkContent(this, mWifiManager, mScanResult);
				} else {
					
					if (isCurrentNetwork() || isCurrentConfigurationStatus(str_security,config)) {
						mContent = new CurrentNetworkContent(this, mWifiManager, mScanResult);
					} else {
						mContent = new ConfiguredNetworkContent(this, mWifiManager, mScanResult);
					}
				}
	}
}
