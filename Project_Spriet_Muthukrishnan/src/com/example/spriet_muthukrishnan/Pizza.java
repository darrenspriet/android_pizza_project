package com.example.spriet_muthukrishnan;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

// Pizza class to hold both static info/methods (true to all pizzas)
// and instance data for a particular pizza 
public class Pizza implements Parcelable {

	// some static constants to keep pizza-related info in one place
	public static final int SMALL = 0;
	public static final int MEDIUM = 1;
	public static final int LARGE = 2;
	private static final String TAG = "MainActivity";


	public static final String CHEESE = "Cheese";
	public static final String GREEN_PEPPER = "Green Pepper";
	public static final String PEPPERONI = "Pepperoni";
	public static final String SAUSAGE = "Sausage";
	public static final String BACON = "Bacon";
	
	private int smallBasePrice;
	private int mediumBasePrice;
	private int largeBasePrice;
	
	private double smallFactor;
	private double mediumFactor;
	private double largeFactor;
	
	private double cheesePrice;
	private double greenPepperPrice;
	private double pepperoniPrice;
	private double sausagePrice;
	private double baconPrice;
	
	
	// instance variable for size and toppings
	private int size;
	private ArrayList<String> toppings;
	private double pizzaPrice;

	// constructor for default so we can have a Pizza that is empty to use
	public Pizza() {
		super();
		this.size = Pizza.SMALL;
		toppings = new ArrayList<String>();
		pizzaPrice = 0.0;
		smallBasePrice = 0;
		mediumBasePrice = 0;
		largeBasePrice = 0;
		smallFactor = 0;
		mediumFactor = 0;
		largeFactor = 0;
		cheesePrice = 0.0;
		greenPepperPrice = 0.0;
		pepperoniPrice = 0.0;
		sausagePrice = 0.0;
		baconPrice = 0.0;
		
	}
	

	// constructor with Parcel input for creating instance from Parcel
	// Called by runtime in readParcelable in PaymentActivity
	public Pizza(Parcel source) {
		size = source.readInt();

		// initialize toppings so it is not null
		toppings = new ArrayList<String>();

		// call readStringList passing in our toppings ArrayList
		source.readStringList(toppings);
		
		pizzaPrice = source.readDouble();
		

	}
	public double getPizzaPrice(){
		return pizzaPrice;
	}
	public void setPizzaPrice(double _pizzaPrice){
		pizzaPrice = _pizzaPrice;
	}

	public int getSize() {
		return size;
	}
	
	public void setSize(int _size) {
		size = _size;
	}

	// return string representation of size
	public static String getSizeString(int size) {
		switch (size) {
		case SMALL:
			return "Small";
		case MEDIUM:
			return "Medium";
		case LARGE:
			return "Large";
		default:
			return "Undefined"; // should never get here
		}

	}

	// returns price of pizza before toppings
	public double getBasePrice(int size) {
		switch(size) {
		case SMALL:
			return smallBasePrice;
		case MEDIUM:
			return mediumBasePrice;
		case LARGE:
			return largeBasePrice;
		default:
			return 0; // should never get here
		}
	}

	//  method for getting topping price given topping and size
	public double getToppingPrice(int size, String topping) {	
			double addFee = 0; // additional fee per size increase from small
			
		// can use the following since fees increase evenly by size
		switch (size) {
		case Pizza.SMALL:
			addFee = smallFactor; // not necessary, but for completeness & readability
			break;
		case Pizza.MEDIUM:
			addFee = mediumFactor;
			break;
		case Pizza.LARGE:
			addFee = largeFactor;
			break;
		}

		if (topping.equals(CHEESE)) {
			return cheesePrice + addFee;
		}
		if (topping.equals(GREEN_PEPPER)) {
			return  greenPepperPrice+ addFee;
		}
		if (topping.equals(PEPPERONI)) {
			return pepperoniPrice + addFee;
		}
		if (topping.equals(SAUSAGE)) {
			return sausagePrice + addFee;
		}
		if (topping.equals(BACON)) {
			return baconPrice + addFee;
		}
		return 0; // should never get here	
	}

