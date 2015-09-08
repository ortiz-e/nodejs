package com.eldimentio.rpgstory;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class BattleEngine extends Activity {
	
	Bundle b;
	public int user_curmp;
	public int user_curhp;
	public int user_maxhp;
	public int user_maxmp;
	public int user_level;
	public int user_atk;
	public int user_def;
	public int user_spd;
	public int user_satk;
	public int user_sdef;
	public int user_status;
	public int userid;
	public int enemy_hp;
	public int enemy_maxhp;
	public int enemy_atk;
	public int enemy_def;
	public int enemy_satk;
	public int enemy_sdef;
	public int enemy_spd;
	public int enemy_status = 0;
	public int enemy_poison_contact = 0;
	public int enemy_poison_attack = 0;
	public int enemy_paralyze_contact = 0;
	public int enemy_paralyze_attack = 0;
	public int enemy_spikes_contact = 0;
	
	//stats increases and decreases,number of turns left
	public String user_atk_multiplier = "1,1";
	public String user_def_multiplier = "1,1";
	public String user_satk_multiplier = "1,1";
	public String user_sdef_multiplier = "1,1";
	public String user_spd_multiplier = "1,1";
	public String enemy_atk_multiplier = "1,1";
	public String enemy_def_multiplier = "1,1";
	public String enemy_satk_multiplier = "1,1";
	public String enemy_sdef_multiplier = "1,1";
	public String enemy_spd_multiplier = "1,1";
	public String enemy_weakness = "";
	public String enemy_strength = "";
	public ArrayList<String> my_items = new ArrayList<String>();
	public ArrayList<String> my_spells = new ArrayList<String>();
	public String enemy_name;
	public String user_name;
	public String[] user_spells;
	public String[] enemy_stats;
	public String[] enemy_flags;
	public int enemy_gold;
	public int enemy_exp;
	public int decreaser = 0;
	public int increaser = 0;
	public int action = 0;
	public ArrayList<Integer> item_mapper = new ArrayList<Integer>();
	public ArrayList<Integer> spell_mapper = new ArrayList<Integer>();
	public int item_to_use;
	public int spell_to_use;
	public boolean battle_complete = false;
	public boolean i_won = false;
	public boolean has_items = false;
	ProgressBar myhp;
	ProgressBar mymp;
	ProgressBar tahp;
	TextView myname;
	TextView taname;
	TextView myhptxt;
	TextView mymptxt;
	TextView game_status;
	TextView mystatustxt;
	Button b_attack;
	Button b_magic;
	Button b_item;
	Button b_flee;
	ScrollView game_status_holder;
	static boolean done = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_engine);
		b = getIntent().getExtras();
		user_curmp = b.getInt("curmp");
		user_curhp = b.getInt("curhp");
		user_maxhp = b.getInt("maxhp");
		user_maxmp = b.getInt("maxmp");
		user_atk = b.getInt("atk");
		user_def = b.getInt("def");
		user_spd = b.getInt("spd");
		user_satk = b.getInt("satk");
		user_sdef = b.getInt("sdef");
		user_status = b.getInt("status");
		userid = b.getInt("userid");
		user_level = b.getInt("user_level");
		user_name = b.getString("user_name");
		enemy_name = b.getString("enemy_name");
		user_spells = b.getString("spells").split(",");
		enemy_stats = b.getString("enemy_stats").split(",");
		enemy_flags = b.getString("enemy_flags").split(",");
		enemy_gold = b.getInt("enemy_gold");
		enemy_exp = b.getInt("enemy_exp");
		enemy_hp = Integer.valueOf(enemy_stats[0]);
		enemy_maxhp = enemy_hp;
		enemy_atk = Integer.valueOf(enemy_stats[1]);
		enemy_def = Integer.valueOf(enemy_stats[2]);
		enemy_satk = Integer.valueOf(enemy_stats[3]);
		enemy_sdef = Integer.valueOf(enemy_stats[4]);
		enemy_spd = Integer.valueOf(enemy_stats[5]);
		myhp = (ProgressBar)findViewById(R.id.my_hp);
		mymp = (ProgressBar)findViewById(R.id.my_mp);
		tahp = (ProgressBar)findViewById(R.id.enemyHP);
		myname = (TextView)findViewById(R.id.player_name);
		taname = (TextView)findViewById(R.id.enemy_name);
		myhptxt = (TextView)findViewById(R.id.myhp_text);
		mymptxt = (TextView)findViewById(R.id.mymp_text);
		game_status = (TextView)findViewById(R.id.game_status);
		mystatustxt = (TextView)findViewById(R.id.mystatus_text);
		game_status_holder = (ScrollView)findViewById(R.id.textAreaScroller);
		myhp.setProgress((int)Math.floor((((double)user_curhp / (double)user_maxhp) * 100)) - 1);
		mymp.setProgress((int)Math.floor((((double)user_curmp / (double)user_maxmp) * 100)) - 1);
		tahp.setProgress((int)Math.floor(((enemy_hp / enemy_maxhp) * 100)) - 1);
		myhptxt.setText("HP: " + user_curhp + " / " + user_maxhp);
		mymptxt.setText("MP: " + user_curmp + " / " + user_maxmp);
		mystatustxt.setText("Status: " + GameDatabase.STATUS_NAMES[user_status]);
		myname.setText(user_name);
		taname.setText(enemy_name);
		game_status.setText("You are now fighting " + enemy_name);
		b_attack = (Button)findViewById(R.id.b_attack);
		b_magic = (Button)findViewById(R.id.b_spells);
		b_item = (Button)findViewById(R.id.b_items);
		b_flee = (Button)findViewById(R.id.b_flee);
		loadFlags();
		attack_listener();
		spells_listener();
		item_listener();
		flee_listener();
		loadItemList();
		loadSpellList();
	}
	
	//this loads the enemy's flags
	public boolean loadFlags(){
		if(enemy_flags == null) return false;
		for(int i = 0; i < enemy_flags.length; i+= 1){
			String[] theflag = enemy_flags[i].split("=");
			switch(theflag[0]){
				case "poison_contact": enemy_poison_contact = Integer.valueOf(theflag[1]); break;
				case "poison_attack": enemy_poison_attack = Integer.valueOf(theflag[1]); break;
				case "paralyze_contact": enemy_paralyze_contact = Integer.valueOf(theflag[1]); break;
				case "paralyze_attack": enemy_paralyze_attack = Integer.valueOf(theflag[1]); break;
				case "spikes_contact" : enemy_spikes_contact = Integer.valueOf(theflag[1]); break;
				case "weak_to" : enemy_weakness = theflag[1]; break;
				case "strong_against" : enemy_strength = theflag[1]; break;
			}
		}
		return true;
	}
	
	
	public void battle_turn(){
		b_attack.setEnabled(false);
		b_magic.setEnabled(false);
		b_item.setEnabled(false);
		b_flee.setEnabled(false);
		TheBattleLoop b = new TheBattleLoop();
		b.execute();
	}
	
	public void battle_turn_end(){
		doStatChanges();
		b_attack.setEnabled(true);
		b_magic.setEnabled(true);
		b_item.setEnabled(true);
		b_flee.setEnabled(true);
	}
	
	private class TheBattleLoop extends AsyncTask<Void,String,String>{
		
		public void sleeper(){
			try {
				Thread.sleep(1400);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public boolean onEnemyContact(){
			if(user_status == 0){
				int chance = randInt(0,99);
				if(enemy_poison_contact > 0){
					if(chance <= enemy_poison_contact){
						user_status = 1;
						publishProgress("...But physical contact with the enemy has poisoned you!");
						sleeper();
					}
				}
				if(enemy_paralyze_contact > 0){
					if(chance <= enemy_paralyze_contact){
						user_status = 2;
						publishProgress("...But physical contact with the enemy has paralyzed you!");
						sleeper();
					}
				}
				if(enemy_spikes_contact > 0){
						user_curhp -= enemy_spikes_contact;
						if(user_curhp < 1) user_curhp = 0;
						decreaser = (int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100);
						update_bars(0,decreaser,false);
						publishProgress("The enemy's rough body hurts you for " + enemy_spikes_contact + " HP!");
						sleeper();
						if(user_curhp < 1) return true;
				}
			}
			return false;
		}
		
		public void onContactByEnemy(){
			if(user_status == 0){
				int chance = randInt(0,100);
				if(enemy_poison_attack > 0){
					if(chance <= enemy_poison_attack){
						user_status = 1;
						publishProgress("...and on top of that, the attack has poisoned you!");
						sleeper();
					}
				}
				if(enemy_paralyze_attack > 0){
					if(chance <= enemy_paralyze_attack){
						user_status = 2;
						publishProgress("...and on top of that, the attack has paralyzed you!");
						sleeper();
					}
				}
			}
		}
		
		public void attackEnemy(){
			int damage;
			damage = (int)((((2.0 * (double)user_level / 5.0) * user_atk * randInt(user_level, user_level + 10)) / enemy_def) / 10.0) + 2;
			enemy_hp -= damage;
			if(enemy_hp < 1) enemy_hp = 0;
			decreaser = (int)Math.floor(((double)enemy_hp / (double)enemy_maxhp) * 100);
			update_bars(1,decreaser,false);
			publishProgress("You have hurt " + enemy_name + " for " + damage + " HP!");
			sleeper();
		}
		
		public void enemyAttacks(){
			int damage;
			damage = (int)((((2.0 * (double)enemy_hp / 6.0) * enemy_atk * randInt(enemy_atk, enemy_atk + 10)) / user_def) / user_level) + 1;
			user_curhp -= damage;
			if(user_curhp < 1) user_curhp = 0;
			decreaser = (int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100);
			update_bars(0,decreaser,false);
			publishProgress(enemy_name + " attacks you for " + damage + " HP!");
			sleeper();
		}
		
		public int checkUserStatus(){
			if(user_status == 1){
				user_curhp -= ((user_maxhp / 20) < 1 ? 1 : (user_maxhp / 20));
				if(user_curhp < 1) user_curhp = 0;
				decreaser = (int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100);
				update_bars(0,decreaser,false);
				publishProgress("You lose HP due to poison!");
				sleeper();
				if(user_curhp == 0) return 1;
			}
			else if(user_status == 3){
				user_curhp -= ((user_maxhp / 8) < 1 ? 1 : (user_maxhp / 8));
				if(user_curhp < 1) user_curhp = 0;
				decreaser = (int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100);
				update_bars(0,decreaser,false);
				publishProgress("You lose a lot of HP due to poison!");
				sleeper();
				if(user_curhp == 0) return 1;
			}
			return 0;
		}
		
		public int checkEnemyStatus(){
			if(enemy_status == 1){
				enemy_hp -= ((enemy_maxhp / 20) < 1 ? 1 : (enemy_maxhp / 20));
				if(enemy_hp < 1) enemy_hp = 0;
				decreaser = (int)Math.floor(((double)enemy_hp / (double)enemy_maxhp) * 100);
				update_bars(0,decreaser,false);
				publishProgress("The enemy lost HP due to poison!");
				sleeper();
				if(enemy_hp == 0) return 1;
			}
			return 0;
		}
		
		public void loadItemFlags(String itemname, String[] itemflags){
			for(int i = 0; i < itemflags.length; i+= 1){
				switch(itemflags[i]){
					case "heal_poison": 
						if(user_status == 1 || user_status == 3){
							user_status = 0;
							publishProgress("The " + itemname + " has cured your poison!");
						}
						else publishProgress("But you weren't poisoned!");
						break;
					case "heal_paralysis":
						if(user_status == 2){
							user_status = 0;
							publishProgress("The " + itemname + " has cured your paralysis!");
						}
						else publishProgress("But you weren't paralyzed!");
						break;
					case "swap_hp":
						if(user_curhp != enemy_hp){
							boolean meup;
							if(user_curhp > enemy_hp)  meup = true;
							else meup = false;
							int myhp = enemy_hp;
							enemy_hp = user_curhp;
							user_curhp = myhp;
							if(user_curhp > user_maxhp) user_curhp = user_maxhp;
							if(enemy_hp > enemy_maxhp) enemy_hp = enemy_maxhp;
							update_bars(0,((int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100)),(meup == true ? false : true));
							update_bars(1,((int)Math.floor(((double)enemy_hp / (double)enemy_maxhp) * 100)),(meup == false ? false : true));
							publishProgress("You and " + enemy_name + " have switched HP!");
						}
						else publishProgress("...but you and " + enemy_name + " both have the same HP left!");
				}
			}
		}
		
		public int loadSpellFlags(String spellname, String[] spellflags){
			for(int i = 0; i < spellflags.length; i+= 1){
				int chance = randInt(0,99);
				String[] theflag = spellflags[i].split("=");
				switch(theflag[0]){
					case "burn" : if(chance <= Integer.valueOf(theflag[1])) {
						enemy_status = 4; 
						publishProgress("The enemy was burned! Their attack will be lowered somewhat!");
						return 2;
					} break;
					case "poison" : if(chance <= Integer.valueOf(theflag[1])) {
						enemy_status = 1; 
						publishProgress("The enemy was poisoned!");
						return 2;
					} break;
					case "paralyze" : if(chance <= Integer.valueOf(theflag[1])){
						enemy_status = 2;
						publishProgress("The enemy has been paralyzed!");
						return 2;
					} break;
					case "heal" : user_curhp += (user_maxhp / (100 / Integer.valueOf(theflag[1])));
						if(user_curhp > user_maxhp) user_curhp = user_maxhp;
						increaser = (int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100);
						update_bars(0,increaser,true);
						publishProgress("The spell healed your HP!");
						return 2;
					case "double_attack" : if((double)Integer.valueOf((user_atk_multiplier.split(","))[0]) <= 1){
						user_atk = (int)((user_atk / (double)(Integer.valueOf((user_atk_multiplier.split(","))[0]))) * 2);
						user_atk_multiplier = "2," + theflag[1];
						publishProgress("Your attack has doubled for " + theflag[1] + " turns!");
					}
					else publishProgress("You can't increase your attack any further!");
					return 2;
					case "double_defense" : if((double)Integer.valueOf((user_def_multiplier.split(","))[0]) <= 1){
						user_def = (int)((user_def / (double)(Integer.valueOf((user_def_multiplier.split(","))[0]))) * 2);
						user_def_multiplier = "2," + theflag[1];
						publishProgress("Your defense has doubled for " + theflag[1] + " turns!");
					}
					else publishProgress("You can't increase your defense any further!");
					return 2;
					case "double_satk" : if((double)Integer.valueOf((user_satk_multiplier.split(","))[0]) <= 1){
						user_satk = (int)((user_satk / (double)(Integer.valueOf((user_satk_multiplier.split(","))[0]))) * 2);
						user_satk_multiplier = "2," + theflag[1];
						publishProgress("Your special attack has doubled for " + theflag[1] + " turns!");
					}
					else publishProgress("You can't increase your special attack any further!");
					return 2;
					case "double_sdef" : if((double)Integer.valueOf((user_sdef_multiplier.split(","))[0]) <= 1){
						user_sdef = (int)((user_sdef / (double)(Integer.valueOf((user_sdef_multiplier.split(","))[0]))) * 2);
						user_sdef_multiplier = "2," + theflag[1];
						publishProgress("Your special defense has doubled for " + theflag[1] + " turns!");
					}
					else publishProgress("You can't increase your special defense any further!");
					return 2;
					case "double_speed" : if((double)Integer.valueOf((user_spd_multiplier.split(","))[0]) <= 1){
						user_spd = (int)((user_spd / (double)(Integer.valueOf((user_spd_multiplier.split(","))[0]))) * 2);
						user_spd_multiplier = "2," + theflag[1];
						publishProgress("Your speed has doubled for " + theflag[1] + " turns!");
					}
					else publishProgress("You can't increase your speed any further!");
					return 2;
					case "halve_attack" : if((double)Integer.valueOf((user_atk_multiplier.split(","))[0]) >= 1){
						user_atk = (int)((user_atk / (double)(Integer.valueOf((user_atk_multiplier.split(","))[0]))) / 2);
						user_atk_multiplier = "0.5," + theflag[1];
						publishProgress("Your attack has halved for " + theflag[1] + " turns!");
					}
					else publishProgress("Your attack won't decrease any further!");
					return 2;
					case "halve_defense" : if((double)Integer.valueOf((user_def_multiplier.split(","))[0]) >= 1){
						user_def = (int)((user_def / (double)(Integer.valueOf((user_def_multiplier.split(","))[0]))) / 2);
						user_def_multiplier = "0.5," + theflag[1];
						publishProgress("Your defense has halved for " + theflag[1] + " turns!");
					}
					else publishProgress("Your defense won't decrease any further!");
					return 2;
					case "halve_satk" : if((double)Integer.valueOf((user_satk_multiplier.split(","))[0]) >= 1){
						user_satk = (int)((user_satk / (double)(Integer.valueOf((user_satk_multiplier.split(","))[0]))) / 2);
						user_satk_multiplier = "0.5," + theflag[1];
						publishProgress("Your special attack has halved for " + theflag[1] + " turns!");
					}
					else publishProgress("Your special attack won't decrease any further!");
					return 2;
					case "halve_sdef" : if((double)Integer.valueOf((user_sdef_multiplier.split(","))[0]) >= 1){
						user_sdef = (int)((user_sdef / (double)(Integer.valueOf((user_sdef_multiplier.split(","))[0]))) / 2);
						user_sdef_multiplier = "0.5," + theflag[1];
						publishProgress("Your special defense has halved for " + theflag[1] + " turns!");
					}
					else publishProgress("Your special defense won't decrease any further!");
					return 2;
					case "halve_speed" : if((double)Integer.valueOf((user_spd_multiplier.split(","))[0]) >= 1){
						user_spd = (int)((user_spd / (double)(Integer.valueOf((user_spd_multiplier.split(","))[0]))) / 2);
						user_spd_multiplier = "0.5," + theflag[1];
						publishProgress("Your speed has halved for " + theflag[1] + " turns!");
					}
					else publishProgress("Your speed won't decrease any further!");
					return 2;
				}
			}
			return 0;
		}
		
		protected String doInBackground(Void...voids){

			//Initate variables
			boolean me_first;
			int damage;
			
			//Action 0 is ATTACK, this is the most standard of battle methods
			if(action == 0){
				
				//If I am faster than the opponent and I'm not paralyzed
				if(user_spd >= enemy_spd && user_status != 2){
					me_first = true;
					publishProgress(user_name + " attacks first!");
					sleeper();
					attackEnemy();
					if(onEnemyContact() == true){
						publishProgress("You died from the damage! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
					
					//Did the opponent die already?
					if(enemy_hp < 1){
						publishProgress(enemy_name + " has been defeated! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = true;
						battle_ends();
						return "yay";
					}
					else{
						//if the opponent has not died yet, then it's his turn to attack
						enemyAttacks();
						onContactByEnemy();
						
						//Did this attack kill the player?
						if(user_curhp < 1){
							publishProgress(enemy_name + " has defeated you! You lost " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = false;
							battle_ends();
							return "yay";
						}
						
						//check enemy then user for poison
						
						if(checkEnemyStatus() == 1){
							publishProgress("The poison has killed the enemy! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = true;
							battle_ends();
							return "yay";
						}
						
						if(checkUserStatus() == 1){
							publishProgress("The poison killed you! You lost " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = false;
							battle_ends();
							return "yay";
						}
					}
				}
				else{
					//In this case, the opponent is faster
					me_first = false;
					publishProgress(enemy_name + " attacks first!");
					sleeper();
					enemyAttacks();
					onContactByEnemy();
					if(user_curhp < 1){
						publishProgress(enemy_name + " has defeated you! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
					else{
						attackEnemy();
						if(onEnemyContact() == true){
							publishProgress("You died from the damage! You lost " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = false;
							battle_ends();
							return "yay";
						}
						if(enemy_hp < 1){
							publishProgress(enemy_name + " has been defeated! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = true;
							battle_ends();
							return "yay";
						}
						
						//check enemy then user for poison
						
						if(checkEnemyStatus() == 1){
							publishProgress("The poison has killed the enemy! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = true;
							battle_ends();
							return "yay";
						}
						if(checkUserStatus() == 1){
							publishProgress("The poison killed you! You lost " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = false;
							battle_ends();
							return "yay";
						}
					}
				}
			}
			else if(action == 1){
				
				int spell_pow = Integer.valueOf(GameEngine.spells_data.get(spell_to_use).get(2));
				int mp_used = Integer.valueOf(GameEngine.spells_data.get(spell_to_use).get(1));
				String spell_name = GameEngine.spells_data.get(spell_to_use).get(0);
				String spell_element = GameEngine.spells_data.get(spell_to_use).get(3); 
				String[] spell_flags = GameEngine.spells_data.get(spell_to_use).get(4).split(",");
				
				if(mp_used > user_curmp){
					publishProgress("You do not have enough MP to use that spell!");
					return "yay";
				}
				
				user_curmp -= mp_used;
				int m_decreaser = (int)Math.floor(((double)user_curmp / (double)user_maxmp) * 100);
				
				//If I am faster than the opponent and I'm not paralyzed
				if(user_spd >= enemy_spd && user_status != 2){
					if(spell_pow != 0){
						damage = (int)((((2.0 * (double)spell_pow / 5.0) * user_satk * randInt(user_level, user_level + 10)) / enemy_sdef) / 10.0) + 2;
						if(spell_element.equalsIgnoreCase(enemy_weakness)) damage = damage * 2;
						if(spell_element.equalsIgnoreCase(enemy_strength)) damage = damage / 2;
						enemy_hp -= damage;
						if(enemy_hp < 1) enemy_hp = 0;
						decreaser = (int)Math.floor(((double)enemy_hp / (double)enemy_maxhp) * 100);
						update_bars(1,decreaser,false);
						update_bars(2,m_decreaser,false);
						publishProgress("You have hurt " + enemy_name + " for " + damage + " HP using the spell " + spell_name + "!");
						sleeper();
						if(enemy_hp < 1){
							publishProgress(enemy_name + " has been defeated! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = true;
							battle_ends();
							return "yay";
						}
					}
					int result = loadSpellFlags(spell_name, spell_flags);
					if(result == 1){ //means the enemy died
						publishProgress(enemy_name + " has been defeated! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = true;
						battle_ends();
						return "yay";						
					}
					if(result == 2) sleeper();
					
					//if the opponent has not died yet, then it's his turn to attack
					enemyAttacks();
					onContactByEnemy();
					
					//Did this attack kill the player?
					if(user_curhp < 1){
						publishProgress(enemy_name + " has defeated you! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
					
					//check enemy then user for poison
					
					if(checkEnemyStatus() == 1){
						publishProgress("The poison has killed the enemy! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = true;
						battle_ends();
						return "yay";
					}
					
					if(checkUserStatus() == 1){
						publishProgress("The poison killed you! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
					
				}
				else {
					publishProgress(enemy_name + " attacks first!");
					sleeper();
					enemyAttacks();
					onContactByEnemy();
					if(user_curhp < 1){
						publishProgress(enemy_name + " has defeated you! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
					if(spell_pow != 0){
						damage = (int)((((2.0 * (double)spell_pow / 5.0) * user_satk * randInt(user_level, user_level + 10)) / enemy_sdef) / 10.0) + 2;
						if(spell_element.equalsIgnoreCase(enemy_weakness)) damage = damage * 2;
						if(spell_element.equalsIgnoreCase(enemy_strength)) damage = damage / 2;
						enemy_hp -= damage;
						if(enemy_hp < 1) enemy_hp = 0;
						decreaser = (int)Math.floor(((double)enemy_hp / (double)enemy_maxhp) * 100);
						update_bars(1,decreaser,false);
						update_bars(2,m_decreaser,false);
						publishProgress("You have hurt " + enemy_name + " for " + damage + " HP using the spell " + spell_name + "!");
						sleeper();
						if(enemy_hp < 1){
							publishProgress(enemy_name + " has been defeated! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
							sleeper();
							sleeper();
							i_won = true;
							battle_ends();
							return "yay";
						}
					}
					int result = loadSpellFlags(spell_name, spell_flags);
					if(result == 1){ //means the enemy died
						publishProgress(enemy_name + " has been defeated! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = true;
						battle_ends();
						return "yay";						
					}
					if(result == 2) sleeper();
					
					//if the opponent has not died yet, then it's his turn to attack
					enemyAttacks();
					onContactByEnemy();
					
					//Did this attack kill the player?
					if(user_curhp < 1){
						publishProgress(enemy_name + " has defeated you! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
					
					//check enemy then user for poison
					
					if(checkEnemyStatus() == 1){
						publishProgress("The poison has killed the enemy! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = true;
						battle_ends();
						return "yay";
					}
					
					if(checkUserStatus() == 1){
						publishProgress("The poison killed you! You lost " + enemy_gold + " gold!");
						sleeper();
						sleeper();
						i_won = false;
						battle_ends();
						return "yay";
					}
				}
				
			}
			else if(action == 2){
				//Here we are adding item use support
				int item_hp = Integer.valueOf(GameEngine.items_data.get(item_to_use).get(1));
				int item_mp = Integer.valueOf(GameEngine.items_data.get(item_to_use).get(2));
				String[] item_flag = GameEngine.items_data.get(item_to_use).get(3).split(",");
				user_curhp += item_hp;
				user_curmp += item_mp;
				if(user_curhp > user_maxhp) user_curhp = user_maxhp;
				if(user_curmp > user_maxmp) user_curmp = user_maxmp;
				increaser = (int)Math.floor(((double)user_curhp / (double)user_maxhp) * 100);
				update_bars(0,increaser,true);
				update_bars(2,((int)Math.floor(((double)user_curmp / (double)user_maxmp) * 100)),true);
				publishProgress(user_name + " has used " + GameEngine.items_data.get(item_to_use).get(0));
				loadItemFlags(GameEngine.items_data.get(item_to_use).get(0), item_flag);
				sleeper();
				enemyAttacks();
				onContactByEnemy();
				if(user_curhp < 1){
					publishProgress(enemy_name + " has defeated you! You lost " + enemy_gold + " gold!");
					sleeper();
					sleeper();
					i_won = false;
					battle_ends();
					return "yay";
				}
				
				//check enemy then user for poison
				
				if(checkEnemyStatus() == 1){
					publishProgress("The poison has killed the enemy! You earned " + enemy_exp + " experience and " + enemy_gold + " gold!");
					sleeper();
					sleeper();
					i_won = true;
					battle_ends();
					return "yay";
				}
				if(checkUserStatus() == 1){
					publishProgress("The poison killed you! You lost " + enemy_gold + " gold!");
					sleeper();
					sleeper();
					i_won = false;
					battle_ends();
					return "yay";
				}
			}
			return "lol";
		}
		
		protected void onProgressUpdate(String... update){
			myhptxt.setText("HP :" + user_curhp + " / " + user_maxhp);
			mymptxt.setText("MP :" + user_curmp + " / " + user_maxmp);
			mystatustxt.setText("Status: " + GameDatabase.STATUS_NAMES[user_status]);
			game_status.setText(game_status.getText() + "\n" + update[0]);
			game_status_holder.post(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					game_status_holder.fullScroll(View.FOCUS_DOWN);
				}
			});
			
		}
		
		protected void onPostExecute(String result){
			battle_turn_end();
		}
		
	}
	
	public void update_bars(final int which, final int towhere, final boolean up){
		
		new Thread(new Runnable() {
			
			public void sleeper(){
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
	        public void run() {
	            if(which == 0){
	            	if(up == false){
	            		while(myhp.getProgress() > towhere){
		            		myhp.post(new Runnable() {
		    	                public void run() {
		    	                    myhp.setProgress(myhp.getProgress() - 1);
		    	                }
		    	            });
		            		sleeper();
		            	}
	            	}
	            	else {
	            		while(myhp.getProgress() < towhere){
		            		myhp.post(new Runnable() {
		    	                public void run() {
		    	                    myhp.setProgress(myhp.getProgress() + 1);
		    	                }
		    	            });
		            		sleeper();
		            	}
	            	}
	            }
	            else if (which == 1){
	            	if(up == false){
	            		while(tahp.getProgress() > towhere){
		            		tahp.post(new Runnable() {
		    	                public void run() {
		    	                    tahp.setProgress(tahp.getProgress() - 1);
		    	                }
		    	            });
		            		sleeper();
		            	}
	            	}
	            	else {
	            		while(tahp.getProgress() < towhere){
		            		tahp.post(new Runnable() {
		    	                public void run() {
		    	                    tahp.setProgress(tahp.getProgress() + 1);
		    	                }
		    	            });
		            		sleeper();
		            	}
	            	}
	            }
	            else if(which == 2){
	            	if(up == false){
	            		while(mymp.getProgress() > towhere){
		            		mymp.post(new Runnable() {
		    	                public void run() {
		    	                    mymp.setProgress(mymp.getProgress() - 1);
		    	                }
		    	            });
		            		sleeper();
		            	}
	            	}
	            	else {
	            		while(mymp.getProgress() < towhere){
		            		mymp.post(new Runnable() {
		    	                public void run() {
		    	                    mymp.setProgress(mymp.getProgress() + 1);
		    	                }
		    	            });
		            		sleeper();
		            	}
	            	}
	            }
	        }
	    }).start();
	}
	
	public void battle_ends(){
		Intent returnIntent = new Intent();
		returnIntent.putExtra("newgold", enemy_gold);
		returnIntent.putExtra("newexp", enemy_exp);
		returnIntent.putExtra("newhp", user_curhp);
		returnIntent.putExtra("newmp", user_curmp);
		returnIntent.putExtra("status", user_status);
		if(i_won == true) returnIntent.putExtra("won", true);
		else returnIntent.putExtra("won", false);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
	

	protected void onClose(){
		if(battle_complete == false){
			Intent returnIntent = new Intent();
			returnIntent.putExtra("user_hpleft", ((double)user_curhp / (double)user_maxhp) * 100);
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
	}
	
	public void attack_listener(){
		b_attack.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				action = 0;
				battle_turn();
			}
	   });
	}
	
	public void spells_listener(){
		b_magic.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(user_spells.length > 0){
					action = 1;
					DialogFragment itemlist = new ListDialog();
					itemlist.show(getFragmentManager(), "spell_select");
				}
			}
	   });
	}
	
	public void item_listener(){
		b_item.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				action = 2;
				DialogFragment itemlist = new ListDialog();
				itemlist.show(getFragmentManager(), "item_select");
			}
	   });
	}
	
	public void flee_listener(){
		b_flee.setOnClickListener(new OnClickListener(){	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				i_won = false;
				battle_ends();
			}
	   });
	}
	
	public void loadItemList(){
		GameDatabase db = new GameDatabase(this);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor entries = reader.rawQuery("SELECT quantity, id FROM itemdata WHERE quantity > 0 AND owner=" + userid, null);
		int i = 0;
		if(entries.getCount() > 0){
			has_items = true;
			entries.moveToFirst();
			if(GameEngine.items_data.get(entries.getInt(entries.getColumnIndex("id"))).get(4).equalsIgnoreCase("true")){
				my_items.add(i, entries.getInt(entries.getColumnIndex("quantity")) + " of " + GameEngine.items_data.get(entries.getInt(entries.getColumnIndex("id"))).get(0));
				item_mapper.add(i, entries.getInt(entries.getColumnIndex("id")));
				i += 1;
			}
			while(entries.moveToNext() == true){
				if(GameEngine.items_data.get(entries.getInt(entries.getColumnIndex("id"))).get(4).equalsIgnoreCase("true")){
					my_items.add(i, entries.getInt(entries.getColumnIndex("quantity")) + " of " + GameEngine.items_data.get(entries.getInt(entries.getColumnIndex("id"))).get(0));
					item_mapper.add(i, entries.getInt(entries.getColumnIndex("id")));
					i += 1;
				}
			}
		}
		else has_items = false;
		entries.close();
		reader.close();
		db.close();
	}
	
	public void loadSpellList(){
		for(int i = 0; i < user_spells.length; i += 1){
			my_spells.add(i, GameEngine.spells_data.get(Integer.valueOf(user_spells[i])).get(0) + " (requires: " + GameEngine.spells_data.get(Integer.valueOf(user_spells[i])).get(1) + "MP)");
			spell_mapper.add(i, Integer.valueOf(user_spells[i]));
		}
	}
	
	public String[] arraylist_toarray(ArrayList<String> arraylist){
		int size = arraylist.size();
		String[] thelist = new String[size];
		for(int i = 0; i < size; i+= 1){
			thelist[i] = arraylist.get(i);
		}
		return thelist;
	}
	
	@SuppressLint("ValidFragment")
	public class ListDialog extends DialogFragment {
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        String[] item_display;
	        if(action == 1){
	        	builder.setTitle("Pick a spell");
	        	item_display = arraylist_toarray(my_spells);
	        }
	        else{
		        builder.setTitle("Pick an item"); 
		        item_display = arraylist_toarray(my_items);
	        }
	        builder.setItems(item_display, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					if(action == 1) spell_to_use = spell_mapper.get(arg1);
					else item_to_use = item_mapper.get(arg1);
					battle_turn();
				}
			});
		return builder.create();
		}
	}
	
	public void doStatChanges(){
		if(user_atk_multiplier != "1,1"){
			String[] values = user_atk_multiplier.split(",");
			if(values[1] == "1"){
				user_atk = (int)(user_atk / (double)(Integer.valueOf(values[0])));
				user_atk_multiplier = "1,1";
			}
			else {
				user_atk_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(user_def_multiplier != "1,1"){
			String[] values = user_def_multiplier.split(",");
			if(values[1] == "1"){
				user_def = (int)(user_def / (double)(Integer.valueOf(values[0])));
				user_def_multiplier = "1,1";
			}
			else {
				user_def_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(user_satk_multiplier != "1,1"){
			String[] values = user_satk_multiplier.split(",");
			if(values[1] == "1"){
				user_satk = (int)(user_satk / (double)(Integer.valueOf(values[0])));
				user_satk_multiplier = "1,1";
			}
			else {
				user_satk_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(user_sdef_multiplier != "1,1"){
			String[] values = user_sdef_multiplier.split(",");
			if(values[1] == "1"){
				user_sdef = (int)(user_sdef / (double)(Integer.valueOf(values[0])));
				user_sdef_multiplier = "1,1";
			}
			else {
				user_sdef_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(user_spd_multiplier != "1,1"){
			String[] values = user_spd_multiplier.split(",");
			if(values[1] == "1"){
				user_spd = (int)(user_spd / (double)(Integer.valueOf(values[0])));
				user_spd_multiplier = "1,1";
			}
			else {
				user_spd_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		
		//enemy stats
		if(enemy_atk_multiplier != "1,1"){
			String[] values = enemy_atk_multiplier.split(",");
			if(values[1] == "1"){
				enemy_atk = (int)(enemy_atk / (double)(Integer.valueOf(values[0])));
				enemy_atk_multiplier = "1,1";
			}
			else {
				enemy_atk_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(enemy_def_multiplier != "1,1"){
			String[] values = enemy_def_multiplier.split(",");
			if(values[1] == "1"){
				enemy_def = (int)(enemy_def / (double)(Integer.valueOf(values[0])));
				enemy_def_multiplier = "1,1";
			}
			else {
				enemy_def_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(enemy_satk_multiplier != "1,1"){
			String[] values = enemy_satk_multiplier.split(",");
			if(values[1] == "1"){
				enemy_satk = (int)(enemy_satk / (double)(Integer.valueOf(values[0])));
				enemy_satk_multiplier = "1,1";
			}
			else {
				enemy_satk_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(enemy_sdef_multiplier != "1,1"){
			String[] values = enemy_sdef_multiplier.split(",");
			if(values[1] == "1"){
				enemy_sdef = (int)(enemy_sdef / (double)(Integer.valueOf(values[0])));
				enemy_sdef_multiplier = "1,1";
			}
			else {
				enemy_sdef_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
		}
		if(enemy_spd_multiplier != "1,1"){
			String[] values = enemy_spd_multiplier.split(",");
			if(values[1] == "1"){
				enemy_spd = (int)(enemy_spd / (double)(Integer.valueOf(values[0])));
				enemy_spd_multiplier = "1,1";
			}
			else {
				enemy_spd_multiplier = values[0] + "," + (Integer.valueOf(values[1]) - 1);
			}
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
}
