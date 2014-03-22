package project.chameleon.lite;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TimeInterval {

	Context context;
	private long smstime;
	private long logtime;
	private boolean state;
	
	TimeInterval(Context ctx){
		this.context = ctx;
		
		//retrieve values from preferences
		SharedPreferences getData = PreferenceManager.getDefaultSharedPreferences(context);
		String values = getData.getString("log", "2");
		
		if(values.equals("1")){
			setLogTime(1 * 1000);
		} else if(values.equals("2")){
			setLogTime(300 * 1000);
		} else if(values.equals("3")){
			setLogTime(1800 * 1000);
		} else if(values.equals("4")){
			setLogTime(3600 * 1000);
		} else if(values.equals("5")){
			setLogTime(86400 * 1000);
		} else if(values.equals("6")){
			setLogTime(604800 * 1000);
		} else {
			setLogTime(2419200 * 1000);
		}
		
		//deactivate the app is the checkbox is checked
		state = getData.getBoolean("state", false);
	}
	
	// sets time
		public void setTime(long _time) {
			this.smstime = _time;
		}

		// gets time
		public long getTime() {
			return this.smstime;
		}
	
	public void setLogTime(long _time){
		this.logtime = _time;
	}
	
	public long getLogTime(){
		return this.logtime;
	}
	
	public boolean setState(boolean state){
		return this.state;
	}
	
	public boolean getState(){
		return this.state;
	}
}
