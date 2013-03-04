package SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EnigmaBDD {
	private static final int 	DB_VERSION 	= 1;
	private static final String DB_NAME 	= "Enigma.db"; 
	
	private static final String ENIGMA_TABLE 			= "enigma_table" ;	
	private static final String ENIGMA_COL_ID 			= "ID"           ;
	private static final int 	NUM_ENIGMA_COL_ID 		= 0				 ;
	private static final String ENIGMA_COL_SOLUTION 	= "Solution"     ;
	private static final int 	NUM_ENIGMA_COL_SOLUTION = 1				 ;
	
	private static final String CLUES_TABLE 		 	= "clues_table"  ;
	private static final String CLUES_COL_ID 		 	= "ID"           ;
	//private static final int 	NUM_CLUES_COL_ID 	 	= 0			  	 ;
	private static final String CLUES_COL_CLUE 		 	= "Clue"         ;
	//private static final int 	NUM_CLUES_COL_CLUE 	 	= 1			  	 ;
	private static final String CLUES_COL_ENIGMA 	 	= "Enigma_id"    ;
	//private static final int 	NUM_CLUES_COL_ENIGMA 	= 2			  	 ;
	
	
	
	private SQLiteDatabase bdd;
 
	private MySQLiteDatabase mySqliteDatabase;
 
	public EnigmaBDD(Context context){
		//On crée la BDD et sa table
		mySqliteDatabase = new MySQLiteDatabase(context, DB_NAME, null, DB_VERSION);		
	}
 
	public void open(){
		//on ouvre la BDD en écriture
		bdd = mySqliteDatabase.getWritableDatabase();
	}
 
	public void close(){
		//on ferme l'accès à la BDD
		bdd.close();
	}
 
	public SQLiteDatabase getBDD(){
		return bdd;
	}
 
	public long insertEnigma(Enigma enigma){
		long result = -1;
		
		//Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		
		//on lui ajoute une valeur associée à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
		values.put(ENIGMA_COL_SOLUTION, enigma.getEnigmaSolution());		
		
		//on insère l'objet dans la BDD via le ContentValues
		long enigmaId = bdd.insert(ENIGMA_TABLE, null, values);
		
		if(enigmaId >= 0)
		{
			insertClues(enigma, enigmaId);
		}
		
		return result;
	}	
	
	public long insertClues(Enigma enigma, long enigmaId)
	{
		long result = 0;
		
		if(enigma.getClueList() != null && enigma.getClueList().size() > 0)
		{
			long temp = 0;
			
			for(String clue : enigma.getClueList())
			{
				temp = insertClue(clue, enigmaId);
				
				if(temp < 0)
					break;
				
				result += temp;
			}				
		}		
		else
			result = Long.MIN_VALUE;
		
		return result;
	}
	
	public long insertClue (String clue, long enigmaId)
	{
		//Création d'un ContentValues (fonctionne comme une HashMap)
		ContentValues values = new ContentValues();
		
		//on lui ajoute une valeur associée à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
		values.put(CLUES_COL_CLUE  , clue    );
		values.put(CLUES_COL_ENIGMA, enigmaId);
		
		//on insère l'objet dans la BDD via le ContentValues
		return bdd.insert(CLUES_TABLE, null, values);
	}
 
	public int updateEnigma(int id, Enigma enigma){
		int result = -1;
		
		//La mise à jour d'une énigme dans la BDD fonctionne plus ou moins comme une insertion
		//il faut simplement préciser quelle énigme on doit mettre à jour grâce à l'ID
		ContentValues values = new ContentValues();
		values.put(ENIGMA_COL_SOLUTION, enigma.getEnigmaSolution());
		
		result = bdd.update(ENIGMA_TABLE, values, ENIGMA_COL_ID + " = " +id, null);
		
		if(result > 0)
		{
			updateClues(id, enigma);
		}
		
		return result;
	}
	
	public long updateClues(int enigmaId, Enigma enigma)
	{
		long result = 0;
		
		if(enigma.getClueList() != null && enigma.getClueList().size() > 0)
		{
			long temp = -1;
			
			// TODO : contrôles ?
			removeCluesWithEnigmaID(enigmaId);
			
			for(String clue : enigma.getClueList())
			{
				temp = insertClue(clue, enigmaId);
				
				if(temp == 0)
					break;
				
				result += temp;
			}				
		}		
		else
			result = Long.MIN_VALUE;
		
		return result;
	}
 
	public int removeEnigmaWithID(int id){
		int result = 0;
		
		//Suppression d'une énigme de la BDD grâce à l'ID
		result += bdd.delete(ENIGMA_TABLE, ENIGMA_COL_ID + " = " +id, null);
		//Suppression des clues de l'énigme également
		result += removeCluesWithEnigmaID(id);
		
		return result;
	}
	
	public int removeCluesWithEnigmaID(int enigmaId)
	{
		return bdd.delete(CLUES_TABLE, CLUES_COL_ENIGMA + "=?", new String[]{String.valueOf(enigmaId)});
	}
 
	private final String MY_QUERY = "SELECT " + CLUES_COL_CLUE + 
									" FROM "  + CLUES_TABLE      + " clues INNER JOIN "  + ENIGMA_TABLE + " enigma" +
									" ON clues." + CLUES_COL_ENIGMA + "=enigma." + ENIGMA_COL_ID + 
									" WHERE enigma." + ENIGMA_COL_SOLUTION + "=?" ;
	public Enigma getEnigmaWithSolution(String solution){
		//Récupère dans un Cursor les valeurs correspondant à une énigme contenu dans la BDD (ici on sélectionne l'énigme grâce à sa solution)
		Cursor c1 = bdd.query(ENIGMA_TABLE, new String[] {ENIGMA_COL_ID, ENIGMA_COL_SOLUTION}, ENIGMA_COL_SOLUTION + " LIKE \"" + solution +"\"", null, null, null, null);
		Cursor c2 = bdd.rawQuery(MY_QUERY, new String[]{solution});
		return cursorsToEnigma(c1, c2);
	}
 
	//Cette méthode permet de convertir un cursor en une énigme
	private Enigma cursorsToEnigma(Cursor c1, Cursor c2){
		//si aucun élément n'a été retourné dans la requête, on renvoie null
		if (c1.getCount() == 0)
			return null;
		if (c2.getCount() == 0)
			return null;
 
		//Sinon on se place sur le premier élément
		c1.moveToFirst();
		//On créé une énigme
		Enigma enigma = new Enigma();
		//on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
		enigma.setId(c1.getInt(NUM_ENIGMA_COL_ID));
		enigma.setEnigmaSolution(c1.getString(NUM_ENIGMA_COL_SOLUTION));		
		//On ferme le cursor
		c1.close();		
		
		
		c2.moveToFirst();
		do enigma.getClueList().add(c2.getString(0));
		while(c2.moveToNext());
		c2.close();
 
		
		//On retourne l'énigme
		return enigma;
	}
	
	private final String QUERY = "SELECT "+ ENIGMA_COL_SOLUTION + " FROM " + ENIGMA_TABLE;
	public List<Enigma> getAllEnigmas()
	{		
		//Récupère toutes les énigmes
		Cursor c = bdd.rawQuery(QUERY, null);
			
		if(c.getCount() == 0) return null ;
		
		List<Enigma> enigmas = new ArrayList<Enigma>();
		
		c.moveToFirst();
		do enigmas.add(getEnigmaWithSolution(c.getString(0)));
		while(c.moveToNext());		
		c.close();
			
		return enigmas;
	}
}
