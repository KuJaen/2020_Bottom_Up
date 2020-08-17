package com.ollive.loopstation.waveform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ollive.loopstation.R;
import com.ollive.loopstation.databinding.TrackBinding;

import java.util.ArrayList;

public class TrackRecyclerAdapter extends RecyclerView.Adapter<TrackViewHolder> {
    private ArrayList<WaveData> dataList;


    public TrackRecyclerAdapter(ArrayList<WaveData> data){
        this.dataList = data;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrackBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_track, parent, false);

        return new TrackViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, final int position) {
        final WaveData data = dataList.get(position);
        TrackViewModel model = new TrackViewModel();
        model.setTrackName(data.getTrackName());
        holder.setViewModel(model);

        final TrackViewHolder h = holder;
        holder.getBtnR().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackBtnRClickListener.onTrackBtnRClick(v, position, data, h.getBoardManager());
            }
        });
        holder.getBtnS().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackBtnSClickListener.onTrackBtnSClick(v, position, data);
            }
        });
        holder.getBtnL().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackBtnLClickListener.onTrackBtnLClick(v, position, data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public interface OnTrackBtnRClickListener{
        void onTrackBtnRClick(View v, int pos, WaveData data, final BoardManager bdManager);
    }
    public static OnTrackBtnRClickListener trackBtnRClickListener = null;
    public void setOnTrackBtnRClickListenrer(OnTrackBtnRClickListener listener){
        this.trackBtnRClickListener = listener;
    }

    public interface OnTrackBtnSClickListener{
        void onTrackBtnSClick(View v, int pos, WaveData data);
    }
    public static OnTrackBtnSClickListener trackBtnSClickListener = null;
    public void setOnTrackBtnSClickListenrer(OnTrackBtnSClickListener listener){
        this.trackBtnSClickListener = listener;
    }

    public interface OnTrackBtnLClickListener{
        void onTrackBtnLClick(View v, int pos, WaveData data);
    }
    public static OnTrackBtnLClickListener trackBtnLClickListener = null;
    public void setOnTrackBtnLClickListenrer(OnTrackBtnLClickListener listener){
        this.trackBtnLClickListener = listener;
    }


}
