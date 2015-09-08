package com.eldimentio.rpgstory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDatabase extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
    public static final String[] CLASS_NAMES = {"Healer","Fighter","Knight","Barbarian","Thief","Cleric"};
    public static final String[] STATUS_NAMES = {"Healthy","Poisoned","Paralyzed","Severely Poisoned","Burned"};
    private static final String CREATE_USER = "CREATE TABLE userdata (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, class INTEGER, spells TEXT, exp LONG DEFAULT 0, gold LONG DEFAULT 0, curroom INTEGER DEFAULT 0, curhp INTEGER DEFAULT 20, curmp INTEGER DEFAULT 15, status INTEGER DEFAULT 0);";
    private static final String CREATE_ITEMS = "CREATE TABLE itemdata (id INTEGER PRIMARY KEY, owner INTEGER, quantity INTEGER);";
 //   private static final String CREATE_MONSTERS = "CREATE TABLE monsters (id INTEGER PRIMARY KEY, name TEXT, baseHP INTEGER, baseATK INTEGER, baseDEF INTEGER, baseSATK INTEGER, baseSDEF INTEGER, baseSPD INTEGER, exp INTEGER, gold INTEGER);";
    GameDatabase(Context context){
    	super(context, "gamedata", null, DATABASE_VERSION);
    }
    
    //Status: 0 = none; 1 = poisoned; 2 = paralyzed; 3 = burned; 4 severely poisoned; 
    
    @Override
    public void onCreate(SQLiteDatabase db){
    	db.execSQL(CREATE_USER);
    	db.execSQL(CREATE_ITEMS);
   // 	db.execSQL(CREATE_MONSTERS);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    	db.execSQL("DROP TABLE IF EXISTS userdata");
    	db.execSQL("DROP TABLE IF EXISTS itemdata");
 //   	db.execSQL("DROP TABLE IF EXISTS monsters");
    	onCreate(db);
    }
    
    public void restart(SQLiteDatabase db){
    	db.execSQL("DROP TABLE IF EXISTS userdata");
    	db.execSQL("DROP TABLE IF EXISTS itemdata");
    	db.execSQL("DROP TABLE IF EXISTS monsters");
    	db.execSQL(CREATE_USER);
    	db.execSQL(CREATE_ITEMS);
 //   	db.execSQL(CREATE_MONSTERS);
    }
}
