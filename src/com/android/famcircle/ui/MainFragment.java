package com.android.famcircle.ui;

import com.android.famcircle.R;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


public class MainFragment extends Fragment{

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View root = inflater.inflate(R.layout.fragment_main, container, false);
		
		TextView text = (TextView)root.findViewById(R.id.gotoshare);
		Log.i("view ", ""+(text==null));
		text.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),ShareActivity.class);
				startActivity(intent);
			}
		});
		
		return root;  
	}

}
