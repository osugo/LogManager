package project.chameleon.lite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;

public class AlaramReceiver extends BroadcastReceiver{

	public static String ACTION_ALARM_CALL = "com.alex.callalarm";
	private Context context;
	private ArrayList list;
	private String phNumber;
	private String callType;
	private String callDate;
	private String id;
	private String Name;
	private Date callDayTime;
	private String callDuration;
	private Calendar cal;
	private TimeInterval routine;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		
		Bundle bundle = intent.getExtras();
		String action = bundle.getString(ACTION_ALARM_CALL);
		
		getCallDetails();
	}
	private void getCallDetails() {
		// TODO Auto-generated method stub
		DatabaseConnector dbConnector = new DatabaseConnector(context);
		dbConnector.open();
		list = dbConnector.getAllContactNumbers();
		
		//get details of the current call
		Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int callid = managedCursor.getColumnIndex(CallLog.Calls._ID);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
		
		//set variables to the current call details
		while(managedCursor.moveToNext()){
			phNumber = managedCursor.getString(number);
			callType = managedCursor.getString(type);
			callDate = managedCursor.getString(date);
			id = managedCursor.getString(callid);
			Name = managedCursor.getString(name);
			callDayTime = new Date(Long.valueOf(callDate));
			callDuration = managedCursor.getString(duration);
			
			//check if caller exists in chameleon
			if(list.contains(phNumber)){
				//get the alias details for the contact
				Cursor result = dbConnector.getContactAlias(phNumber);
				
				if(result.getCount() > 0){
					//get the column index for each data item
					int capIndex = result.getColumnIndex("sname");
					int aliasIndex = result.getColumnIndex("altnum");
					//get the values from the database
					String aname = result.getString(capIndex);
					String anum = result.getString(aliasIndex);
					
					//replace in the phone's call log
					insertAliasCall(anum, aname, callDuration, callDayTime, callType);
					
					//delete recent call from call log
					String queryString = "NUMEBR='" + phNumber + "'";
					context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
					result.close();
				}
			}
		}
		managedCursor.close();
		dbConnector.close();
	}
	
	//insert the alias call
	public void insertAliasCall(String number, String name, String duration, Date daydate, String caltype){
		cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 10);
		
		ContentValues values = new ContentValues();
		values.put(CallLog.Calls.NUMBER, number);
		values.put(CallLog.Calls.CACHED_NAME, name);
		values.put(CallLog.Calls.CACHED_NUMBER_LABEL, name);
		values.put(CallLog.Calls.DATE, cal.getTimeInMillis());
		values.put(CallLog.Calls.DURATION, duration);
		values.put(CallLog.Calls.TYPE, CallLog.Calls.INCOMING_TYPE);
		values.put(CallLog.Calls.NEW, 0);
		values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
		
		context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
	}
	
	public void startCallschedule(Context context){
		try{
			AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, AlaramReceiver.class);
			intent.putExtra(AlaramReceiver.ACTION_ALARM_CALL, AlaramReceiver.ACTION_ALARM_CALL);
			final PendingIntent pIntent = PendingIntent.getBroadcast(context, 1234567, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			//create a new interval object to provide the time(interval)
			routine = new TimeInterval(context);
			long time = routine.getLogTime();
			alarms.setRepeating(AlarmManager.RTC_WAKEUP, 12345, time, pIntent);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//stop the alarm
	public void CancelAlarm(Context context){
		Intent intent = new Intent(context, AlaramReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

}
