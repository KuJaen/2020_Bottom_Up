package com.ollive.loopstation.bindingAdapter;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

public class TextBindingAdapter {
    @BindingAdapter({"setTrackText"})
    public static void setTrackText(TextView view, String data){
        try{
            if(data != null)
                view.setText(data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
