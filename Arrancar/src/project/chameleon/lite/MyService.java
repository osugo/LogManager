package project.chameleon.lite;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Service to handle the alarms in the background
 * @author Root
 *
 */
public class MyService extends Service{

	AlarmReceiver smsalarm;
	AlaramReceiver callalarm;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		smsalarm = new AlarmReceiver();
		callalarm = new AlaramReceiver();
		
		startRepeatingTimer();
	}

	private void startRepeatingTimer() {
		// TODO Auto-generated method stub
		Context context = this.getApplicationContext();
		
		if(smsalarm != null){
			callalarm.startCallschedule(context);
		} else {
			Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		cancelRepeatingTimer();
	}

	private void cancelRepeatingTimer() {
		// TODO Auto-generated method stub
		Context context = this.getApplicationContext();
		
		if(smsalarm != null){
			callalarm.CancelAlarm(context);
		} else {
			Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
		}
	}

}
