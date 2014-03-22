package project.chameleon.lite;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
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

public class AddContact extends Fragment {

	private long rowID;
	private EditText fnameEt, snameEt, numEt, aliasEt;
	private Button phonebook, phonebook_contact;
	
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
		snameEt = (EditText) view.findViewById(R.id.snameEdit);
		numEt = (EditText) view.findViewById(R.id.numEdit);
		aliasEt = (EditText) view.findViewById(R.id.aliasNum);
		phonebook_contact = (Button) view.findViewById(R.id.from_phonebook);
		phonebook = (Button) view.findViewById(R.id.pbook);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null) {
			rowID = getArguments().getLong("row_id");
			fnameEt.setText(getArguments().getString("fname"));
			snameEt.setText(getArguments().getString("num"));
			numEt.setText(getArguments().getString("altnum"));
		}

		phonebook.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, 100);
			}
		});

		phonebook_contact.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, 101);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {

			Uri contact = data.getData();
			ContentResolver cr = getActivity().getContentResolver();
			Cursor c = getActivity().managedQuery(contact, null, null, null,
					null);

			while (c.moveToNext()) {

				String id = c.getString(c
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = c
						.getString(c
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (Integer
						.parseInt(c.getString(c
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

					Cursor pCur = cr.query(Phone.CONTENT_URI, null,
							Phone.CONTACT_ID + " = ?", new String[] { id },
							null);

					if (requestCode == 100) {
						while (pCur.moveToNext()) {
							String phone = pCur.getString(pCur
									.getColumnIndex(Phone.NUMBER));
							snameEt.setText(name);
							aliasEt.setText(phone.replace(" ", "").replace("-",
									""));
						}
					} else {
						while (pCur.moveToNext()) {
							String phone = pCur.getString(pCur
									.getColumnIndex(Phone.NUMBER));
							fnameEt.setText(name);
							numEt.setText(phone.replace(" ", "").replace("-",
									""));
						}
					}
				}
			}
		}
	}

	private void saveContact() {
		DatabaseConnector dbConnector = new DatabaseConnector(getActivity());

		if (getArguments() == null) {
			dbConnector.insertContact(fnameEt.getText().toString(), numEt
					.getText().toString(), snameEt.getText().toString(),
					aliasEt.getText().toString());
			createContact(fnameEt.getText().toString(), numEt.getText()
					.toString());
		} else {
			dbConnector.updateContact(rowID, fnameEt.getText().toString(),
					numEt.getText().toString(), snameEt.getText().toString(),
					aliasEt.getText().toString());
			createContact(fnameEt.getText().toString(), numEt.getText()
					.toString());
		}
	}

	private void createContact(String name, String phone) {
		// TODO Auto-generated method stub
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String existName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (existName.contains(name)) {
					// contact will not be added if it already exists in the
					// phonebook
					return;
				}
			}
		}

		// add the contact to the phonebook if it doesnt already exist
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
		inflater.inflate(R.menu.addcontact, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.save:
			saveContact();
			return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
