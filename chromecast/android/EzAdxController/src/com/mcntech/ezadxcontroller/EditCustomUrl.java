package com.mcntech.ezadxcontroller;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


public class EditCustomUrl extends DialogFragment implements
OnEditorActionListener 
{
    public interface EditCustomUrlDialogListener {
        void onFinishEditDialog(String inputText, Boolean isLive);
    }
	
	private EditText mEditText;
	private CheckBox mCheckIsLive;
	
	public EditCustomUrl() 
	{        
		// Empty constructor required for DialogFragment    
	}    
	
	@Override    
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{       
		Bundle bundle = this.getArguments();
		View view = inflater.inflate(R.layout.edit_custom_url, container);        
		mEditText = (EditText) view.findViewById(R.id.txt_custom_url);
		mCheckIsLive = (CheckBox)view.findViewById(R.id.checkbox_live);
		getDialog().setTitle("Main Channel Custom URL");
		mEditText.setText(bundle.getString("url"));
		mCheckIsLive.setChecked(bundle.getBoolean("isLive"));
        mEditText.requestFocus();
        mEditText.setOnEditorActionListener(this);
		
		return view;    
	}
	
	static EditCustomUrl newInstance(int num, String Url, Boolean isLive) {
		EditCustomUrl f = new EditCustomUrl();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        args.putString("url", Url);
        args.putBoolean("isLive", isLive);
        f.setArguments(args);
        return f;
	}
   @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
        	EditCustomUrlDialogListener activity = (EditCustomUrlDialogListener) getActivity();
            activity.onFinishEditDialog(mEditText.getText().toString(), mCheckIsLive.isChecked());
            this.dismiss();
            return true;
        }
        return false;
    }
   
   public void onLiveCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    switch(view.getId()) {
	        case R.id.checkbox_live:
	        	// Do nothing for now
	            break;
	    }
	}   
}
