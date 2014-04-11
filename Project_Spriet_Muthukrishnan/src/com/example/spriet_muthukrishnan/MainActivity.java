package com.example.spriet_muthukrishnan;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements 	RadioGroup.OnCheckedChangeListener, 
														SharedPreferences.OnSharedPreferenceChangeListener{

	// declare controls that will be used in more than one method
	CheckBox chkCheese;
	CheckBox chkPeperoni;
	CheckBox chkSausage;
	CheckBox chkBacon;
	CheckBox chkPepper;
	RadioGroup grpSize;
	Button btnPayment;
	MenuItem saveItem; 
	MenuItem loadItem; 
	MenuItem showHistory; 
	TextView priceFile;
	RadioButton rbtnSmall;
	RadioButton rbtnMedium;
	RadioButton rbtnLarge;

	// now that we're ready, create the pizza!
	private Pizza pizza;
	private static final String TAG = "MainActivity";


	// for requestCode in startActivityforResult and onActivityResult
	private static int PAYMENT_ACTIVITY = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		grpSize = (RadioGroup) findViewById(R.id.grpSize);
		// set MainActivity to be the listener
		grpSize.setOnCheckedChangeListener(this);

		// initialize those controls we'll be using
		chkCheese = (CheckBox) findViewById(R.id.chkCheese);
		chkPeperoni = (CheckBox) findViewById(R.id.chkPeperoni);
		chkSausage = (CheckBox) findViewById(R.id.chkSausage);
		chkBacon = (CheckBox) findViewById(R.id.chkBacon);
		chkPepper = (CheckBox) findViewById(R.id.chkPepper);

		btnPayment = (Button) findViewById(R.id.btnPayment);
		priceFile = (TextView) findViewById(R.id._txtPriceFile);
		rbtnSmall = (RadioButton) findViewById(R.id.radSmall);
		rbtnMedium = (RadioButton) findViewById(R.id.radMedium);
		rbtnLarge = (RadioButton) findViewById(R.id.radLarge);
		pizza = new Pizza();

		// set the screen to default to small
		
		
		
				
		SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		myPrefs.registerOnSharedPreferenceChangeListener(this);
		String value = myPrefs.getString("dataLocation", "0");
		loadPriceFile(Integer.parseInt(value));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		saveItem =  menu.getItem(0);
		loadItem =  menu.getItem(1);
		showHistory = menu.getItem(2);
		SharedPreferences myPrefs =PreferenceManager.getDefaultSharedPreferences(this);
		Boolean enableSavings = myPrefs.getBoolean("enableSavingOrder", false);
	
		if(enableSavings){
			//Hiding the MenuItems
		    saveItem.setVisible(true);
		    loadItem.setVisible(true);
		}else{
			//Hiding the MenuItems
		    saveItem.setVisible(false);
		    loadItem.setVisible(false);	
		}
		Boolean showHistoryBool = myPrefs.getBoolean("keepHistory", false);
		if(showHistoryBool){
			//Hiding the MenuItems
		    showHistory.setVisible(true);
		}else{
			//Hiding the MenuItems
			showHistory.setVisible(false);
		}

		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		// set the prices according to chosen size
		switch (checkedId) {
		case R.id.radSmall:
			adjustToppings(Pizza.SMALL);
			pizza.setSize(0);
			break;
		case R.id.radMedium:
			adjustToppings(Pizza.MEDIUM);
			pizza.setSize(1);
			break;
		case R.id.radLarge:
			adjustToppings(Pizza.LARGE);
			pizza.setSize(2);
			break;
		}
	}

	// method responsible for calling the PaymentActivity or clearing the screen
	public void doPayment(View view) {

		// no need to use flag, just check text of button to see what we're doing
		if (((Button)view).getText().equals("Payment")) {
			//payment
			Intent intent = new Intent(this, PaymentActivity.class);

			int size = 0;
			switch (grpSize.getCheckedRadioButtonId()) {
			case R.id.radSmall:
				size = Pizza.SMALL;
				break;
			case R.id.radMedium:
				size = Pizza.MEDIUM;
				break;
			case R.id.radLarge:
				size = Pizza.LARGE;
				break;			
			}
			
			// now that we're ready, create the pizza!
			pizza.setSize(size);
			
			addToppingsToPizza();
			
			intent.putExtra("pizza", pizza);

			startActivityForResult(intent, PAYMENT_ACTIVITY);
		} else { 

			// button text is New Order
			clearControls();
		}
	}
	
	//This was needed to get the current controls that are checked
	public void addToppingsToPizza(){
		pizza.clearToppings();
		if (chkCheese.isChecked())
			pizza.addTopping(Pizza.CHEESE);
		if (chkPeperoni.isChecked())
			pizza.addTopping(Pizza.PEPPERONI);			
		if (chkSausage.isChecked())
			pizza.addTopping(Pizza.SAUSAGE);				
		if (chkBacon.isChecked())
			pizza.addTopping(Pizza.BACON);	
		if (chkPepper.isChecked())
			pizza.addTopping(Pizza.GREEN_PEPPER);
	}

	// clear the controls for the next order
	private void clearControls() {

		RadioButton radSmall = (RadioButton) findViewById(R.id.radSmall);
		radSmall.setChecked(true); // will trigger event so adjustToppings will be called

		TextView txtResult = (TextView) findViewById(R.id.txtResult);
		txtResult.setText("");

		chkCheese.setChecked(false);
		chkPeperoni.setChecked(false);
		chkSausage.setChecked(false);
		chkBacon.setChecked(false);
		chkPepper.setChecked(false);

		btnPayment.setText("Payment");
		
		enableControls(true);
	}

	// adjust the price according to the size
	private void adjustToppings(int size) {

		// format string for checkbox text
		String format = "%s $%.2f";

		chkCheese.setText(String.format(format, Pizza.CHEESE, pizza.getToppingPrice(size, Pizza.CHEESE)));
		chkPeperoni.setText(String.format(format, Pizza.PEPPERONI, pizza.getToppingPrice(size, Pizza.PEPPERONI)));
		chkSausage.setText(String.format(format, Pizza.SAUSAGE, pizza.getToppingPrice(size, Pizza.SAUSAGE)));
		chkBacon.setText(String.format(format, Pizza.BACON, pizza.getToppingPrice(size, Pizza.BACON)));
		chkPepper.setText(String.format(format, Pizza.GREEN_PEPPER, pizza.getToppingPrice(size, Pizza.GREEN_PEPPER)));
	}
	
	private void adjustPizzaPrice(){

		rbtnSmall.setText(String.format("Small \n$"+(int)pizza.getBasePrice(Pizza.SMALL)));
		rbtnMedium.setText(String.format("Medium \n$"+(int)pizza.getBasePrice(Pizza.MEDIUM)));
		rbtnLarge.setText(String.format("Large \n$"+(int)pizza.getBasePrice(Pizza.LARGE)));

	}

	// get the return value from Payment Activity 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PAYMENT_ACTIVITY) {

			TextView txtResult = (TextView) findViewById(R.id.txtResult);

			if (resultCode == RESULT_OK) {
				// get total passed in from PaymentActivity
				double total = data.getDoubleExtra("total", 0);
				txtResult.setTextColor(Color.BLACK);
				txtResult.setText(String.format("Payment accepted.\nTotal is $%.2f inc. HST", total));

				// set button to New Order
				btnPayment.setText("New Order");
				
				enableControls(false);

			} else {
				// == RESULT_CANCELED
				// will also catch if user presses back instead of Pay Now
				txtResult.setText("Payment not accepted!");
				txtResult.setTextColor(Color.RED);
			}
		} else {
			Toast.makeText(this, "Invalid request code in onActivityResult", Toast.LENGTH_SHORT).show();
		}
	}

	// method to enable/disable controls.. Nice idea from Taylor Glazier's assignment
	private void enableControls(boolean enable) {
		
		// enable/disable the toppings
		chkCheese.setEnabled(enable);
		chkPeperoni.setEnabled(enable);
		chkSausage.setEnabled(enable);
		chkBacon.setEnabled(enable);
		chkPepper.setEnabled(enable);
		
		// enable/disable the size radio buttons
		((RadioButton)findViewById(R.id.radSmall)).setEnabled(enable);
		((RadioButton)findViewById(R.id.radMedium)).setEnabled(enable);
		((RadioButton)findViewById(R.id.radLarge)).setEnabled(enable);
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if(key.equals("numOfOrders") ){
			String value = sharedPreferences.getString(key,"");
			Toast.makeText(this, "KEY: " +key +" VALUE: " + value, Toast.LENGTH_LONG).show();	
		}		
		else if(key.equals("dataLocation")){

			String value = sharedPreferences.getString(key,"0");
			//Log.e(TAG, "value is: "+Integer.parseInt(value));
			loadPriceFile(Integer.parseInt(value));
			
		//	Toast.makeText(this, "KEY: " +key +" VALUE: " + value, Toast.LENGTH_LONG).show();	
		}
		else{
			Boolean answer = sharedPreferences.getBoolean(key, false);
			if(key.equals("enableSavingOrder")){
				//If the boolean in enableSavingOrder is true then we set the Menu Items to true 
				if(answer){
					saveItem.setVisible(true);
					loadItem.setVisible(true);
				}
				//Else we keep we don't show them
				else{
					saveItem.setVisible(false);
					loadItem.setVisible(false);
				}
				Toast.makeText(this, "Other, KEY: " +key +" VALUE: " + answer, Toast.LENGTH_LONG).show();
			}
			else if(key.equals("keepHistory")){
				
				if(answer){
					//Hiding the MenuItems
				    showHistory.setVisible(true);
				}else{
					//Hiding the MenuItems
					showHistory.setVisible(false);
				}
				Toast.makeText(this, "Other, KEY: " +key +" VALUE: " + answer, Toast.LENGTH_LONG).show();	
			}
			else if(key.equals("keepHistoryUnique")){
				
				if(answer){
					//On
				   
				}else{
					//Off
				}
				Toast.makeText(this, "Other, KEY: " +key +" VALUE: " + answer, Toast.LENGTH_LONG).show();	
			}
			
		}
		
	}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			Intent intent = new Intent(this, UserPrefActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
//	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//If the action_save is called then it enters
		if (item.getItemId() == R.id.action_save) {
			//Save to Share preferences
			SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
			Editor editor = myPrefs.edit();
			editor.putInt("size", pizza.getSize());
			addToppingsToPizza();
			editor.putString("toppings", pizza.getToppingsList());
			editor.apply();
		}
		//else if the action_load it calls then it enters
		else if (item.getItemId() == R.id.action_load) {
			//Retrieve them out
			SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
			//Clear the controls and prepare for the loaded order
			clearControls();
			RadioButton grpSmall = (RadioButton) findViewById(R.id.radSmall);
			RadioButton grpMedium = (RadioButton) findViewById(R.id.radMedium);
			RadioButton grpLarge = (RadioButton) findViewById(R.id.radLarge);
			//Set the grp to the proper size of pizza
			if(myPrefs.getInt("size", 10)==0){
				grpSmall.setChecked(true);
				pizza.setSize(Pizza.SMALL);
			}
			else if(myPrefs.getInt("size", 10)==1){
				grpMedium.setChecked(true);
				pizza.setSize(Pizza.MEDIUM);
			}
			else if(myPrefs.getInt("size", 10)==2){
				grpLarge.setChecked(true);
				pizza.setSize(Pizza.LARGE);
			}
			//Set the proper checked boxes so they are correct from the loading of shared preferences
			String ourToppings = myPrefs.getString("toppings", "empty");
			String[] separated = ourToppings.split("\n");
			for (String string : separated) {
				
				if(string.equals(Pizza.CHEESE)){
					chkCheese.setChecked(true);
				}
				else if(string.equals(Pizza.PEPPERONI)){
					chkPeperoni.setChecked(true);
				}
				else if(string.equals(Pizza.SAUSAGE)){
					chkSausage.setChecked(true);
				}
				else if(string.equals(Pizza.BACON)){
					chkBacon.setChecked(true);
				}
				else if(string.equals(Pizza.GREEN_PEPPER)){
					chkPepper.setChecked(true);
				}
			}
		}
		//else if the action_history it calls then it enters
		else if (item.getItemId() == R.id.action_showHistory) {
			// Start the ListActivity that will show all records
			Intent intent = new Intent(this, PizzaListActivity.class);	
			startActivity(intent);
		}
		return super.onMenuItemSelected(featureId, item);
	}
	//Check to see if the Network is connected
	private boolean isNetworkConnected() {
		// get Connectivity Manager to get network status
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true; //we have a connection
		} else {
			return false; // no connection!
		}
	}
	
	public void loadPriceFile(int priceFileNum) {

		if (priceFileNum!=0){
			if(isNetworkConnected()){
				priceFile.setText("Price File: server-"+priceFileNum);

				// do network call off main thread
				NetworkHelperTask myHelper = new NetworkHelperTask();
				myHelper.execute(priceFileNum);
			}else{
				Toast.makeText(this, "Network Not Avaialble", Toast.LENGTH_LONG).show();
			}
			
		} else if(priceFileNum==0){
			priceFile.setText("Price File: 'local'");
			RawFileHelperTask myHelper = new RawFileHelperTask();
			// do file io off main thread
			myHelper.execute();
			
		}
	}

	private class NetworkHelperTask extends AsyncTask<Integer, Void, String> {

		URL url;
		InputStream inputStream = null; 
		@Override
		protected String doInBackground(Integer... params) {

			switch (params[0]) {
			case 1:
				try {
					url = new URL("http://mobile.sheridanc.on.ca/~bonenfan/PROG38448/pizza_prices_1.json");
				} catch (MalformedURLException e) {
					Log.e(TAG + "The Exception is:", e.toString());
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					url = new URL("http://mobile.sheridanc.on.ca/~bonenfan/PROG38448/pizza_prices_2.json");
				} catch (MalformedURLException e) {
					Log.e(TAG + "The Exception is:", e.toString());
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					url = new URL("http://mobile.sheridanc.on.ca/~bonenfan/PROG38448/pizza_prices_3.json");
				}catch (MalformedURLException e) {
					Log.e(TAG + "The Exception is:", e.toString());
					e.printStackTrace();
				}
				break;
			default:
				Toast.makeText(getApplicationContext(), "This is unknown Behaviour",Toast.LENGTH_LONG).show();
				break;
			}
			try {
				URLConnection connection = url.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				
				int responseCode = httpConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
		
					inputStream = httpConnection.getInputStream();

					byte[] buffer = new byte[inputStream.available()];
					inputStream.read(buffer);

					return parsePizzaPriceFileOnline(new String(buffer));
				} else {
					return "Cannot retrieve info: " + responseCode;
				}
			} catch (Exception e) {
				Log.e(TAG + ": in doInBackground", e.toString());
				return "Cannot retrieve info";
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG + ": in doInBackground: finally", e.toString());
				}
			}
		}	

		private String parsePizzaPriceFileOnline(String jsonData) {

			try {
				pizza.LoadFromFile(jsonData);
			} catch (Exception e) {
				Log.e(TAG + ": in parsePizzaFile", e.toString());
			}
			return jsonData; 
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			// now back on UI thread; 
			// write file
			adjustPizzaPrice();
			adjustToppings(Pizza.SMALL);
			//Log.e(TAG + "The result is: ", result);
		}

	}
	
	private class RawFileHelperTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			Resources res = getResources();
		
				// create InputStream to read in the file
				InputStream inputStream = res.openRawResource(R.raw.pizza_prices);
				try {
					// create byte array to store contents of stream
					byte[] buffer = new byte[inputStream.available()];
					inputStream.read(buffer);
					
					// Return result as a String 
					return parsePizzaLocalFile(new String(buffer));
					
				} catch (IOException e) {
					Log.e(TAG + ": in doInBackground", e.toString());
					return "Cannot retrieve info";
				} finally {
					try {
						inputStream.close();
					} catch (IOException e) {
						Log.e(TAG + ": in doInBackground: finally", e.toString());
					}
				}
	
		}	
		private String parsePizzaLocalFile(String jsonData) {
						
			try {
				pizza.LoadFromFile(jsonData);

			} catch (Exception e) {
				Log.e(TAG + ": in parsePizzaInfo", e.toString());
			}
			return jsonData;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			// now back on UI thread; 
			adjustPizzaPrice();
			adjustToppings(Pizza.SMALL);

			//Log.e(TAG + ": this is the result: ", result);
		}
		
	}
	
}
