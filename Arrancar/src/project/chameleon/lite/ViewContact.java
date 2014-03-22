package project.chameleon.lite;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor")
public class ViewContact extends Fragment{

	private long rowID;
	private EditText fnameEt, snameEt, numEt, aliasEt;
	private Button alias, phonebook;
	DatabaseConnector connect;
	private final int PICK = 10;
	private final int ADD = 100;
	private ProgressDialog progress;
	
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
		View view = inflater.inflate(R.layout.addcontact, container, false);
		
		fnameEt = (EditText) view.findViewById(R.id.fnameEdit);
		numEt = (EditText) view.findViewById(R.id.numEdit);
		snameEt = (EditText) view.findViewById(R.id.snameEdit);
		aliasEt = (EditText) view.findViewById(R.id.aliasNum);
		phonebook = (Button) view.findViewById(R.id.from_phonebook);
		alias = (Button) view.findViewById(R.id.pbook);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		connect = new DatabaseConnector(getActivity());
		connect.open();
		
		if(savedInstanceState == null){
			rowID = getArguments().getLong("ROW_ID");
		} else {
			rowID = savedInstanceState.getLong("ROW_ID");
		}
		
		phonebook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, ADD);
			}
		});
		
		alias.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK){
			
			Uri contact = data.getData();
			ContentResolver cr = getActivity().getContentResolver();
			Cursor c = getActivity().managedQuery(contact, null, null, null, null);
			
			while (c.moveToNext()){
				String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
				String cname = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				
				if(Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0){
					Cursor pCur = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					
					while (pCur.moveToNext()){
						String cnumber = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
						
						switch(requestCode){
						case ADD:
							fnameEt.setText(cname);
							numEt.setText(cnumber);
							break;
						case PICK:
							snameEt.setText(cname);
							aliasEt.setText(cnumber);
						}
					}
				}
			}			
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putLong("ROW_ID", rowID);
	}
	
	private class LoadContacts extends AsyncTask<Long, Object, Cursor>{
		
		DatabaseConnector dbConnector = new DatabaseConnector(getActivity());

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progress = new ProgressDialog(getActivity());
			progress.setMessage("Loading details...");
			progress.setIndeterminate(false);
			progress.setCancelable(false);
			progress.show();
		}
		
		@Override
		protected Cursor doInBackground(Long... params) {
			// TODO Auto-generated method stub
			dbConnector.open();
			return dbConnector.getOneContact(params[0]);
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progress.dismiss();
			
			result.moveToFirst();
			int nameIndex = result.getColumnIndex("fname");
			int capIndex = result.getColumnIndex("sname");
			int codeIndex = result.getColumnIndex("num");
			int aliasIndex = result.getColumnIndex("altnum");
			
			fnameEt.setText(result.getString(nameIndex));
			snameEt.setText(result.getString(capIndex));
			numEt.setText(result.getString(codeIndex));
			aliasEt.setText(result.getString(aliasIndex));
			
			result.close();
			dbConnector.close();
			
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new LoadContacts().execute(rowID);
	}
	
	private void saveContact(){
		DatabaseConnector dbConnector = new DatabaseConnector(getActivity());
		
		if (getArguments() == null) {
			dbConnector.insertContact(fnameEt.getText().toString(), numEt
					.getText().toString(), snameEt.getText().toString(),
					aliasEt.getText().toString());
			createContact(fnameEt.getText().toString(), numEt.getText().toString());

		} else {
			dbConnector.updateContact(rowID, fnameEt.getText().toString(),
					numEt.getText().toString(), snameEt.getText().toString(),
					aliasEt.getText().toString());
			createContact(fnameEt.getText().toString(), numEt.getText().toString());
			}
		}
	
	private void deleteContact(){
		final Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(R.layout.dialog_box);
		dialog.setTitle("Confirm Action");
		
		TextView message = (TextView) dialog.findViewById(R.id.body);
		message.setText("This will permanently remove the entry!");
		message.setBackgroundColor(R.color.ThemeBlue);
		
		Button remover = (Button) dialog.findViewById(R.id.away);
		Button hide = (Button) dialog.findViewById(R.id.close);
		
		remover.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final DatabaseConnector dbConnector = new DatabaseConnector(getActivity());
				
				AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Long, Object, Object>(){
					@Override
					protected Object doInBackground(Long... params) {
						// TODO Auto-generated method stub
						dbConnector.deleteContact(params[0]);
						return null;
					}
					
					@Override
					protected void onPostExecute(Object result) {
						Toast.makeText(getActivity(), "Contact deleted", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				};
				deleteTask.execute(new Long[] { rowID } );
			}
		});
		
		hide.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	//check if contact exists in the phonebook and adds it if it doesnt exist
	private void createContact(String name, String phone){
		//check for error source
		ContentResolver cr = getActivity().getContentResolver();

		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String existName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (existName.contains(name)) {
					// the number will not be added if it already exists in the
					// phone book
					return;
				}
			}
		}

		// add the contact to phone book if it doesnt exist
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
						"accountname@gmail.com")
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
						"com.google").build());
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
				.withValue(
						ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
						name).build());
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
						ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
				.build());

		try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.view_contact, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.newcontact:
			
			return true;
		case R.id.save:
			saveContact();
			return true;
		case R.id.delete:
			deleteContact();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		connect.close();
	}
}

