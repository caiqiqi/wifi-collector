package com.farproc.wifi.ui;

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
	public static final String parce_ScanResult = "ScanResult";
	private Context mContext = WifiScanActivity.this;

	/*从服务器上推送过来的推荐可能有两种：“优选” 和 “传统”*/
	private static final String LEVEL_SELECTED = "Selected";
	private static final String LEVEL_TRADITIONAL = "Traditional";

//	private static final String KEY_PARCELABLE = "com.farproc.wifi.ui.WifiScanActivity";
	private static final String NETWORK_OK = "网络连接畅通";
	private static final String NOTICE_CHECK_NETWORK = "请检查网络连接";
	private static final int TIME_INTERVAL = 10;//单位：秒

	private String mServerIp;
	private int mServerPort;

	private WifiManager mWifiManager;
	// 用一个List来保存扫描到的各个热点的扫描结果
	private List<ScanResult> mList_Results;

	private ListView mListView;
	private WifiapAdapter mAdapter;

	private ClientThread mClientThread;
	private Thread mThread;

	public ScheduledExecutorService mExecutor;

	public Handler mHandler;

	// 这个Receiver是接收这个 “SCAN_RESULTS_AVAILABLE_ACTION”的
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			// An access point scan has completed
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				mList_Results = mWifiManager.getScanResults();
				//更新界面的Adapter已更新界面
				mAdapter.notifyDataSetChanged();
				//扫描热点信息
				mWifiManager.startScan();
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//初始化以用来扫描热点信息
		initWifiManager();

		//Android界面显示相关的
		initAdapter();
		initListView();

		//初始化子线程与UI线程(主线程)通信的Handler对象
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


	private void sendUsingThreadPool(final List<ScanResult> scanResults) {
		Log.d(TAG, "sendUsingThreadPool");
		// 启动一个线程每10秒钟向日志文件写一次数据
		mExecutor = Executors.newScheduledThreadPool(1);
		mExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				sendMessage(scanResults);
			}

		}, 0, TIME_INTERVAL, TimeUnit.SECONDS);
	}

	private void sendMessage(final List<ScanResult> scanResults) {

		Log.d(TAG, "sendMessage");

		//对于Message对象，一般并不推荐直接使用它的构造方法得到，而是建议通过使用Message.obtain()这个静态的方法
		Message msg = new Message();
		msg.what = Constants.MESSAGE_TO_BE_SENT;
		// 靠，直接把mList_Results作为msg.obj不就行了
		msg.obj = scanResults;

		if (mClientThread.rcvHandler != null){
			Log.d(TAG, "rcvHandler不为null");
			mClientThread.rcvHandler.sendMessage(msg);
		}

	}

	private void startNewThread() {
		// 是不是这个开启新线程的动作如果放在onCreate()里面就不会执行onResume(）了？我感觉好像是的
		// 另外，这是不是一个bug？

		// 加一个新线程用于与服务器通信
		mClientThread = new ClientThread(mHandler, mServerIp, mServerPort);
		// 在主线程中启动ClientThread线程用来与服务器通信
		mThread = new Thread(mClientThread);
		mThread.start();
	}

	private void initAdapter() {
		// 这句话不能再onCreate()方法之前调用，即不能放在onCreate()方法的外面，因为系统得首先执行onCreate()
		mAdapter = new WifiapAdapter(this, mList_Results);
		// 为这个ListActivity(PreferenceActivity)设置Adapter
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
					Toast.makeText(mContext, "Synchronized!",
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
		//加入这个action： SUPPLICANT_CONNECTION_CHANGE_ACTION
		final IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION );

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


	public class WifiapAdapter extends BaseAdapter {

		private boolean isWifiConnected;
		private Context mmContext;

		public WifiapAdapter(Context context, List<ScanResult> list) {
			super();
			mmContext = context;
			mList_Results = list;
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


		// getView()方法返回的itemView对象用来显示ListView的一个Item
		/*使用LayoutInflater类的inflater方法可以从list_item.xml文件中解析出itemView对象,
		但此时不可直接将itemView返回, 还需定义itemView中的ImageView和TextView的行为.
		可以将itemView理解为ImageView和TextView的父控件*/
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			//如果未选择或者mList_Results为空,直接跳出，避免NullPointException
			if (position < 0 || mList_Results == null) {
				return null;
			}

			ScanResult ap = mList_Results.get(position);

			ImageView iv_rssi;
			TextView tv_ssid;
			TextView tv_desc;

			isWifiConnected = false;

			if (convertView == null) {
				convertView = LayoutInflater.from(mmContext).inflate(R.layout.listitem_wifiap, null);
			}

			iv_rssi = ((ImageView) convertView.findViewById(R.id.wifiap_item_iv_rssi));
			tv_ssid = ((TextView) convertView.findViewById(R.id.wifiap_item_tv_ssid));
			tv_desc = ((TextView) convertView.findViewById(R.id.wifiap_item_tv_desc));

			if (isWifiConnected() && ap.BSSID.equals(getBSSID())) {
				isWifiConnected = true;
			}

			/*SSID*/
			tv_ssid.setText(ap.SSID);
			/*状态*/
			tv_desc.setText(getDescription(ap));
			/*图标*/
			Picasso.with(mmContext).load(getSignalLevelImgId(ap)).into(iv_rssi);

			return convertView;
		}

		/**
		 * 根据具体的信号强度获取图标资源文件
		 */
		private int getSignalLevelImgId(ScanResult ap) {
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

		/*获取从服务器那边得到的推荐等级（“优选” “传统”）*/
		private String getRecommendLevel(ScanResult ap) {

			//TODO 给每个扫描到的AP一个标示
			return null;
		}

		/**
		 * 根据服务器端推送过来的消息来决定在界面显示“Selected（精选）”还是"Traditional（传统）"
		 */

		/**
		 * 判断状态：Connected/Open/Secured*/
		private String getDescription(ScanResult ap) {
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


	// list中每一项的“单击”监听器
	private OnItemClickListener mItemOnClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			//先得确定WIFI信号是否是连接上的，不然弹出对话框没有意义
			if (isWifiConnected()) {

				// 哦，其实开始就获取了 list_ScanResults这个List，而在这里把具体的ScanResult找出来
				final ScanResult result = mList_Results.get(position);
				startFloatingActivity(mContext, result);
			} else {
				//若WIFI并没有连接，则提示用户WIFI网络断了
				Toast.makeText(mContext, NOTICE_CHECK_NETWORK ,Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void startFloatingActivity(final Context context,
									   final ScanResult scanResult_hotspot) {

		Intent intent = new Intent(context, FloatingActivity.class);
		intent.putExtra(parce_ScanResult, scanResult_hotspot);
		context.startActivity(intent);
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

				mi.setEnabled(false);

				//将热点信息发送到服务器（socket的另一端）
				if (isOnline()){
					Log.d(TAG, NETWORK_OK);
					//先得到配置文件里的IP和PORT
					initPrefs();
					//开启新线程（建立socket）
					startNewThread();
					sendToServer();
					//结束之后将菜单选项设置为不可用
					mi.setEnabled(true);

				}
				break;


			case R.id.action_stop_sync:
				mi.setEnabled(false);
				if (isOnline()) {
					Log.d(TAG, NETWORK_OK);

					stopSync();
					//结束之后将菜单选项设置为可用
					mi.setEnabled(true);

				}
				break;

			// set server的过程很重要，因为这一步会开启新线程
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

		// 设置服务器的IP和port
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

		if(mThread != null){
			//再关闭mClientThread
			mThread.interrupt();
		}

		Log.d("WifiScanActivity", "Sync stopped");
		Toast.makeText(mContext, R.string.sync_stopped,  Toast.LENGTH_SHORT).show();
	}

}
