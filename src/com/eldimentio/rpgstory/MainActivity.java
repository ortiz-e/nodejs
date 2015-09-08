package com.eldimentio.rpgstory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		newgamelistener();
		loadgamelistener();
		delgamelistener();
	}
	
	public void newgame_intent(){
		Intent intent = new Intent(this, NewGame.class);
		startActivity(intent);
	}
	
	public void newgamelistener(){
		Button b = (Button)findViewById(R.id.explore_button);
		b.setOnClickListener(new OnClickListener(){	
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			newgame_intent();
		}
			
		});
	}
	
	public void loadgame_intent(){
		Intent intent = new Intent(this, ChooseGame.class);
		startActivity(intent);
	}
	
	public void loadgamelistener(){
		Button b = (Button)findViewById(R.id.rpg_status_button);
		b.setOnClickListener(new OnClickListener(){	
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			loadgame_intent();
		}
			
		});
	}
	
	public void delgame(){
		final GameDatabase db = new GameDatabase(this);
		SQLiteDatabase updater = db.getWritableDatabase();
		db.restart(updater);
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle("Success!");
		d.setMessage("The database has been reinstalled.");
		d.show();
	}
	
	public void delgamelistener(){
		Button b = (Button)findViewById(R.id.b_east);
		b.setOnClickListener(new OnClickListener(){	
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			delgame();
		}
			
		});
	}
	
}
