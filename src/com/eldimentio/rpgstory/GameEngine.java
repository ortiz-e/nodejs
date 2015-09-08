package com.eldimentio.rpgstory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class GameEngine extends Activity implements SurfaceHolder.Callback {
	
	Bundle b;
	Map<Integer, ArrayList<String>> cv = new HashMap<Integer,ArrayList<String>>();
	public static Map<Integer, ArrayList<String>> items_data = new HashMap<Integer,ArrayList<String>>();
	public static Map<Integer, ArrayList<String>> spells_data = new HashMap<Integer, ArrayList<String>>();
	Map<Integer, ArrayList<String>> monster_data = new HashMap<Integer,ArrayList<String>>();
	public ContentValues the_room_data = new ContentValues();
	public static String user_name;
	public static String TAG = "GameEngine";
	public ArrayList<String> map_tiles = new ArrayList<String>();
	public int userid;
	public int the_room;
	public int user_class;
	public int user_curhp;
	public int user_gold;
	public int user_exp;
	public int user_curmp;
	public int user_level;
	public int user_maxhp;
	public int user_maxmp;
	public int user_atk;
	public int user_def;
	public int user_spd;
	public int user_satk;
	public int user_sdef;
	public int user_status;
	public String[] user_spells;
	public String spelllist;
	public int baseHP = 100;
	public int baseMP = 80;
	public int baseATK = 80;
	public int baseDEF = 80;
	public int baseSPD = 80;
	public int baseSATK = 80;
	public int baseSDEF = 80;
	public double infHP = 1;
	public double infMP = 1;
	public double infATK = 1;
	public double infDEF = 1;
	public double infSPD = 1;
	public double infSATK = 1;
	public double infSDEF = 1;
	public int exp_for_next;
	public int awaiting_decision;
	public int next_room = 0;
	public int north = 0;
	public int south = 0;
	public int east = 0;
	public int west = 0;
	public int curid = 0;
	public boolean random_encounters;
	public boolean random_items;
	public boolean status_window_opened = false;
	public String[] available_items;
	public String extra_buttons;
	public String gui_message = "";
	public String current_command = "";
	public int draw_x;
	public int draw_y;
	public String draw_text = "";
	Button b_explore;
	Button b_continue;
	Button b_north;
	Button b_south;
	Button b_east;
	Button b_west;
	Button b_rpg_status;
	TextView game_window;
	TextView game_status;
	ScrollView status_scroller;
	SurfaceView game_gui;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_engine);
		b_explore = (Button)findViewById(R.id.explore_button);
		b_continue = (Button)findViewById(R.id.b_attack);
		b_north = (Button)findViewById(R.id.b_north);
		b_south = (Button)findViewById(R.id.b_south);
		b_east = (Button)findViewById(R.id.b_east);
		b_west = (Button)findViewById(R.id.b_west);
		b_rpg_status = (Button)findViewById(R.id.rpg_status_button);
		b = getIntent().getExtras();
		game_window = (TextView)findViewById(R.id.game_window);
		game_status = (TextView)findViewById(R.id.game_status);
		status_scroller = (ScrollView)findViewById(R.id.textAreaScroller);
		game_gui = (SurfaceView)findViewById(R.id.surfaceView1);
		game_gui.getHolder().addCallback(this);
		
		//Loads all of the game data
		loadRoomList();
		loadItemList();
		loadMonsterList();
		loadSpellList();
		beginRoom();
		
		//Listeners
		eastPressed();
		westPressed();
		northPressed();
		southPressed();
		contPressed();
		explorePressed();
		rpgPressed();
	}
	
	public void beginRoom(){
		
		//Disable continue button
		b_continue.setEnabled(false);
		
		//Loads user data variables
		loadData();
		
		//Picks out the data for the current room
		loadRoom(the_room);
		
		//Update GUI accordingly
		updateGUI(the_room_data);
}
	
	public void loadData(){
		userid = b.getInt("id");
		GameDatabase db = new GameDatabase(this);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor data = reader.query("userdata", new String[]{"name","class","spells","curroom","curhp","exp","gold","curmp","status"}, "id="+userid, null, null, null, "id");
		data.moveToFirst();
		gui_message = "";
		awaiting_decision = 0;
		extra_buttons = "false";
		user_name = data.getString(data.getColumnIndex("name"));
	    the_room = data.getInt(data.getColumnIndex("curroom"));
		user_class = data.getInt(data.getColumnIndex("class"));
		user_curhp = data.getInt(data.getColumnIndex("curhp"));
		user_gold = data.getInt(data.getColumnIndex("gold"));
		user_exp = data.getInt(data.getColumnIndex("exp"));
		user_curmp = data.getInt(data.getColumnIndex("curmp"));
		user_status = data.getInt(data.getColumnIndex("status"));
		spelllist = data.getString(data.getColumnIndex("spells"));
		user_spells = spelllist.split(",");
		data.close();
		db.close();
		calculateRPG();
	}
	
	public void calculateRPG(){
		switch(user_class){
		case 0: //Healer
			baseHP = 120;
			infDEF = 1.1;
			infATK = 0.9;
			break;
		case 1: //Fighter
			baseATK = 120;
			infATK = 1.1;
			infSATK = 1.1;
			infHP = 0.9;
			infDEF = 0.9;
			break;
		case 2: //Knight
			baseDEF = 120;
			baseATK = 120;
			infSPD = 0.6;
			infSDEF = 0.9;
			break;
		case 3: //Barbarian
			baseATK = 140;
			infSPD = 0.9;
			infSATK = 0.5;
			infSDEF = 0.6;
			break;
		case 4: //Thief
			baseSPD = 180;
			infSPD = 1.2;
			infDEF = 0.8;
			infSDEF = 0.8;
			break;
		case 5: //Cleric
			baseMP = 140;
			baseSATK = 100;
			infATK = 0.8;
			infDEF = 0.8;
			infSDEF = 1.1;
			break;
		}
		if(user_exp == 0) user_level = 1;
		else user_level = (int) Math.floor(Math.pow(user_exp / 1.25, 1.0 / 3.0 ));
		exp_for_next = (int) Math.floor(Math.pow((user_level+1), 3)*1.25) - user_exp;
		user_maxhp = (int) Math.floor((((((2 * baseHP) + Math.pow(user_level / 4, 1.39) + 100) * user_level) / 100) + 10) * infHP);
		user_maxmp = (int) Math.floor((((((2 * baseMP) + Math.pow(user_level / 4, 1.20) + 100) * user_level) / 100) + 7) * infMP);
		user_atk = (int) Math.floor((((((2 * baseATK) + Math.pow(user_level / 4, 1.12)) * user_level) / 100) + 5) * infATK);
		user_def = (int) Math.floor((((((2 * baseDEF) + Math.pow(user_level / 4, 1.1)) * user_level) / 100) + 5) * infDEF);
		user_spd = (int) Math.floor((((((2 * baseSPD) + Math.pow(user_level / 4, 1.18)) * user_level) / 100) + 5) * infSPD);
		user_satk = (int) Math.floor((((((2 * baseSATK) + Math.pow(user_level / 4, 1.04)) * user_level) / 100) + 5) * infSATK);
		user_sdef = (int) Math.floor((((((2 * baseSDEF) + Math.pow(user_level / 4, 1.07)) * user_level) / 100) + 5) * infSDEF);
		if(user_exp == 0) {
			GameDatabase db = new GameDatabase(this);
			SQLiteDatabase writer = db.getWritableDatabase();
			Cursor cu = writer.rawQuery("UPDATE userdata SET curhp = " + user_maxhp + ", curmp = " + user_maxmp + " WHERE id = " + userid, null);
			cu.moveToFirst();
			cu.close();
			writer.close();
			db.close();
			user_curhp = user_maxhp;
			user_curmp = user_maxmp;
		}
	}
	
	public void updateUserData(){
		GameDatabase db = new GameDatabase(this);
		SQLiteDatabase updater = db.getWritableDatabase();
		if(user_curhp < 1){
			user_curhp = user_maxhp; //this means I died in battle
			user_status = 0;
		}
 		Cursor cu = updater.rawQuery("UPDATE userdata SET curhp = " + user_curhp + ", curmp = " + user_curmp + ", status = " + user_status + ", gold = " + user_gold + ", exp = " + user_exp + " WHERE id = " + userid, null);
		cu.moveToFirst();
		cu.close();
		updater.close();
		db.close();
	}
	
	
	public void updateRoom(int new_room){
		
		//Make Directional Buttons Invisible Again//
		b_north.setVisibility(View.INVISIBLE); b_north.setEnabled(false);
		b_south.setVisibility(View.INVISIBLE); b_south.setEnabled(false);
		b_east.setVisibility(View.INVISIBLE); b_east.setEnabled(false); b_east.setText("East");
		b_west.setVisibility(View.INVISIBLE); b_west.setEnabled(false); b_west.setText("West");
		
		GameDatabase db = new GameDatabase(this);
		SQLiteDatabase writer = db.getWritableDatabase();
		Cursor cu = writer.rawQuery("UPDATE userdata SET curroom = " + new_room + " WHERE id = " + userid, null);
		cu.moveToFirst();
		cu.close();
		writer.close();
		db.close();
		beginRoom();
	}
	
	public void loadRoom(int room_id){
		for(String content : cv.get(the_room)){
			String[] values = content.split("<>");
			the_room_data.put(values[0], values[1]);
		}
		map_tiles.clear();
		String[] tileset = (the_room_data.get("map_gui").toString()).split(",");
		for(int i=0; i < tileset.length; i+=1)  map_tiles.add(tileset[i]);
		tryDrawing(game_gui.getHolder(), false);
		
	}
	
	public void updateGUI(ContentValues data){
		setTitle(data.getAsString("title"));
		random_encounters = data.getAsBoolean("random_encounters");
		random_items = data.getAsBoolean("items");
		if(random_items == true) available_items = data.getAsString("item_list").split(",");
		((Button)findViewById(R.id.explore_button)).setText(data.getAsString("re_button"));
		
		//Let's check whether we have extra buttons to load, the directional category is the only one currently "installed"
		extra_buttons = data.getAsString("extra_buttons");
		if(extra_buttons.equalsIgnoreCase("dpad")){
			
			//Make Directional Buttons Visible Again//
			b_north.setVisibility(View.VISIBLE);
			b_south.setVisibility(View.VISIBLE);
			b_east.setVisibility(View.VISIBLE); b_east.setText("East");
			b_west.setVisibility(View.VISIBLE); b_west.setText("West");
			
			//Figure out which buttons are usable, and where do they take me//
			for(String direction_data : data.getAsString("directional").split(",")){
				String[] dat = direction_data.split("=");
				switch(dat[0]){
				case "N" : north = Integer.parseInt(dat[1]); b_north.setEnabled(true); break; 
				case "S" : south = Integer.parseInt(dat[1]); b_south.setEnabled(true); break;
				case "E" : east = Integer.parseInt(dat[1]); b_east.setEnabled(true); break;
				case "W" : west = Integer.parseInt(dat[1]); b_west.setEnabled(true); break;
				}
			}
			
		}
		
		//Let's start loading up the text
		if(gui_message == "") parseText(data.getAsString("begin_text"));
	}
	
	public void parseText(String txt){
		current_command = txt;
		txt = txt.replace("$username", user_name);
		if(txt.indexOf("$do_") > 0){
			String parts[] = txt.split("\\$do_");
			if(parts[1].indexOf("ecision") > 0){
				String the_options = the_room_data.getAsString(parts[1]);
				String[] eachoption = the_options.split("\\|\\|");
				
				//Make Decision Buttons Visible Again//
				b_north.setVisibility(View.INVISIBLE); b_north.setEnabled(false); 
				b_south.setVisibility(View.INVISIBLE); b_south.setEnabled(false);
				b_east.setVisibility(View.VISIBLE); b_east.setEnabled(true); b_east.setText(eachoption[0]); 
				b_west.setVisibility(View.VISIBLE); b_west.setEnabled(true); b_west.setText(eachoption[1]);
				
				//Get the decision number
				awaiting_decision = Integer.valueOf(parts[1].substring(8, 9));
			}
			else if(parts[1].indexOf("oroom") > 0){
				
				next_room = Integer.valueOf(parts[1].substring(6, 7));
				
				//Make Directional Buttons Invisible Again//
				b_north.setVisibility(View.INVISIBLE); b_north.setEnabled(false);
				b_south.setVisibility(View.INVISIBLE); b_south.setEnabled(false);
				b_east.setVisibility(View.INVISIBLE); b_east.setEnabled(false); b_east.setText("East");
				b_west.setVisibility(View.INVISIBLE); b_west.setEnabled(false); b_west.setText("West");
				
				
				//Make continue button usable
				b_continue.setEnabled(true);
			}
			gui_message = parts[0];
		}
		else gui_message = txt;
		game_window.setText(unescape(gui_message));
	}
	
	private String unescape(String description) {
	    return description.replaceAll("\\\\n", "\\\n");
	}
	
	public void eastPressed(){
		b_east.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(awaiting_decision != 0){
					parseText(the_room_data.getAsString("trigger" + awaiting_decision + "-0"));
				}
				else if(extra_buttons.equalsIgnoreCase("dpad")){
					updateRoom(east);
				}
			}
	   });
	}
	
	public void westPressed(){
		b_west.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(awaiting_decision != 0){
					parseText(the_room_data.getAsString("trigger" + awaiting_decision + "-1"));
				}
				else if(extra_buttons.equalsIgnoreCase("dpad")){
					updateRoom(west);
				}
			}
	   });
	}
	
	public void northPressed(){
		b_north.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				updateRoom(north);
			}
	   });
	}
	
	public void southPressed(){
		b_south.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				updateRoom(south);
			}
	   });
	}
	
	public void contPressed(){
		b_continue.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				updateRoom(next_room);
			}
	   });
	}
	
	public void explorePressed(){
		b_explore.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				findSomething();
			}
	   });
	}
	
	public void rpgPressed(){
		b_rpg_status.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				do_status_window();
			}
	   });
	}
	
	public void findSomething(){
		int odds = randInt(1,6);
		
		//One in 6 chance of finding an item
		if(odds == 1) {
			int theitem = randInt(0, available_items.length - 1);
			int item_found = Integer.valueOf(available_items[theitem]);
			draw_x = 0; draw_y = 0; draw_text = "You found the item: " + items_data.get(item_found).get(0);
			tryDrawing(game_gui.getHolder(), true);
			//game_status.setText(game_status.getText() + "\nYou found the item: " + items_data.get(item_found).get(0));
			scrollDown();
			
			//If we can store this item//
			if(items_data.get(item_found).get(3).equalsIgnoreCase("unobtainable") == false){
				GameDatabase db = new GameDatabase(this);
				SQLiteDatabase updater = db.getWritableDatabase();
				SQLiteDatabase reader = db.getReadableDatabase();
				Cursor exists = reader.rawQuery("SELECT * FROM itemdata WHERE id=" + item_found + " AND owner=" + userid, null);
				if(exists.getCount() <= 0){
					Cursor adder = updater.rawQuery("INSERT INTO itemdata SELECT " + item_found + " AS id, " + userid + " AS owner, 1 AS quantity", null);
					adder.moveToFirst();
					adder.close();
				}
				else {
					exists.moveToFirst();
					int curamount = exists.getInt(exists.getColumnIndex("quantity")) + 1;
					Cursor adder = updater.rawQuery("UPDATE itemdata SET quantity = " + curamount + " WHERE id=" + item_found + " AND owner=" + userid, null);
					adder.moveToFirst();
					adder.close();
				}
				reader.close();
				updater.close();
				db.close();
			}
			if(the_room_data.containsKey("item_found" + item_found) == true) parseText(the_room_data.getAsString("item_found" + item_found));
		} //One in 6 chance of not finding anything
		else if(odds == 6){
			draw_x = 0; draw_y = 0; draw_text = "Nothing was^found!";
			tryDrawing(game_gui.getHolder(), true);
			//game_status.setText(game_status.getText() + "\nNothing was found!");
			scrollDown();
		}//2 in 6 chance of a battle
		else if(odds == 5 || odds == 4){
			int enemy = randInt(0,3);
			Intent i = new Intent(this, BattleEngine.class);
			i.putExtra("user_name", user_name);
			i.putExtra("user_level", user_level);
			i.putExtra("userid", userid);
			i.putExtra("curhp", user_curhp);
			i.putExtra("maxhp", user_maxhp);
			i.putExtra("curmp", user_curmp);
			i.putExtra("maxmp", user_maxmp);
			i.putExtra("atk", user_atk);
			i.putExtra("def", user_def);
			i.putExtra("satk", user_satk);
			i.putExtra("sdef", user_sdef);
			i.putExtra("spd", user_spd);
			i.putExtra("status", user_status);
			i.putExtra("spells", spelllist);
			i.putExtra("enemy_name", monster_data.get(enemy).get(0).toString());
			i.putExtra("enemy_stats", monster_data.get(enemy).get(1).toString());
			i.putExtra("enemy_gold", Integer.valueOf(monster_data.get(enemy).get(2).toString()));
			i.putExtra("enemy_exp", Integer.valueOf(monster_data.get(enemy).get(3).toString()));
			i.putExtra("enemy_flags", monster_data.get(enemy).get(4).toString());
			startActivityForResult(i, 1);
		}//2 in 6 chance of coins
		else{
			int coins = randInt(1,100);
			int gold_found = ((the_room + 1) * (odds * 5)) + coins;
			user_gold += gold_found;
			if(user_gold > 50000000) user_gold = 50000000; //50 million is max amount of gold
			//game_status.setText(game_status.getText() + "\nYou found " + gold_found + " gold!");
			draw_x = 0; draw_y = 0; draw_text = "You found^" + gold_found + " gold!";
			tryDrawing(game_gui.getHolder(), true);
			scrollDown();
			
			//Update the database
			GameDatabase db = new GameDatabase(this);
			SQLiteDatabase updater = db.getWritableDatabase();
			Cursor cu = updater.rawQuery("UPDATE userdata SET gold =" + user_gold + ", exp =" + user_exp + " WHERE id=" + userid , null);
			cu.moveToFirst();
			cu.close();
			updater.close();
			db.close();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	    if (requestCode == 1) {
	        if(resultCode == RESULT_OK){
	            boolean won = data.getBooleanExtra("won", false);
	            user_curhp = data.getIntExtra("newhp", 1);
            	user_curmp = data.getIntExtra("newmp", 1);
            	user_status = data.getIntExtra("status", 1);
	            if(won == true){
	            	int newexp = data.getIntExtra("newexp", 1);
	            	int newgold = data.getIntExtra("newgold", 1);
	            	draw_x = 0; draw_y = 0; draw_text = "You earned " + newexp + " EXP and " + newgold + " gold!";
	    			tryDrawing(game_gui.getHolder(), true);
	            	user_exp += newexp;
	            	user_gold += newgold;
	            	updateUserData();
	            }
	            else {
	            	user_gold -= data.getIntExtra("newgold", 1);
	            	if(user_gold < 0) user_gold = 0;
	            	draw_x = 0; draw_y = 0; draw_text = "You lost the battle! And some gold, too!";
	    			tryDrawing(game_gui.getHolder(), true);
	            	updateUserData();
	            }
	        }
	        if (resultCode == RESULT_CANCELED) {
	        	int hpleft = data.getIntExtra("user_hpleft", 50);
	        	int lostgold = (user_gold / 100) * hpleft;
	        	user_gold -= lostgold;
	        	if(user_gold < 0) user_gold = 0;
	        	draw_x = 0; draw_y = 0; draw_text = "You cancelled the battle without fleeing! Gold lost: " + lostgold;
    			tryDrawing(game_gui.getHolder(), true);
	        	updateUserData();
	        }
	    }
	}
	
	public void do_status_window(){
		if(status_window_opened == false){
			calculateRPG();
			b_rpg_status.setText("Close");
			b_continue.setVisibility(View.INVISIBLE);
			b_explore.setVisibility(View.INVISIBLE);
			game_window.setText("");
			
			//Make Directional Buttons Invisible //
			b_north.setVisibility(View.INVISIBLE); 
			b_south.setVisibility(View.INVISIBLE);
			b_east.setVisibility(View.INVISIBLE); 
			b_west.setVisibility(View.INVISIBLE);
			
			//Expand the scrollview to put the status message in
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) status_scroller.getLayoutParams();
			final float scale = getResources().getDisplayMetrics().density;
			int pixels = (int) (450 * scale + 0.5f);
			params.height = pixels;
			status_scroller.setLayoutParams(params);
			

			String thestatus = user_name + 
					"^Class: " + GameDatabase.CLASS_NAMES[user_class] + 
					"^Status: " + GameDatabase.STATUS_NAMES[user_status] +
					"^Level: " + user_level + 
					"^EXP to level up: " + exp_for_next +
					"^Total EXP: " + user_exp +
					"^HP: " + user_curhp + " / " + user_maxhp + 
					"^MP: " + user_curmp + " / " + user_maxmp + 
					"^Attack: " + user_atk +
					"^Defense: " + user_def + 
					"^Magic Attack: " + user_satk + 
					"^Magic Defense: " + user_sdef + 
					"^Speed: " + user_spd + 
					"^Gold: " + user_gold + 
					"^^Items:^";
			GameDatabase db = new GameDatabase(this);
			SQLiteDatabase reader = db.getReadableDatabase();
			Cursor entries = reader.rawQuery("SELECT quantity, id FROM itemdata WHERE owner=" + userid, null);
			if(entries.getCount() > 990){
				entries.moveToFirst();
				thestatus = thestatus + entries.getInt(entries.getColumnIndex("quantity")) + " of " + items_data.get(entries.getInt(entries.getColumnIndex("id"))).get(0);
				while(entries.moveToNext() == true){
					thestatus = thestatus + "^" + entries.getInt(entries.getColumnIndex("quantity")) + " of " + items_data.get(entries.getInt(entries.getColumnIndex("id"))).get(0);
				}
			}
			else thestatus = thestatus + "You have no items!";
			if(user_spells.length > 0 && false){
				thestatus = thestatus + "\n\nSpells:";
				for(int i = 0; i < user_spells.length; i += 1){
					thestatus = thestatus + "\n" + spells_data.get(Integer.valueOf(user_spells[i])).get(0) + " (requires: " + spells_data.get(Integer.valueOf(user_spells[i])).get(1) + "MP)";
				}
			}
			entries.close();
			reader.close();
			db.close();
			status_window_opened = true;
			draw_x = 0; draw_y = 0; draw_text = thestatus;
			tryDrawing(game_gui.getHolder(), true);

		}
		else{
			b_rpg_status.setText("Status");
			b_continue.setVisibility(View.VISIBLE);
			b_explore.setVisibility(View.VISIBLE);
			
			if(extra_buttons.equalsIgnoreCase("dpad")){
				
				//Make Directional Buttons Visible Again//
				b_north.setVisibility(View.VISIBLE);
				b_south.setVisibility(View.VISIBLE);
				b_east.setVisibility(View.VISIBLE); b_east.setText("East");
				b_west.setVisibility(View.VISIBLE); b_west.setText("West");
			}
			
			parseText(current_command);
			
			//Shrink back the scrollview to put the status message in
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) status_scroller.getLayoutParams();
			final float scale = getResources().getDisplayMetrics().density;
			int pixels = (int) (60 * scale + 0.5f);
			params.height = pixels;
			status_scroller.setLayoutParams(params);
			
			status_window_opened = false;
			draw_x = 0; draw_y = 0; draw_text = "Status window closed.";
			tryDrawing(game_gui.getHolder(), true);
		}
	}
	
	public void loadRoomList(){
		
		try {
			 //Load game data
			 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		     factory.setNamespaceAware(false);
		     Context context = getApplicationContext();
		     Resources res = context.getResources();
		     XmlResourceParser xpp = res.getXml(R.xml.game_data);
		  
		     //Helper stuff
	         int curid = 0;
	         ArrayList<String> temp = new ArrayList<String>();
	
	         // Returns the type of current event: START_TAG, END_TAG, etc..
		     int eventType = xpp.getEventType();
		     while (eventType != XmlPullParser.END_DOCUMENT) {
		         if (eventType == XmlPullParser.START_TAG) {
		        	 if(xpp.getName().equalsIgnoreCase("id")){
	            		 curid = Integer.parseInt(xpp.nextText());
		        	 } 
		        	 if(xpp.getName().equalsIgnoreCase("room") == false && xpp.getName().equalsIgnoreCase("id") == false && xpp.getName().equalsIgnoreCase("data") == false){
		        		 temp.add(xpp.getName() + "<>" + xpp.nextText());
		        	 }
		         
		         } 
		         else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("room")){
		        	 cv.put(curid, temp);
		        	 temp = new ArrayList<String>();
		         }
		         eventType = xpp.next(); //move to next element
		         //Restart Loop
		     }
			 } catch (XmlPullParserException e) {
			     e.printStackTrace();
			 } catch (IOException e) {
			     e.printStackTrace();
			 }
	}
	
	public void loadItemList(){
		
		try {	
			 //Load game data
			 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		     factory.setNamespaceAware(false);
		     Context context = getApplicationContext();
		     Resources res = context.getResources();
		     XmlResourceParser xpp = res.getXml(R.xml.item_data);
		  
		     //Helper stuff
	         int curid = 0;
	         ArrayList<String> temp = new ArrayList<String>();
	
	         // Returns the type of current event: START_TAG, END_TAG, etc..
		     int eventType = xpp.getEventType();
		     while (eventType != XmlPullParser.END_DOCUMENT) {
		         if (eventType == XmlPullParser.START_TAG) {
		        	 if(xpp.getName().equalsIgnoreCase("id")){
	            		 curid = Integer.parseInt(xpp.nextText());
		        	 } 
		        	 if(xpp.getName().equalsIgnoreCase("item") == false && xpp.getName().equalsIgnoreCase("id") == false && xpp.getName().equalsIgnoreCase("data") == false){
		        		 temp.add(xpp.nextText());
		        	 }
		         
		         } 
		         else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
		        	 items_data.put(curid, temp);
		        	 temp = new ArrayList<String>();
		         }
		         eventType = xpp.next(); //move to next element
		         //Restart Loop
		     }
			 } catch (XmlPullParserException e) {
			     e.printStackTrace();
			 } catch (IOException e) {
			     e.printStackTrace();
			 }
	}
	
	public void loadMonsterList(){
		
		try {	
			 //Load game data
			 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		     factory.setNamespaceAware(false);
		     Context context = getApplicationContext();
		     Resources res = context.getResources();
		     XmlResourceParser xpp = res.getXml(R.xml.monster_data);
		  
		     //Helper stuff
	         int curid = 0;
	         ArrayList<String> temp = new ArrayList<String>();
	
	         // Returns the type of current event: START_TAG, END_TAG, etc..
		     int eventType = xpp.getEventType();
		     while (eventType != XmlPullParser.END_DOCUMENT) {
		         if (eventType == XmlPullParser.START_TAG) {
		        	 if(xpp.getName().equalsIgnoreCase("id")){
	            		 curid = Integer.parseInt(xpp.nextText());
		        	 } 
		        	 if(xpp.getName().equalsIgnoreCase("monster") == false && xpp.getName().equalsIgnoreCase("id") == false && xpp.getName().equalsIgnoreCase("data") == false){
		        		 temp.add(xpp.nextText());
		        	 }
		         
		         } 
		         else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("monster")){
		        	 monster_data.put(curid, temp);
		        	 temp = new ArrayList<String>();
		         }
		         eventType = xpp.next(); //move to next element
		         //Restart Loop
		     }
			 } catch (XmlPullParserException e) {
			     e.printStackTrace();
			 } catch (IOException e) {
			     e.printStackTrace();
			 }
	}
	
	public void loadSpellList(){
		
		try {	
			 //Load game data
			 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		     factory.setNamespaceAware(false);
		     Context context = getApplicationContext();
		     Resources res = context.getResources();
		     XmlResourceParser xpp = res.getXml(R.xml.spells);
		  
		     //Helper stuff
	         int curid = 0;
	         ArrayList<String> temp = new ArrayList<String>();
	
	         // Returns the type of current event: START_TAG, END_TAG, etc..
		     int eventType = xpp.getEventType();
		     while (eventType != XmlPullParser.END_DOCUMENT) {
		         if (eventType == XmlPullParser.START_TAG) {
		        	 if(xpp.getName().equalsIgnoreCase("id")){
	            		 curid = Integer.parseInt(xpp.nextText());
		        	 } 
		        	 if(xpp.getName().equalsIgnoreCase("spell") == false && xpp.getName().equalsIgnoreCase("id") == false && xpp.getName().equalsIgnoreCase("data") == false){
		        		 temp.add(xpp.nextText());
		        	 }
		         
		         } 
		         else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("spell")){
		        	 spells_data.put(curid, temp);
		        	 temp = new ArrayList<String>();
		         }
		         eventType = xpp.next(); //move to next element
		         //Restart Loop
		     }
			 } catch (XmlPullParserException e) {
			     e.printStackTrace();
			 } catch (IOException e) {
			     e.printStackTrace();
			 }
	}
	
	public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	public void scrollDown(){
		status_scroller.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				status_scroller.fullScroll(View.FOCUS_DOWN);
			}
		});
	}
	
	
	//Drawing functions
	
	@Override
    public void surfaceCreated(SurfaceHolder holder) {
        tryDrawing(holder, false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int frmt, int w, int h) { 
        tryDrawing(holder, false);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
    
    private void tryDrawing(SurfaceHolder holder, boolean text) {
    	 Log.i(TAG, "Trying to draw...");
    	 Bitmap cache = game_gui.getDrawingCache();
         Canvas canvas = holder.lockCanvas();
         if (canvas == null) {
             Log.e(TAG, "Cannot draw onto the canvas as it's null");
         } else {
        	 canvas.drawColor(Color.rgb(50, 50, 50));
        	 if(text == true){
        		 if(cache != null) canvas.drawBitmap(cache, game_gui.getWidth(), game_gui.getHeight(), null);
        		 drawText(draw_x, draw_y, draw_text, canvas);
        	 }
        	 if(status_window_opened == false && (text == false || cache == null)) drawMyStuff(canvas);
             holder.unlockCanvasAndPost(canvas);
         }
    }
    
    private void drawMyStuff(final Canvas canvas) {
    	Resources res = getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res, R.raw.rpg_tileset);
		int tilewidth = (bitmap.getWidth() / 32);
		int tileheight = (bitmap.getHeight() / 63);
		int destwidth = game_gui.getWidth() / 16;
		int destheight = game_gui.getHeight() / 5;
		if(!map_tiles.isEmpty()){
			for(int i = 0; i < 16*4; i+=1){
				int curtilexnum = Integer.valueOf((map_tiles.get(i).split("="))[0]); 
				int curtileynum = Integer.valueOf((map_tiles.get(i).split("="))[1]); 
				Rect thetile = new Rect(curtilexnum*tilewidth,curtileynum*tileheight,(curtilexnum*tilewidth)+tilewidth,(curtileynum*tileheight)+tileheight);
				Rect destination = new Rect((i % 16)*destwidth,((int)Math.floor((double)i / 16.0))*destheight,((i % 16)*destwidth)+destwidth,(((int)Math.floor((double)i / 16.0))*destheight)+destheight);
				canvas.drawBitmap(bitmap, thetile, destination, null);
			}
		}
    }
    
    private void drawText(int x, int y, String text, Canvas canvas){
    	int len = text.length();
    	x+=5;
    	y+=5;
    	int destheight = (game_gui.getHeight() / 5) * 4;
    	int rowlength = game_gui.getWidth() / 5;
    	int grid = (game_gui.getHeight() / 5) / 5;
    	if(status_window_opened == true){
    		destheight = 0;
    		grid = game_gui.getHeight() / 5;
    	}
    	drawBox(destheight, x, rowlength, grid, canvas);
    	writeFont(x,y,text,canvas);
    }
    
    public void drawBox(int start_y, int start_x, int width, int height, Canvas canvas){
    	for(int yy = 0; yy < height; yy += 1){
    		for(int xx = 0; xx < width; xx += 1){
    			Bitmap todraw = null;
    			if(xx == 0 && yy == 0) todraw = BitmapFactory.decodeResource(getResources(), R.raw.topleft);
    			if(xx > 0 && xx + 1 < width && yy == 0) todraw = BitmapFactory.decodeResource(getResources(), R.raw.top);
    			if(xx + 1 == width && yy == 0) todraw = BitmapFactory.decodeResource(getResources(), R.raw.topright);
    			if(xx == 0 && yy > 0 && yy + 1 < height) todraw = BitmapFactory.decodeResource(getResources(), R.raw.left);
    			if(xx + 1 == width && yy > 0 && yy + 1 < height) todraw = BitmapFactory.decodeResource(getResources(), R.raw.right);
    			if(xx == 0 && yy + 1 == height) todraw = BitmapFactory.decodeResource(getResources(), R.raw.bottom_left);
    			if(xx > 0 && xx + 1 < width && yy + 1 == height) todraw = BitmapFactory.decodeResource(getResources(), R.raw.bottom);
    			if(xx + 1 == width && yy + 1 == height) todraw = BitmapFactory.decodeResource(getResources(), R.raw.bottom_right);
    			if(todraw != null){
    				Rect drawplace = new Rect((xx*5)+start_x,(yy*5)+start_y,start_x+(xx*5)+5,start_y + (yy*5)+5);
    				canvas.drawBitmap(todraw, null, drawplace, null);
    			}
    		}
    	}
    }
    
    public void write(int x, int y, String text, Canvas canvas){
    	int len = text.length();
    	x+=5;
    	y+=5;
    	Bitmap font = BitmapFactory.decodeResource(getResources(), R.raw.font);
    	int w = font.getWidth() / 16;
    	int h = font.getHeight() / 16;
    	int halp = 0;
    	for(int i = 0; i < len; i += 1){
    		if(text.charAt(i) == ("^").charAt(0)){
    			//x -= (halp*w)+halp;
    			halp = 0;
    			y += h+5;
    			continue;
    		}
    		int ch = (int)text.charAt(i);
    		int bitmap_x = (int)Math.floor((double)ch % 16) * w;
    		int bitmap_y = (int)Math.floor((double)ch / 16.0) * h;
    		Rect theletter = new Rect(bitmap_x, bitmap_y, bitmap_x + w - 1, bitmap_y + h - 1);
    		Rect drawplace = new Rect((halp*w)+x, y, x+(halp*w)+w, y + h);
    		canvas.drawBitmap(font, theletter, drawplace, null);
    		halp += 1;
    	}
    }
    
    public void writeFont(int x, int y, String text, Canvas canvas){
    	Typeface font = Typeface.createFromAsset(getAssets(), "mariorpg.ttf");
    	Paint paint = new Paint();
    	paint.setColor(Color.WHITE);
    	paint.setTextSize(16);
    	paint.setTypeface(font);
    	paint.setShadowLayer(3, 4, 4, Color.BLUE);
    	int i = 30;
    	for(String piece : text.split("\\^")){
    		canvas.drawText(piece, x+5, y+i, paint);
    		i+=30;
    	}
    }
}
