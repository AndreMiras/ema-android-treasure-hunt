package SQLiteDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteDatabase extends SQLiteOpenHelper{
	
	private static final String ENIGMA_TABLE 		= "enigma_table" ;	
	private static final String ENIGMA_COL_ID 		= "ID"           ;
	private static final String ENIGMA_COL_SOLUTION = "Solution"     ;	
	
	private static final String CLUES_TABLE 		= "clues_table"  ;
	private static final String CLUES_COL_ID 		= "ID"           ;
	private static final String CLUES_COL_CLUE 		= "Clue"         ;
	private static final String CLUES_COL_ENIGMA 	= "Enigma_id"    ;
	
	
	private static final String CREATE_TABLE_ENIGMA = 	"CREATE TABLE "     + ENIGMA_TABLE                           + " (" +
														ENIGMA_COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
														ENIGMA_COL_SOLUTION + " TEXT NOT NULL);";
	
	private static final String CREATE_TABLE_CLUES 	= 	"CREATE TABLE "     + CLUES_TABLE                            + " (" +
														CLUES_COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
														CLUES_COL_CLUE      + " TEXT NOT NULL, "                     +
														CLUES_COL_ENIGMA    + " INTEGER);"                           ;
	
	
	private static final String DELETE_TABLE_ENIGMA = 	"DROP TABLE " + ENIGMA_TABLE + ";" ;
	private static final String DELETE_TABLE_CLUES  =	"DROP TABLE " + CLUES_TABLE  + ";" ;
	

	public MySQLiteDatabase(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);	
	}

	@Override
	public void onCreate(SQLiteDatabase db) {		
		db.execSQL(CREATE_TABLE_ENIGMA);
		db.execSQL(CREATE_TABLE_CLUES );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DELETE_TABLE_ENIGMA);
		db.execSQL(DELETE_TABLE_CLUES);
		onCreate(db);
	}

}
