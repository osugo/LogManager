package project.chameleon.lite;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Contacts extends ListFragment{

	public static final String ROW_ID = "row_id";
	private ListView conListView;
	private CursorAdapter conAdapter;
	private ProgressDialog progress;
	private OnContactClickedListener contactListener;
	
	interface OnContactClickedListener {
		public void onContactClicked(long id);
	}
	
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
		View view = inflater.inflate(R.layout.contacts, container, false);
		
		conListView = (ListView) view.findViewById(android.R.id.list);
		
		//map each name to a textview
		String [] from = new String [] { DatabaseOpenHelper.KEY_NAME, DatabaseOpenHelper.KEY_NUMBER, DatabaseOpenHelper.KEY_ALTNAME, DatabaseOpenHelper.KEY_ALTNUMBER };
		int [] to = new int [] { R.id.actualName, R.id.actualNumber, R.id.aliasName, R.id.aliasNumber };
		
		conAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.contacts_row, null, from, to);
		setListAdapter(conAdapter);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		conListView.setOnItemClickListener(viewConListener);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new GetContacts().execute((Object[]) null);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		Cursor cursor = conAdapter.getCursor();
		
		if(cursor != null){
			cursor.deactivate();
		}
		
		conAdapter.changeCursor(null);
		super.onStop();
	}
	
	private class GetContacts extends AsyncTask<Object, Object, Cursor>{
		
		DatabaseConnector dbConnector = new DatabaseConnector(getActivity());

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progress = new ProgressDialog(getActivity());
			progress.setMessage("Fetching contacts...");
			progress.setIndeterminate(false);
			progress.setCancelable(false);
			progress.show();
		}
		@Override
		protected Cursor doInBackground(Object... params) {
			// TODO Auto-generated method stub
			dbConnector.open();
			return dbConnector.getAllContacts();
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			// TODO Auto-generated method stub
			progress.dismiss();
			conAdapter.changeCursor(result);
			dbConnector.close();
			
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		try {
			contactListener = (OnContactClickedListener)activity;
		} catch (ClassCastException exe){
			throw new ClassCastException("The activity must implement the OnContactClickedListener");
		}
	}
	
	OnItemClickListener viewConListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			contactListener.onContactClicked(arg3);
		}
		
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.contacts, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.add:
			
			return true;
			default: 
				return super.onOptionsItemSelected(item);
		}
	}
}
