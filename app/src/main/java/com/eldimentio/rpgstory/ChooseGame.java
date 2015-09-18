package com.eldimentio.rpgstory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChooseGame extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_game);
		try {
			final GameDatabase db = new GameDatabase(this);
			SQLiteDatabase reader = db.getReadableDatabase();
			Cursor data = reader.query("userdata", new String[]{"name","curroom"}, null, null, null, null, "id");
	        data.moveToFirst();
			List<String> content = new ArrayList<String>();
			content.add(data.getString(data.getColumnIndex("name")) + " --- " + data.getString(data.getColumnIndex("curroom")));
			while(data.moveToNext()){
				content.add(data.getString(data.getColumnIndex("name")) + " --- " + data.getString(data.getColumnIndex("curroom")));
			}
			data.close();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listviewrow, content);
			ListView l = (ListView)findViewById(R.id.userslist);
			l.setAdapter(adapter);
			l.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					// TODO Auto-generated method stub
					loadGame(position);
				}
				
			});
				
		}
		catch(Exception e){
			AlertDialog.Builder d = new AlertDialog.Builder(this);
			d.setTitle("Error");
			d.setMessage("No users found! Please make a new game");
			d.show();
		}
	}
	
	public void loadGame(int pos){
		Intent intent = new Intent(this, GameEngine.class);
		intent.putExtra("id", pos+1);
		startActivity(intent);
	}
}