	public void LoadFromFile(String jsonData){
		
		try {
		Log.e(TAG, "Pizza info is here"+jsonData);

		//Now use the Data to fill in the getTopping Price infromation!!!!
		JSONObject wrapper = new JSONObject(jsonData);
		
		JSONObject pizzaPrices = wrapper.getJSONObject("pizza-prices");
		smallBasePrice = pizzaPrices.getInt("small");
		mediumBasePrice = pizzaPrices.getInt("medium");
		largeBasePrice = pizzaPrices.getInt("large");
		smallFactor = pizzaPrices.getDouble("small-factor");
		mediumFactor = pizzaPrices.getDouble("medium-factor");
		largeFactor = pizzaPrices.getDouble("large-factor");
		
		JSONArray toppingsList = pizzaPrices.getJSONArray("toppings");
		for (int i = 0; i < toppingsList.length(); i++) {
			JSONObject toppingObject = toppingsList.getJSONObject(i);

			switch (i) {
			case 0:
				cheesePrice = toppingObject.getDouble("price");
				Log.e(TAG,"0 "+ toppingObject.getDouble("price"));
				break;
			case 1:
				greenPepperPrice = toppingObject.getDouble("price");
				Log.e(TAG,"1 "+ toppingObject.getDouble("price"));		
				break;
			case 2:
				pepperoniPrice = toppingObject.getDouble("price");
				Log.e(TAG,"2 "+ toppingObject.getDouble("price"));
				break;		
			case 3:
				sausagePrice = toppingObject.getDouble("price");
				Log.e(TAG,"3 "+ toppingObject.getDouble("price"));			
				break;
			case 4:
				baconPrice= toppingObject.getDouble("price");
				Log.e(TAG,"4 "+ toppingObject.getDouble("price"));
				break;
			default:
				break;
			}
		}
			} catch (JSONException e) {
				Log.e(TAG + ": in parsing the Pizza", e.toString());
			}
	}
	


	// instance method since we want the price of a specific pizza
	public double getPrice() {
		// first get base price
		double price = getBasePrice(size);

		// now get cost of additional toppings
		Iterator<String> toppingIter = toppings.iterator();
		while (toppingIter.hasNext()) {
			price += getToppingPrice(size, toppingIter.next());
		}

		return price;
	}

	// instance method since we want the topping list of a specific pizza
	// returns a String that is displayed in PaymentActivity
	public String getToppingsList() {
		StringBuilder toppingsList = new StringBuilder();

		// now get listing of all toppings
		Iterator<String> toppingIter = toppings.iterator();
		while (toppingIter.hasNext()) {
			toppingsList.append(toppingIter.next());
			if (toppingIter.hasNext()) {
				toppingsList.append("\n"); // add new line
			}
		}
		
		if (toppingsList.length() > 0) {
			return toppingsList.toString();
		} else {
			// No toppings.
			return "No toppings";
		}
	}
	//Clears toppings when trying to save a type of order
	public void clearToppings(){
		toppings = new ArrayList<String>();
		toppings.clear();
	}

	// add a topping to the pizza
	public void addTopping(String topping) {
		
		if (toppings == null) {
			toppings = new ArrayList<String>();
		}
		toppings.add(topping);
	}

	public ArrayList<String> getToppings() {
		return toppings;
	}

	@Override // from Parcelable
	public int describeContents() {
		return 0; // return default
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// flatten the object into the Parcel for marshalling
		dest.writeInt(size);
		dest.writeStringList(toppings);
		setPizzaPrice(getPrice());
		dest.writeDouble(pizzaPrice);
	}

	// CREATOR class for the parcelling into the intent
	public static final Parcelable.Creator<Pizza> CREATOR = new Creator<Pizza>() {

		@Override
		public Pizza[] newArray(int size) {
			return new Pizza[size];
		}

		@Override
		public Pizza createFromParcel(Parcel source) {
			return new Pizza(source);
		}
	};


	
	
	
}
