package com.example.spriet_muthukrishnan.db;

import com.example.spriet_muthukrishnan.Pizza;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PizzaDataSource {
	
	// instance members for accessing the database
	private SQLiteDatabase database;
	private SQLiteOpenHelper dbOpenHelper;

	// static members for describing the Pizza table structure
	public static final String TABLE_NAME = "Pizzas";

	public static final String ID_COLUMN = "_id";
	public static final int ID_COLUMN_POSITION = 0;
	public static final String SIZE_COLUMN = "size";
	public static final int SIZE_COLUMN_POSITION = 1;
	public static final String TOPPINGS_COLUMN = "toppings";
	public static final int TOPPINGS_COLUMN_POSITION = 2;
	
	public static final String CREATE_TABLE = 
			"CREATE TABLE " + TABLE_NAME + " (" +
			ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			SIZE_COLUMN + " TEXT, " +
			TOPPINGS_COLUMN + " TEXT)";

	// constructor; must pass in Context
	public PizzaDataSource(Context context) {
		// instantiate our helper class
		dbOpenHelper = new PizzaDBOpenHelper(context);
	}

	// method for saving pizza order info into database
	public Pizza savePizza(Pizza pizza, boolean unique, boolean keepHistory, int maxOrders) {
		
		if(!keepHistory) {
			return null;
		}
		// get writable database since we want to insert a row
		if (database == null || !database.isOpen() || database.isReadOnly()) {
			database = dbOpenHelper.getWritableDatabase();
		}
		
		// use ContentValues to group field names and values
		ContentValues values = new ContentValues();
		values.put(SIZE_COLUMN, Pizza.getSizeString(pizza.getSize()));
		String toppings = pizza.getToppingsList();
		String[] separated = toppings.split("\n");
		toppings = separated[0];
		for (int i = 1; i < separated.length; i++) {
			
			toppings += ", " + separated[i];
		}
		values.put(TOPPINGS_COLUMN, toppings);
		
		Cursor duplicates = database.rawQuery("SELECT * from Pizzas WHERE size = " + "'" +
											Pizza.getSizeString(pizza.getSize()) + "'" + 
											" AND toppings = " + "'" + toppings + "'", null);
		// If history needs to be unique, and there is a duplicate
		// remove the duplicate before inserting
		if (unique && duplicates.getCount() > 0)
		{
			duplicates.moveToFirst();	// There should ONLY be one!!
			long _id = duplicates.getLong(ID_COLUMN_POSITION);
			database.delete(TABLE_NAME, "_id = ?", new String[] {String.valueOf(_id)});
		}
		
		Cursor records = database.rawQuery("SELECT * from Pizzas", null);
		// Delete the first record before adding another one if there are too many
		if (records.getCount() >= maxOrders) {
			
			records.moveToFirst();
			long _id = records.getLong(ID_COLUMN_POSITION);
			database.delete(TABLE_NAME, "_id = ?", new String[] {String.valueOf(_id)});
		}
		
		long dbId = database.insert(TABLE_NAME, null, values);
		pizza.setDbId(dbId);
		
		return pizza; // return pizza with dbId assigned
	}
	
	//Updates the Show History to Unique when the user switches to unique orders
	public void updatingDatabaseToUniqueEntries(){
		
		// get writable database since we want to insert a row
		if (database == null || !database.isOpen() || database.isReadOnly()) {
			database = dbOpenHelper.getWritableDatabase();
		}
		//gets all the records
		Cursor allRecords = database.rawQuery("SELECT * from Pizzas", null);
		//go through all the records
		while(allRecords.moveToNext())
		{
			//pulls out the size and toppings
		    String pizzaSize=allRecords.getString(SIZE_COLUMN_POSITION);
		    String toppings=allRecords.getString(TOPPINGS_COLUMN_POSITION);

		    //creates a duplicate query
			Cursor duplicates = database.rawQuery("SELECT * from Pizzas WHERE size = " + "'" +
					pizzaSize + "'" + 
					" AND toppings = " + "'" + toppings + "'", null);
			
			//If there are duplicates we go through and remove the duplicate
			if(duplicates.getCount() >1)
			{
				duplicates.moveToFirst();	// There should ONLY be one!!
				long _id = duplicates.getLong(ID_COLUMN_POSITION);
				database.delete(TABLE_NAME, "_id = ?", new String[] {String.valueOf(_id)});			
			}	
		}
	}
	
	// method for getting all pizza records in a Cursor
	public Cursor getAllPizzas() {
		
		// get readable database since we only want to read
		if (database == null || !database.isOpen()) {
			database = dbOpenHelper.getReadableDatabase();
		}
		
		// this will return all records form the table
		//Cursor pizzas = database.query(TABLE_NAME, null, null, null, null, null, null);

		// could also use below
		Cursor pizzas = database.rawQuery("SELECT * from Pizzas", null);
		
		return pizzas;
	}
	
	// Update the database to reflect number of orders to keep in history
	public void updateDBNumberOfOrders(int maxOrders) {
		
		// get readable database since we only want to read
		if (database == null || !database.isOpen()) {
			database = dbOpenHelper.getReadableDatabase();
		}
		
		// Get a list of records
		Cursor pizzas = database.rawQuery("SELECT * from Pizzas", null);
		// Don't do anything if there aren't enough records
		if (pizzas.getCount() <= maxOrders) {
			return;
		}
		else {
			pizzas.moveToFirst();
			int deletedRecords = 0;
			while (deletedRecords < pizzas.getCount() - maxOrders)
			{
				long _id = pizzas.getLong(ID_COLUMN_POSITION);
				database.delete(TABLE_NAME, "_id = ?", new String[] {String.valueOf(_id)});
				pizzas.moveToNext();
				deletedRecords++;
			}
		}
		
	}
	
	// Update the database to reflect keep history preference
	public void updateDBKeepHistory(boolean keepHistory) {
		
		// Don't do anything if they want to keep the history
		if (keepHistory) {
			return;
		}
		else {
			// get readable database since we only want to read
			if (database == null || !database.isOpen()) {
				database = dbOpenHelper.getReadableDatabase();
			}
			// Delete the records
			database.delete(TABLE_NAME, null, null);
		}
	}

	public void removePizza(long _id) {
		
		// get writable database since we want to delete a row
		if (database == null || !database.isOpen() || database.isReadOnly()) {
			database = dbOpenHelper.getWritableDatabase();
		}
		
		database.delete(TABLE_NAME, "_id = ?", new String[] {String.valueOf(_id)});
		
	}
}
