package com.eldimentio.rpgstory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class NewGame extends Activity {

	Spinner spins;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game);
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, GameDatabase.CLASS_NAMES);
		 spins = (Spinner)findViewById(R.id.spinner1);
		spins.setAdapter(ad);
		submitlistener();
	}
	
	public void add_user(){
		final GameDatabase db = new GameDatabase(this);
		SQLiteDatabase updater = db.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", ((EditText)findViewById(R.id.editText1)).getText().toString());
		cv.put("class", spins.getSelectedItemId());
		cv.put("spells", "0,1,2,3,4"); //remove later
		updater.insert("userdata", null, cv);
		updater.close();
		Intent intent = new Intent(this, ChooseGame.class);
		startActivity(intent);
		
	}
	
	public void submitlistener(){
		Button b = (Button)findViewById(R.id.rpg_status_button);
		b.setOnClickListener(new OnClickListener(){	
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			add_user();
		}
			
		});
	}
}
