package com.example.spriet_muthukrishnan;

import com.example.darren_spriet_final_project.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UserPrefActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_pref_fragment);
	}
	
}