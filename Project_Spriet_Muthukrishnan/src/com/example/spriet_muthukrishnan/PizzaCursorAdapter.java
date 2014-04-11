package com.example.spriet_muthukrishnan;

import com.example.spriet_muthukrishnan.db.PizzaDataSource;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class PizzaCursorAdapter extends ResourceCursorAdapter {

	int layoutResourceId; // will hold resource id of list_item_row.xml
	Cursor pizzas; // cursor returned from db query

	public PizzaCursorAdapter(Context context, int layoutResourceId, Cursor pizzas) {
		super(context, layoutResourceId, pizzas, 0);
		this.layoutResourceId = layoutResourceId;
		this.pizzas = pizzas;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		
		// Inflate the row that will be used to display the info
		LayoutInflater inflator = ((Activity)context).getLayoutInflater();
		View row = inflator.inflate(layoutResourceId, parent, false);
		
		return row; // return the row
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		
		// get the row widgets and bind the data to them; called when 
		// rows become visible
		TextView txtSize = (TextView) row.findViewById(R.id.rowTxtSize);
		TextView txtToppings = (TextView) row.findViewById(R.id.rowTxtToppings);
		
		// set values; cursor is already at proper position
		txtSize.setText(cursor.getString(PizzaDataSource.SIZE_COLUMN_POSITION));
		txtToppings.setText(cursor.getString(PizzaDataSource.TOPPINGS_COLUMN_POSITION));
	}

	@Override
	public Object getItem(int position) {
		
		// get the Pizza that corresponds to the position in cursor
		// first move the cursor to the correct position
		pizzas.moveToPosition(position);
		
		// put cursor values in variables
		String size = pizzas.getString(PizzaDataSource.SIZE_COLUMN_POSITION);
		String toppings = pizzas.getString(PizzaDataSource.TOPPINGS_COLUMN_POSITION);
		long dbId = pizzas.getLong(PizzaDataSource.ID_COLUMN_POSITION);	
		
		// Create and return new instance
		Pizza pizza = new Pizza(dbId);
		if (size.equals("Small")) {
			pizza.setSize(0);
		}
		else if (size.equals("Medium")) {
			pizza.setSize(1);
		}
		else if (size.equals("Large")) {
			pizza.setSize(2);
		}
		String[] separated = toppings.split("\n");
		for (String string : separated) {
			
			pizza.addTopping(string);
		}
		return pizza;
	}	
}
