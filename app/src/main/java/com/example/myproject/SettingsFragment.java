package com.example.myproject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements View.OnClickListener{
    private Button b;
    SharedPreferences sp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View fragmentView= inflater.inflate(R.layout.fragment_settings,
                container, false);
       b=fragmentView.findViewById(R.id.button4);
       b.setOnClickListener(this);
       return fragmentView;
    }
    public void onClick(View v){
        sp=this.getActivity().getSharedPreferences("user",0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isChecked", false);
        editor.putBoolean("isChecked2", false);
        editor.commit();
        Intent go=new Intent(getActivity(), Login.class);
        startActivity(go);
    }

}
