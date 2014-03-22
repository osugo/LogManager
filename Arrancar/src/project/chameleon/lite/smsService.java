package project.chameleon.lite;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;
/**
 * Handles deletion of calls from the log
 * @author Root
 *
 */
public class smsService extends Service{

	private Timer timer = new Timer();
	private long TIMER_INTERVAL = 3 * 1000;
	private Uri deleteUri = Uri.parse("content://sms");
	private String smsNoToBeDeleted ="12345678";
	static int count = 0;
	Bundle extras;
	private String time = "";
	private String num = "";
	private String realtimestamp = "";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		extras = intent.getExtras();
		if(extras != null){
			time = extras.getString("time");
			num = extras.getString("number");
			startService();
		}
		return START_NOT_STICKY;
	}

	private void startService() {
		// TODO Auto-generated method stub
		TimerTask timerTask = new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.i("message", "Timer task executing");
				if(!num.equals("")){
					Log.e("delete call", num);
					deleteCall();
				}
			}		
		};
		timer.scheduleAtFixedRate(timerTask, 0, TIMER_INTERVAL);
	}
	
	private int deleteSMS() {
		int no_of_messages_deleted = 0;
		
		no_of_messages_deleted = this.getContentResolver().delete(deleteUri,
				"address=?", new String[] { num });

		return no_of_messages_deleted;
	}

	private int updateSMS() {

		int no_of_messages_updated = 0;

		try {
			ContentResolver resolver = getContentResolver();
			Cursor c = resolver.query(deleteUri, new String[] { "body" },
					"address=?", new String[] { smsNoToBeDeleted }, null);

			if (c.moveToFirst()) {

				do {
					String mbody = c.getString(0);
					if (!mbody.startsWith("#9999")) {

						ContentValues values = new ContentValues();
						values.put("body", "#9999message updated");

						no_of_messages_updated = this
								.getContentResolver()
								.update(deleteUri, values,
										"address=? and body=?",
										new String[] { smsNoToBeDeleted, mbody });
					}
				} while (c.moveToNext());
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return no_of_messages_updated;
	}

	private void notification() {
		try {
			File workingdir = new File("/data/data/com.android.smsDelete/");
			Process process = Runtime.getRuntime().exec("--help", null,
					workingdir);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()), 8 * 1024);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				Log.i("logcat", line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopping(){
		Log.i("service", "stopped");
		this.stopSelf();
	}
	
	public void deleteCall(){
		String queryString = "NUMBER='" + num + "'";
		int i = this.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
		Log.e("query", queryString);
		Log.e("deleted", i + "");
	}
}
