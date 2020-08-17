package com.ollive.loopstation.waveform;

import androidx.lifecycle.ViewModel;

import com.ollive.loopstation.util.TSLiveData;

public class TrackViewModel extends ViewModel {
    public TSLiveData<String> trackName = new TSLiveData<>();

    public void setTrackName(String trackName) {
        this.trackName.setValue(trackName);
    }
}
