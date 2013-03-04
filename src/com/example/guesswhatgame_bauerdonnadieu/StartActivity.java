package com.example.guesswhatgame_bauerdonnadieu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import SQLiteDatabase.Enigma;
import SQLiteDatabase.EnigmaBDD;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class StartActivity extends Activity 
{
	private final int RESULT_CLOSE_ALL = 30;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}
	
	public void goToMapActivityClick(View v)
    {	
		Enigma enigma = chooseRandomEnigma(); 
		
		if(enigma != null)
		{
			//Toast.makeText(this, enigma.toString(), Toast.LENGTH_LONG).show();	 
			Intent activity = new Intent(this,MyMapActivity.class);			
											
			activity.putExtra(this.getResources().getString(R.string.enigma_solution_var), enigma.getEnigmaSolution());			
			ArrayList<String> clues = new ArrayList<String>();
			
			for(String clue : enigma.getClueList()) clues.add(clue);
			
			String cluesVarNames = this.getResources().getString(R.string.clues_var_names);			
				
			activity.putExtra(cluesVarNames, clues);
			startActivityForResult(activity, RESULT_CLOSE_ALL);
		}
		else
			Toast.makeText(this, "No enigmas in the database.\nPlease hit the \"" + this.getResources().getString(R.string.update) + "\" button", Toast.LENGTH_LONG).show();	 
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch ( resultCode)
		{
			case RESULT_CLOSE_ALL:
				setResult(RESULT_CLOSE_ALL);
				StartActivity.this.finish();
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public Enigma chooseRandomEnigma()
	{	
		//Cr�ation d'une instance de la base SQLite
        EnigmaBDD enigmaDB = new EnigmaBDD(this);        
        
        //On ouvre la base de donn�es pour �crire dedans
        enigmaDB.open();
        
        //Chargement des �nigmes
        List<Enigma> enigmas = enigmaDB.getAllEnigmas();
        
        enigmaDB.close();
        
        if(enigmas == null || enigmas.size() == 0)        
        	return null;
        else
        	return enigmas.get((int)(Math.random() * (enigmas.size())));
	}
	
	public void updateEnigmasClick(View v)
	{
		int nbAdded   = 0;
		int nbUpdated = 0;
		int nbFailed  = 0;
		boolean update = false;
		
		//Cr�ation d'une instance de la base SQLite
        EnigmaBDD enigmaDB = new EnigmaBDD(this);
 
        //Cr�ation des �nigmes    
        List<Enigma> enigmas = generateFakeEnigmas();
        
        //On ouvre la base de donn�es pour �crire dedans
        enigmaDB.open();
        
        for(Enigma enigma : enigmas)
        {
        	update = false;
        	
	        //Pour v�rifier que l'on a bien cr�� notre �nigme dans la BDD
	        //on extrait l'�nigme de la BDD gr�ce � sa solution
	        Enigma enigmaFromDB = enigmaDB.getEnigmaWithSolution(enigma.getEnigmaSolution());
	        
	        //Si une �nigme est retourn�e (donc si l'�nigme � bien �t�e ajout�e � la BDD)
	        if(enigmaFromDB != null){
	        	//On met � jour l'�nigme dans la BDD
		        enigmaDB.updateEnigma(enigmaFromDB.getId(), enigma);  
		        update = true;
	        } 
	        else
	        {
	        	//On ins�re l'�nigme que l'on vient de cr�er
		        enigmaDB.insertEnigma(enigma);
	        }	
	        
	        //On v�rifie � nouveau
	        enigmaFromDB = enigmaDB.getEnigmaWithSolution(enigma.getEnigmaSolution());
	        if(enigmaFromDB != null){
	        	//On affiche les infos de l'�nigme dans un Toast
	        	//Toast.makeText(this, enigmaFromDB.toString(), Toast.LENGTH_LONG).show();	 
	        	
	        	if(update) nbUpdated++;
	        	else       nbAdded++  ;
	        } 
	        else
	        {
	        	nbFailed++;
	        	Toast.makeText(this, "An error occured why trying to add this enigma:\n\n"+enigma.toString(), Toast.LENGTH_LONG).show();	        	
	        }   
        }
 
        Toast.makeText(this, nbAdded + " enigmas added\n" + nbUpdated + " enigmas updated\n" + nbFailed + " enigmas failed", Toast.LENGTH_LONG).show();
        
        enigmaDB.close();
	}
	
	private List<Enigma> generateFakeEnigmas()
	{
		List<Enigma> enigmas = new ArrayList<Enigma>();
		
		    
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Apple", "OS", "Computeur"}), 
        				"Mac"
        				)
        		);
        
        
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Gift", "Snow", "December", "Fir"}), 
        				"Christmas"
        				)
        		);
        
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Internet", "Follow", "Blue bird"}), 
        				"Twitter"
        				)
        		);
        
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Internet", "Friends", "Photos", "Chat", "Network", "Social"}), 
        				"Facebook"
        				)
        		);
        
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Snow", "Sport", "Slide", "Slope", "Sticks"}), 
        				"Ski"
        				)
        		);
        
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Yellow", "Ball", "Hot", "Summer", "Sky"}), 
        				"Sun"
        				)
        		);
        
        enigmas.add(
        		new Enigma(
        				Arrays.asList(new String [] {"Music", "Move", "Rhythm"}), 
        				"Dance"
        				)
        		);
		
		return enigmas;
	}

}
