package com.farproc.wifi.utils;

public class ClientScanResult {

	private String IpAddr;
	private String HWAddr;
	private String Device;

	public ClientScanResult(String ipAddr, String hWAddr, String device) {
		super();
		this.IpAddr = ipAddr;
		this.HWAddr = hWAddr;
		this.Device = device;
	}

	public String getIpAddr() {
		return IpAddr;
	}
	public void setIpAddr(String ipAddr) {
		IpAddr = ipAddr;
	}


	public String getHWAddr() {
		return HWAddr;
	}
	public void setHWAddr(String hWAddr) {
		HWAddr = hWAddr;
	}

	
	public String getDevice() {
		return Device;
	}
	public void setDevice(String device) {
		Device = device;
	}
	
}
