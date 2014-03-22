package project.chameleon.lite;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.util.Log;

public class OutgoingCallReceiver extends BroadcastReceiver{

	private DatabaseConnector connect;
	private TimeInterval routine;
	private Context context;
	private String phone;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		
		phone = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		Log.d("phone", phone);
		
		routine = new TimeInterval(context);
		long time = routine.getLogTime();
		boolean state = routine.getState();
		Log.e("state", state + "");
		
		//change accordingly if delete time should vary
		if(time == 1000){
			deleteCall();
		}
	}

	public void deleteCall() {
		// TODO Auto-generated method stub
		connect = new DatabaseConnector(context);
		connect.open();
		
		String sub = phone;
		
		if(connect.checkforContact(sub) > 0){
			Intent service = new Intent(context, outgoingService.class);
			service.putExtra("number", sub);
			context.startService(service);
			
			new LoadContacts().execute();
		}
	}
	
	private class LoadContacts extends AsyncTask<Long, Object, Cursor>{
		
		DatabaseConnector dbConnector = new DatabaseConnector(context);
		
		@Override
		protected Cursor doInBackground(Long... arg0) {
			// TODO Auto-generated method stub
			dbConnector.open();
			return dbConnector.getContactAlias(phone);
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			String queryString = "NUMBER='" + phone + "'";
			Log.e("querry", queryString);
			
			if(result.getCount() > 0){
				context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
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

		private void insertAliasCall(String number, String name, String duration) {
			// TODO Auto-generated method stub
			int calltype;
			calltype = CallLog.Calls.OUTGOING_TYPE;
			
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
	}
}
