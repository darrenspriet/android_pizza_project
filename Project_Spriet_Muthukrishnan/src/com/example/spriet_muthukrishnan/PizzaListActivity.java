package com.example.spriet_muthukrishnan;


import com.example.spriet_muthukrishnan.db.PizzaDataSource;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PizzaListActivity extends ListActivity {
	
	// private data field to hold the currently selected student
	// set in the onCreateContextMenu and used in the onContextItemSelected
	private Pizza pizza;
	
	// constants used to differentiate which task
	// to perform asynchronously
	private static final long LOAD_PIZZAS = 0;
	private static final long REMOVE_PIZZA = 1; 
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_list);
        
        // do the db access using an AsyncTask
        DatabaseTask dbTask = new DatabaseTask();
        dbTask.execute(LOAD_PIZZAS);
        
        // register the ListView as a source for a context menu
        registerForContextMenu(getListView());
    }
    

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		// get the ListView
		ListView lv = (ListView) v;
		
		// Get the AdapterContextMenuInfo corresponding to the menuInfo passed in
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		
		// get the pizza at the position selected. Calls the getItem method of our 
		// PizzaCursorAdapter. Have to cast from Object
		pizza = (Pizza) lv.getItemAtPosition(info.position);

		// Setting the title of the context menu to the String representation
		// of a Pizza instance
		String toppings = pizza.getToppingsList();
		String[] separated = toppings.split("\n");
		toppings = separated[0];
		for (int i = 1; i < separated.length; i++) {
			
			toppings += ", " + separated[i];
		}
		String title = Pizza.getSizeString(pizza.getSize()) + " pizza with " + toppings.toLowerCase();
		menu.setHeaderTitle(title);
		
		// add two menu items
		menu.add("Remove from History");		
		menu.add("Load to Order");
	}

    
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (item.getTitle().equals("Remove from History")) {

	        // do the db access using an AsyncTask
	        DatabaseTask dbTask = new DatabaseTask();
	        dbTask.execute(REMOVE_PIZZA, pizza.getDbId());
			return true;
		} else if (item.getTitle().equals("Load to Order")) {
			// put code here that will return the pizza to the parent 
			// Activity			
			loadToOrder(Pizza.getSizeString(pizza.getSize()), pizza.getToppingsList());
			return true;
		} 
		
		return super.onContextItemSelected(item);
	}
	
	// send success along with total back
	private void loadToOrder(String size, String toppings) {
		Intent intent = new Intent();
		intent.putExtra("size", size);
		intent.putExtra("toppings", toppings);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private class DatabaseTask extends AsyncTask<Long, Void, Cursor> {
		@Override
		protected Cursor doInBackground(Long... params) {

			// create PizzaDataSource and call saveStudent method
			PizzaDataSource pizzaData = new PizzaDataSource(getBaseContext());
			
			if (params[0] == REMOVE_PIZZA) {
				// remove a pizza from the db 
				// second parameter is the db id of the pizza
				pizzaData.removePizza(params[1]);
			}
			// return Cursor of all pizzas to the onPostExecute
			return pizzaData.getAllPizzas();
		}
		
		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);

	        // use our cursor adapter and pass in the resulting cursor
			PizzaCursorAdapter adapter = new PizzaCursorAdapter(PizzaListActivity.this, 
					R.layout.list_item_row, result);
	        
			// set the adapter for the list
	        setListAdapter(adapter);
		}
	}
}

