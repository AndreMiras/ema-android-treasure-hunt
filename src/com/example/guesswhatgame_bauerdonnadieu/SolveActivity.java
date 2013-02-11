package com.example.guesswhatgame_bauerdonnadieu;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

public class SolveActivity extends Activity 
{
	private final int RESULT_CLOSE_ALL = 30;
	
	// Indices des boites de dialogues à afficher
	private static final int DIALOG_WRONG = 10;
	private static final int DIALOG_GOOD  = 20;
	
	// Solution de l'énigme
	private static String enigmaSolution = "toto a la plage";

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solve);
		
		Bundle extras = getIntent().getExtras();		
		if(extras != null) 
		{
			String cluesVarNames = this.getResources().getString(R.string.clues_var_names);
			ArrayList<String> cluesList = extras.getStringArrayList(cluesVarNames);	
			
			if(cluesList != null)
			{
				// TODO : DEBUG - Exemple d'utilisation réutilisant (salement) le code déjà en place
				// afficher plutot dans une liste scrollable pour pouvoir afficher N Clues
				int i=0;
				
				for (String clue : cluesList)
				{					
					if (i==0)
						((TextView)findViewById(R.id.textClue1Val)).setText(clue.split(this.getResources().getString(R.string.clues_var_separator))[1]);
					if (i==1)
						((TextView)findViewById(R.id.textClue2Val)).setText(clue.split(this.getResources().getString(R.string.clues_var_separator))[1]);
					if (i==2)
						((TextView)findViewById(R.id.textClue3Val)).setText(clue.split(this.getResources().getString(R.string.clues_var_separator))[1]);
					i++;
				}
			}			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		//this adds items to the action bar if it is present.
		//Inflate themenu;
		
		getMenuInflater().inflate(R.menu.activity_solve, menu);
		return true;
	}
	
	public void goToMapActivityClick(View v) 
	{
		SolveActivity.this.finish();
	}

	@SuppressWarnings("deprecation")
	public void onTryClick(View v) 
	{
		boolean found = false;
		
		// Récupération de la saisie utilisateur
		EditText ev = (EditText)findViewById(R.id.editSolution);
		Editable ed = ev.getText();
		String userResponse = ed.toString();
		
		userResponse   = userResponse  .replaceAll("\\s", "");
		enigmaSolution = enigmaSolution.replaceAll("\\s", "");
		
		// Comparaison avec la solution de l'énigme
		if ( userResponse.equalsIgnoreCase( enigmaSolution ) )
			found = true;		
		
		// Affichage de la dialog correspondante
		if ( found ) showDialog(DIALOG_GOOD );		
		else         showDialog(DIALOG_WRONG);
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		switch (id) 
		{
			case DIALOG_GOOD:
			{
				// Create out AlterDialog
				Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Congratulation");
				builder.setCancelable(true);
				builder.setPositiveButton("Exit", new OkOnClickListener());
				//builder.setNegativeButton("No, no", new CancelOnClickListener());
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			break;
			case DIALOG_WRONG:
			{
				// CreateoutAlterDialog
				Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Wrong answer");
				builder.setCancelable(true);
				//builder.setPositiveButton("I agree", new OkOnClickListener());
				builder.setNegativeButton("Again", new CancelOnClickListener());
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			break;
		}

		return super.onCreateDialog(id);
	}

	private final class CancelOnClickListener implements DialogInterface.OnClickListener 
	{
		public void onClick(DialogInterface dialog, int which) 
		{
			Toast.makeText(getApplicationContext(), "Try again (don't forget to use the clues)",
					Toast.LENGTH_LONG).show();
		}
	}

	private final class OkOnClickListener implements DialogInterface.OnClickListener 
	{
		public void onClick(DialogInterface dialog, int which) 
		{
			// Lance la fermeture en cascade des activités de l'application
			setResult(RESULT_CLOSE_ALL);
			SolveActivity.this.finish();
		}
	}
}
