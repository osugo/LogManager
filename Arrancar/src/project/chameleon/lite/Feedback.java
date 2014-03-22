package project.chameleon.lite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class Feedback extends Fragment{

	private EditText recipient;
	private EditText subject;
	private EditText message;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.feedback, container, false);
		
		recipient  = (EditText)view.findViewById(R.id.recipient);
		recipient.setText("feedback@evansgikunda.com");
		subject = (EditText) view.findViewById(R.id.subject);
		message = (EditText) view.findViewById(R.id.content);
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.feedback, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.send:
			sendMail();
			return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void sendMail() {
		// TODO Auto-generated method stub
		String to = recipient.getText().toString();
		String about = subject.getText().toString();
		String content = message.getText().toString();
		
		//relay the info to the email client
		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
		email.putExtra(Intent.EXTRA_SUBJECT, about);
		email.putExtra(Intent.EXTRA_TEXT, content);
		
		email.setType("message/rfc822");
		//choose email client
		startActivity(Intent.createChooser(email, "Choose email client"));
	}
}
