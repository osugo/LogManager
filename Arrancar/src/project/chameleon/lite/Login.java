package project.chameleon.lite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity{

	private EditText create, confirm;
	private Button enter;
	private LoginDatabaseAdapter loginDatabaseAdapter;
	private int count = 0;
	private DatabaseConnector databaseConnector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		//set app title font
		Typeface name = Typeface.createFromAsset(getAssets(), "fonts/Eutemia.ttf");
		TextView app = (TextView)findViewById(R.id.appTitle);
		app.setTypeface(name);
		
		//create a new database object and open it
		loginDatabaseAdapter = new LoginDatabaseAdapter(this);
		databaseConnector = new DatabaseConnector(this);
		databaseConnector.open();
		
		setUpViews();
		
		//check if an account has been created
		if(loginDatabaseAdapter.getCount() == 0){
			enter.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					signUp();
				}
			});
		} else {
			//hide password confirm fields
			create.setVisibility(View.GONE);
			
			//change display text of button and textview
			confirm.setHint("Enter password:");
			enter.setText("SIGN IN");
			
			enter.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					login();
				}
			});
		}
	}
	private void setUpViews(){
		create = (EditText) findViewById(R.id.createPassword);
		confirm = (EditText)findViewById(R.id.confirmPassword);
		enter = (Button) findViewById(R.id.login);
	}
	
	protected void signUp(){
		String password = create.getText().toString();
		String confirmPassword = confirm.getText().toString();
		
		//check if any of the fields are vacant
		if(password.equals("") || confirmPassword.equals("")){
			Toast.makeText(getApplicationContext(), "Field vacant", Toast.LENGTH_LONG).show();
			return;
		}
		
		//check if both passwords match
		if(!password.equals(confirmPassword)){
			Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
			return;
		} else {
			//save the data in the db
			loginDatabaseAdapter.open();
			loginDatabaseAdapter.insertEntry(password);
			Toast.makeText(getApplicationContext(), "Password created successfully", Toast.LENGTH_SHORT).show();
			
			startActivity(new Intent(Login.this, MainActivity.class));
		}
	}
	private void login(){
		//get entered password
		String password = confirm.getText().toString();
		
		//retrieve the password from the database
		loginDatabaseAdapter.open();
		String storedPassword = loginDatabaseAdapter.getSingleEntry(password);
		
		//compares stored password and allows login if they match
		if(password.equals(storedPassword)){
			startActivity(new Intent(Login.this, MainActivity.class));
		} else {
			Toast.makeText(Login.this, "Incorrect password", Toast.LENGTH_SHORT).show();
			count++;
			
			if(count == 3){
				System.exit(0);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		loginDatabaseAdapter.close();
		databaseConnector.close();
	}
}
