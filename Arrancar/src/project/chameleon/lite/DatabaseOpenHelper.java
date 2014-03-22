package project.chameleon.lite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public static final String KEY_NAME = "fname";
	public static final String KEY_NUMBER = "num";
	public static final String KEY_ALTNAME = "sname";
	public static final String KEY_ALTNUMBER = "altnum";

	public DatabaseOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String createQuery = "CREATE TABLE contact "
				+ " (_id integer primary key autoincrement, " + KEY_NAME
				+ " TEXT NOT NULL, " + KEY_NUMBER + " TEXT NOT NULL, "
				+ KEY_ALTNAME + " TEXT NOT NULL, " + KEY_ALTNUMBER
				+ " TEXT NOT NULL );";
		db.execSQL(createQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE contact");
		onCreate(db);
	}

}
