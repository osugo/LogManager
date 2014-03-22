package project.chameleon.lite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleted extends BroadcastReceiver{

	//called when boot is completed
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//double chcek here for only the boot complete event
		if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
			//here we start the service
			Intent serviceIntent = new Intent(context, MyService.class);
			context.startService(serviceIntent);
		}
	}

}
