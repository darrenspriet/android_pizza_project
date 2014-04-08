package com.example.spriet_muthukrishnan;

import java.util.Random;

import com.example.darren_spriet_final_project.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PaymentActivity extends Activity {

	// have a data field since we are returning total back to MainActivity
	// so need it in another method as well
	double total;
	Button btnSaveHistory;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);
		btnSaveHistory = (Button) findViewById(R.id.btnSaveOrderHistory);
		SharedPreferences myPrefs =PreferenceManager.getDefaultSharedPreferences(this);
		boolean keepHistory = myPrefs.getBoolean("keepHistory",false);
		if(keepHistory){
			btnSaveHistory.setEnabled(true);
		}
		else{
			btnSaveHistory.setEnabled(false);
		}


		// get the intent that launched the Activity
		Intent intent = getIntent();

		// "inflate" the pizza back into an instance
		// can do because we implemented Parcelable
		Pizza pizza = (Pizza) intent.getParcelableExtra("pizza");

		// get the base price
		double price = pizza.getPizzaPrice();

		// get the string representation of the toppings
		String toppingList = pizza.getToppingsList();
		
		// set the size textView
		TextView txtSize = (TextView) findViewById(R.id.txtSize);
		txtSize.setText(Pizza.getSizeString(pizza.getSize()) + " pizza with..."); 
		
		// set the toppings text view
		TextView txtToppings = (TextView) findViewById(R.id.txtToppings);
		txtToppings.setText(toppingList);

		double HST = price * 0.13;
		total = price + HST;

		// set the total price field
		TextView txtPrice = (TextView) findViewById(R.id.txtPrice);
		txtPrice.setText(String.format("Total price with HST: $%.2f", total));
	}

	// payment method which simulates 50% failure for credit card txns
	public void payNow (View view) {

		RadioGroup group = (RadioGroup) findViewById(R.id.grpPayment);

		if (group.getCheckedRadioButtonId() == R.id.radCash) {
			sendSuccess();

		} else {
			Random random = new Random();
			boolean success = random.nextBoolean();
			if (success) {
				sendSuccess();
			} else {
				sendFailure();
			}
		}
	}

	// send failure back to MainActivity
	private void sendFailure() {
		setResult(RESULT_CANCELED);
		finish();		
	}

	// send success along with total back
	private void sendSuccess() {
		Intent intent = new Intent();
		intent.putExtra("total", total);
		setResult(RESULT_OK, intent);
		finish();
	}
}
