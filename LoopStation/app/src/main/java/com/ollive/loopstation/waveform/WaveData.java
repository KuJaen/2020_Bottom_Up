package com.ollive.loopstation.waveform;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class WaveData {
    private String trackName;
    private int status; // 0: idle  1: record  2: pause
    private String fileName = null;
    private boolean isLooping;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    public WaveData(String trackName, String path){
        this.trackName = trackName;
        this.fileName = path + "/" + trackName + ".mp4";
        status = 0;
        isLooping = false;
    }

    public void startPlaying() {
        player = new MediaPlayer();
        try{
            player.setDataSource(fileName);
            player.prepare();
            player.setLooping(isLooping);
            player.start();
        }catch (IOException e){
            Log.e(trackName+"-Error", "prepare() failed");
        }
    }

    public void stopPlaying(){
        player.release();
        player = null;
    }

    public void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        try{
            recorder.prepare();
        } catch (IOException e){
            Log.e(trackName+"-Error", "prepare() failed");
        }

        recorder.start();
    }

    public void stopRecording(){
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    ////////////////////// Get-Set //////////////////////

    public void setStatus(int status){
        this.status = status;
    }

    public String getTrackName(){
        return this.trackName;
    }
    public int getStatus(){
        return this.status;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(){
        if(isLooping){
            isLooping = false;
        }
        else {
            isLooping = true;
        }
    }
}
