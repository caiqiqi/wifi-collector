package com.farproc.wifi.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.farproc.wifi.connecter.R;
import com.farproc.wifi.utils.ClientThread;
import com.farproc.wifi.utils.Constants;
import com.farproc.wifi.utils.DialogUtil;
import com.farproc.wifi.utils.SharedPrefsUtil;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// PreferenceActivity继承自ListActivity，哈哈，这我就放心了，可以放开使用ListActivity的方法了
public class WifiScanActivity extends PreferenceActivity {

	public static final String TAG = "WifiScanActivity";

	private static final String KEY_PARCELABLE = "com.farproc.wifi.ui.WifiScanActivity";
	private static final String NETWORK_OK = "网络连接畅通";
	private static final int TIME_INTERVAL = 10;//单位：秒

	private String mServerIp;
	private int mServerPort;

	private WifiManager mWifiManager;
	// 用一个List来保存扫描到的各个热点的扫描结果
	private List<ScanResult> mList_Results;

	private ListView mListView;
	private WifiapAdapter mAdapter;

	private ClientThread mRun_ClientThread;
	private Thread mThd_ClientThread;

	public ScheduledExecutorService mExecutor;

	public Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initWifiManager();

		initAdapter();

		initListView();

		initHandler();
	}

	private void initPrefs() {

		String ip = SharedPrefsUtil.getStringPrefrences(this, "IP", SharedPrefsUtil.FILE_NAME);
		int port = SharedPrefsUtil.getIntPrefrences(this, "PORT", SharedPrefsUtil.FILE_NAME);

		if (ip == null || port ==0){
			mServerIp = Constants.SERVER_IP;
			mServerPort = Constants.SERVER_PORT;
		} else{
			mServerIp = ip;
			mServerPort =port;
		}

	}

	private void initWifiManager() {
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	}

	private void sendToServer() {
		// 定时向指定服务器发送热点信息
		if (isOnline()) {
			Log.d(TAG, NETWORK_OK);

			if (mList_Results != null) {
				Log.d(TAG, "mList_Results不为null");

				sendUsingThreadPool(mList_Results);
			}
		}
	}


	private void sendUsingThreadPool(final List<ScanResult> scanResult) {
		Log.d(TAG, "sendUsingThreadPool");
		// 启动一个线程每10秒钟向日志文件写一次数据
		mExecutor = Executors.newScheduledThreadPool(1);
		mExecutor.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {

				sendMessage(scanResult);
			}

		}, 0, TIME_INTERVAL, TimeUnit.SECONDS);
	}

	private void sendMessage(final List<ScanResult> scanResult) {

		Log.d(TAG, "sendMessage");

		//对于Message对象，一般并不推荐直接使用它的构造方法得到，而是建议通过使用Message.obtain()这个静态的方法
		Message msg = new Message();
		msg.what = Constants.MESSAGE_TO_BE_SENT;
		// 靠，直接把mList_Results作为msg.obj不就行了
		msg.obj = scanResult;

		if (mRun_ClientThread.rcvHandler != null) {
			mRun_ClientThread.rcvHandler.sendMessage(msg);
		}
	}

	private void startNewThread() {
		// 是不是这个开启新线程的动作如果放在onCreate()里面就不会执行onResume(）了？我感觉好像是的
		// 另外，这是不是一个bug？

		// 加一个新线程用于与服务器通信
		mRun_ClientThread = new ClientThread(mHandler, mServerIp, mServerPort);
		// 在主线程中启动ClientThread线程用来 a与服务器通信
		mThd_ClientThread = new Thread(mRun_ClientThread);
		mThd_ClientThread.start();
	}

	private void initAdapter() {
		// 这句话不能再onCreate()方法之前调用，即不能放在onCreate()方法的外面，因为系统得首先执行onCreate()
		mAdapter = new WifiapAdapter(this, mList_Results);
		// 为这个ListActivity设置Adapter
		setListAdapter(mAdapter);
	}

	private void initListView() {
		mListView = getListView();
		mListView.setOnItemClickListener(mItemOnClick);
	}

	private void initHandler() {

		this.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (msg.what == Constants.MESSAGE_RECEIVED_FROM_SERVER) {
					// 先通知用户，已经接受到来自服务器的消息了
					Toast.makeText(WifiScanActivity.this, "Synchronized!",
							Toast.LENGTH_SHORT).show();

					// TODO 还有后续的功能待完善。。。先不把msg显示出来
					Log.d("WifiScanActivity", msg.obj.toString());
				}
			}
		};
	}

	/**
	 * 每次Activity从启动到运行都会“注册”那个接收“扫描结果已经可用了”的Receiver，并且开始扫描
	 * 另外每次继续的时候就“注册”那个接收“扫描结果已经可用了”的Receiver，并且开始扫描
	 */
	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(mReceiver, filter);
		mWifiManager.startScan();
	}

	/**
	 * 每次暂停的时候就“注销”那个Receiver
	 */
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	/**
	 * 判断Wifi是否处于连接状态
	 */
	private boolean isWifiConnected() {
		return ((ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
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
	 * 获取BSSID
	 */
	private String getBSSID() {
		return mWifiManager.getConnectionInfo().getBSSID();
	}

	// 这个Receiver是接收这个 “SCAN_RESULTS_AVAILABLE_ACTION”Action的
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			// An access point scan has completed
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				mList_Results = mWifiManager.getScanResults();

				mAdapter.notifyDataSetChanged();

				mWifiManager.startScan();
			}

		}
	};

	public class WifiapAdapter extends BaseAdapter {

		private boolean isWifiConnected;
		private LayoutInflater inflater;
		private Context mmContext;

		public WifiapAdapter(Context context, List<ScanResult> list) {
			super();
			mmContext = context;
			mList_Results = list;
			inflater = getLayoutInflater();
			isWifiConnected = false;

		}

		@Override
		public int getCount() {
			if (mList_Results == null) {
				return 0;
			}
			return mList_Results.size();
		}

		@Override
		public Object getItem(int position) {
			return mList_Results.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ScanResult ap = mList_Results.get(position);
			ViewHolder viewHolder = null;
			isWifiConnected = false;

			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.listitem_wifiap, null);
				viewHolder.iv_rssi = ((ImageView) convertView.findViewById(R.id.wifiap_item_iv_rssi));
				viewHolder.tv_ssid = ((TextView) convertView.findViewById(R.id.wifiap_item_tv_ssid));
				viewHolder.tv_desc = ((TextView) convertView.findViewById(R.id.wifiap_item_tv_desc));

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (isWifiConnected() && ap.BSSID.equals(getBSSID())) {
				isWifiConnected = true;
			}

			viewHolder.tv_ssid.setText(ap.SSID);
			viewHolder.tv_desc.setText(getDesc(ap));
			Picasso.with(mmContext).load(getRssiImgId(ap)).into(viewHolder.iv_rssi);
			return convertView;
		}

		/**
		 * 根据具体的信号强度获取图标资源文件
		 *
		 * @param ap
		 * @return
		 */
		private int getRssiImgId(ScanResult ap) {
			int imgId;
			// 若以连接，则直接将“Connected”的图标显示出来
			if (isWifiConnected) {
				imgId = R.drawable.ic_connected;
			} else {
				// 取信号强度（dbm）的绝对值
				int rssi = Math.abs(ap.level);
				if (rssi > 100) {
					imgId = R.drawable.ic_small_wifi_rssi_0;
				} else if (rssi > 80) {
					imgId = R.drawable.ic_small_wifi_rssi_1;
				} else if (rssi > 70) {
					imgId = R.drawable.ic_small_wifi_rssi_2;
				} else if (rssi > 60) {
					imgId = R.drawable.ic_small_wifi_rssi_3;
				} else {
					imgId = R.drawable.ic_small_wifi_rssi_4;
				}
			}
			return imgId;
		}

		private String getDesc(ScanResult ap) {
			String desc = "";

			String descOri = ap.capabilities;
			if (descOri.toUpperCase().contains("WPA-PSK")
					|| descOri.toUpperCase().contains("WPA2-PSK")) {
				desc = "Secured";
			} else {
				desc = "Open";
			}

			// 是否连接此热点
			if (isWifiConnected) {
				desc = "Connected";
			}
			return desc;
		}
	}

	// 装ListView中的每一项的容器
	public static class ViewHolder {
		public ImageView iv_rssi;
		public TextView tv_ssid;
		public TextView tv_desc;
	}

	// 每一项的“单击”监听器
	private OnItemClickListener mItemOnClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			// 哦，其实开始就获取了 list_ScanResults这个List，而在这里把具体的ScanResult找出来
			final ScanResult result = mList_Results.get(position);
			launchWifiConnecter(WifiScanActivity.this, result);
		}
	};

	private void launchWifiConnecter(final Activity activity,
			final ScanResult scanResult_hotspot) {

		final Intent intent = new Intent(WifiScanActivity.this, FloatingActivity.class);
		intent.putExtra("com.farproc.wifi.connecter.extra.HOTSPOT", scanResult_hotspot);
		activity.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {

		switch (mi.getItemId()) {

			case R.id.action_start_sync:

				//先得到配置文件里的IP和PORT
				initPrefs();
				//开启新线程（建立socket）
				startNewThread();

				//将热点信息发送到服务器（socket的另一端）
				if (isOnline()){
					Log.d(TAG, NETWORK_OK);
					sendToServer();
					//结束之后将菜单选项设置为不可用
					mi.setEnabled(false);
				}
				//靠，怪不得每次点击这里都会调用Toast。。。原来是没有加break，导致继续向下一个case执行了
				break;


			case R.id.action_stop_sync:
				if (isOnline()) {
					Log.d(TAG, NETWORK_OK);

					stopSync();
					//结束之后将菜单选项设置为不可用
					mi.setEnabled(false);

				}
				break;

			case R.id.action_set_server:
				if (isOnline()) {
					Log.d(TAG, NETWORK_OK);

					//设置服务器IP和端口
					setServer();
				}
				break;
		}
		return true;

	}

	/**
	 * 设置服务器的IP和port
	 */
	private void setServer() {

		// TODO 设置服务器的IP和port
		String setServer = "Set Server";
		String message ="Set IP and Port";
		DialogUtil.showAlertWithTextDialog(this, setServer, message);
	}

	/**
	 * 停止向服务器上传热点信息
	 */
	private void stopSync() {

		//先关闭线程池
		if(mExecutor != null){
			mExecutor.shutdown();
		}

		if(mThd_ClientThread != null){
			//再关闭mClientThread
			mThd_ClientThread.interrupt();
		}


		Toast.makeText(WifiScanActivity.this, R.string.sync_stopped,
				Toast.LENGTH_SHORT).show();
	}

}
