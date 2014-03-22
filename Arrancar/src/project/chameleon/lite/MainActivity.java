package project.chameleon.lite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import project.chameleon.lite.Contacts.OnContactClickedListener;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends ActionBarActivity implements OnContactClickedListener{

	int mPosition = -1;
	String mTitle = "";
	
	//array of strings storing menu item names
	String [] menuitem;
	
	//array of integers points to images stored in res/drawable-hdpi
	int[] icons = new int[] {R.drawable.contacts, R.drawable.feedback, R.drawable.about};
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private List<HashMap<String, String>> mList;
	private SimpleAdapter mAdapter;
	final private String MENUITEM = "menuitem";
	final private String ICON = "icon";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//getting array of menu item names
		menuitem = getResources().getStringArray(R.array.nav_drawer_items);
		
		//title of the activity
		mTitle = (String) getTitle();
		
		//getting a reference to the drawer listview
		mDrawerList = (ListView)findViewById(R.id.list_slidermenu);
		
		//each row in the list stores the menu item name and icon
		mList = new ArrayList<HashMap<String, String>>();
		for(int i = 0; i <3; i++){
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put(MENUITEM, menuitem[i]);
			hm.put(ICON, Integer.toString(icons[i]));
			mList.add(hm);
		}
		
		//keys used in hashmap
		String [] from = {ICON, MENUITEM};
		
		//ids of views in listview layout
		int[] to = {R.id.icon, R.id.title};
		
		//instantiating the adapter to store each item
		mAdapter = new SimpleAdapter(this, mList, R.layout.drawer_list_item, from,  to);
		
		//geting reference to DrawerLayout
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		//creating a togglebutton for the navigation drawer event listener
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
			
			//called when the navigation drawer is open
			public void onDrawerOpened(View view){
				getSupportActionBar().setTitle("Chameleon");
				supportInvalidateOptionsMenu();
			}
			
			//called when the navigation drawer is closed
			public void onDrawerClosed(View drawerView){
				int selectedItem = mDrawerList.getCheckedItemPosition();
				mPosition = selectedItem;
				
				if(mPosition != -1){
					getSupportActionBar().setTitle(menuitem[mPosition]);
				}
				supportInvalidateOptionsMenu();
			}
		};
		
		//setting the eventlistener for the drawer
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		//click listener for the navigation drawer
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
		
		//enabling up navigation
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		//setting the app logo
		getSupportActionBar().setLogo(R.drawable.app_logo);
		
		//setting the adapter to the listview
		mDrawerList.setAdapter(mAdapter);
		
		if(savedInstanceState == null){
			//on first time display view for the first nav item
			displayView(0);
		}
	}
	
	private class SlideMenuClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			displayView(position);
		}
		
	}
	
	//displaying fragment view for the selected nav drawer item
	private void displayView(int position){
		//update the main content by replacing fragments
		Fragment fragment = null;
		switch(position){
		case 0 :
			fragment = new Contacts();
			break;
		case 1 :
			fragment = new Feedback();
			break;
		case 2 : 
			fragment = new About();
			break;
			default : 
				break;
		}
		
		if(fragment != null){
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
			
			//update selected item and title the close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(menuitem[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			//error creating fragment
			Log.e("Main Activity", "Error in creating fragment");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(mDrawerToggle.onOptionsItemSelected(item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		//if nav drawer is open, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		//pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onContactClicked(long id) {
		// TODO Auto-generated method stub
		ViewContact viewContact = new ViewContact();
		
		Bundle bundle = new Bundle();
		bundle.putLong("ROW_ID", id);
		viewContact.setArguments(bundle);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.frame_container, viewContact);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//model the click for the button according to the contact interface
}
