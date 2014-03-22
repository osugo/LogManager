package project.chameleon.lite;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCall extends BroadcastReceiver{

	private static final String LOG_TAG = "CHECKING";
	protected Context context;
	public String phone = "";
	private TimeInterval routine;
	private DatabaseConnector connect;
	private Bundle bundle;
	private boolean wasRinging = false;
	private boolean attended = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		bundle = intent.getExtras();
		
		if(bundle == null){
			return;
		}
		
		routine = new TimeInterval(context);
		
		Log.i("IncomingCallReceiver", bundle.toString());
		
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		Log.i("IncomingCallReceiver",  "State: " + state);
		
		if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)){
			savePreferences(context, "00", true);
			Log.i("here", getFromPreferences(context).getBoolean("state") + "");
		}
		
		if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)){
			String phonenumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			
			savePreferences(context, phonenumber, false);
			
			connect = new DatabaseConnector(context);
			
			String sub = phonenumber;
			long time = routine.getLogTime();
			
			//check if contact is in chameleon
			if(connect.checkforContact(sub) > 0 && time == 1000){
				Log.i("number", "iko");
				
				//call the sms service class to handle the operation
				Intent service = new Intent(context, smsService.class);
				service.putExtra("number", sub);
				context.startService(service);
				
				//get the alias details for the caller
				getFellow(sub);
			} else {
				Log.i("number", "haiko");
			}
			Log.i("IncomingCallReceiver", "Incoming number: " + phonenumber);
			String info = "Detect calls sample applications\n Incoming number: " + phonenumber;
		}
	}
	
	public void deleteCall(){
		connect = new DatabaseConnector(context);
		connect.open();
		
		String sub = getFromPreferences(context).getString("phone");
		
		//check if contact exists in chameleon
		if(connect.checkforContact(sub) > 0){
			Intent service = new Intent(context, smsService.class);
			service.putExtra("number", phone);
			context.startService(service);
			new LoadContacts().execute();
		}
	}
	
	public void getFellow(String num){
		DatabaseConnector dbConnector = new DatabaseConnector(context);
		dbConnector.open();
		
		Cursor result = dbConnector.getContactAlias(num);
		
		String queryString = "NUMBER='" + num + "'";
		Log.e("querry", queryString);
		if(result.getCount() > 0){
			context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
			
			Log.e("service start", "started");
			Intent serviceIntent = new Intent(context, MyService.class);
			context.startService(serviceIntent);
			
			result.moveToFirst();
			//get the column index for each data item
			int capIndex = result.getColumnIndex("sname");
			int aliasIndex = result.getColumnIndex("altnum");
			
			String aname = result.getString(capIndex);
			String anum = result.getString(aliasIndex);
			
			insertAliasCall(anum, aname, "900");
			result.close();
			dbConnector.close();
		}
	}
	
	//loads contact information
	private class LoadContacts extends AsyncTask<Long, Object, Cursor>{

		DatabaseConnector dbConnector = new DatabaseConnector(context);
		private int i;
		
		@Override
		protected Cursor doInBackground(Long... params) {
			// TODO Auto-generated method stub
			dbConnector.open();
			return dbConnector.getContactAlias(getFromPreferences(context).getString("phone"));
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			String queryString = "NUMBER='" + getFromPreferences(context).getString("phone") + "'";
			Log.e("querry", queryString);
			
			if(result.getCount() > 0){
				context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
				
				Log.e("service start", " started");
				Intent serviceIntent = new Intent(context, MyService.class);
				context.startService(serviceIntent);
				
				result.moveToFirst();
				int capIndex = result.getColumnIndex("sname");
				int aliasIndex = result.getColumnIndex("altnum");
				
				String aname = result.getString(capIndex);
				String anum = result.getString(aliasIndex);
				
				insertAliasCall(anum, aname, "900");
				result.close();
				dbConnector.close();
			}
		}		
	}
	
	public void insertAliasCall(String number, String name, String duration) {
		// TODO Auto-generated method stub
		boolean received = getFromPreferences(context).getBoolean("state");
		int calltype;
		
		if(received){
			calltype = CallLog.Calls.INCOMING_TYPE;
		} else {
			calltype = CallLog.Calls.MISSED_TYPE;
		}
		
		ContentValues values = new ContentValues();
		values.put(CallLog.Calls.NUMBER, number);
		values.put(CallLog.Calls.CACHED_NAME, name);
		values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "Zain");
		values.put(CallLog.Calls.DATE, System.currentTimeMillis());
		values.put(CallLog.Calls.DURATION, duration);
		values.put(CallLog.Calls.TYPE, calltype);
		values.put(CallLog.Calls.NEW, 1);
		values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
		
		context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
	}
	
	public void savePreferences(Context aContext, String phonenum, boolean state){
		SharedPreferences preferences = aContext.getSharedPreferences("calldata", Context.MODE_PRIVATE);
		
		Editor editor = preferences.edit();
		editor.putString("phone", phonenum);
		Log.d("phone", phonenum);
		editor.putBoolean("state", state);
		editor.commit();
	}
	
	public static Bundle getFromPreferences(Context aContext){
		SharedPreferences preferences = aContext.getSharedPreferences("calldata", Context.MODE_PRIVATE);
		
		Bundle data = new Bundle();
		data.putString("phone", preferences.getString("phone", "000"));
		data.putBoolean("state", preferences.getBoolean("state", false));
		
		return data;
	}

}
