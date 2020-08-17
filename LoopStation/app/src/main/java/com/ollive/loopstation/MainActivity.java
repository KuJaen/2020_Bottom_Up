package com.ollive.loopstation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.ollive.loopstation.util.RecyclerDecoration;
import com.ollive.loopstation.waveform.BoardManager;
import com.ollive.loopstation.waveform.TrackRecyclerAdapter;
import com.ollive.loopstation.waveform.WaveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private RecyclerView track_view = null;
    private TrackRecyclerAdapter trackRecyclerAdapter = null;
    private ArrayList<WaveData> trackList = null;

    private Button menu_btn;
    private Button play_btn;
    private SeekBar bpm;
    private TextView bpm_status;

    private String absoluteExternalPath = null;
    private boolean RECORDING_STATUS;

    private String PATH_TO_INST;
    private TextInputEditText inst_path;
    private MediaPlayer instPlayer;

    private LinkedList<BoardManager> record_list;

    private Button metronome_btn;
    private boolean metronome_status;
    private Handler metronome_handler;
    private int metronome_period = 1000;
    private SoundPool metronome_pool;
    private int metronome_sound;

     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         switch (requestCode){
             case REQUEST_RECORD_AUDIO_PERMISSION:
                 permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                 break;
         }
         if(!permissionToRecordAccepted) finish();
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        ///////////////////// Loop Station /////////////////////
        init();
        setTrackRecyclerView();
        setMenuBtn();
        setBpm();
        setPlay();
        setMetronome_btn();
    }

    private void init(){
        track_view = (RecyclerView) findViewById(R.id.track_list);
        trackList = new ArrayList<WaveData>();

        menu_btn = (Button) findViewById(R.id.menu_btn);
        bpm = (SeekBar) findViewById(R.id.bpm_seekbar);
        bpm_status = (TextView) findViewById(R.id.btm_status);
        play_btn = (Button) findViewById(R.id.record_btn);
        absoluteExternalPath = getExternalCacheDir().getAbsolutePath();
        RECORDING_STATUS = false;
        inst_path = (TextInputEditText) findViewById(R.id.inst_url);
        record_list = new LinkedList<>();
        metronome_btn = (Button) findViewById(R.id.metronome);
        metronome_status = false;

        metronome_handler = new Handler();
        metronome_pool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        metronome_sound = metronome_pool.load(this, R.raw.metronome, 1);
    }

    private void setTrackRecyclerView(){
         trackRecyclerAdapter = new TrackRecyclerAdapter(trackList);
         LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
         track_view.setLayoutManager(linearLayoutManager);
         track_view.setAdapter(trackRecyclerAdapter);

        RecyclerDecoration recyclerDecoration = new RecyclerDecoration(0);
        track_view.addItemDecoration(recyclerDecoration);

        trackRecyclerAdapter.setOnTrackBtnRClickListenrer(new TrackRecyclerAdapter.OnTrackBtnRClickListener() {
            @Override
            public void onTrackBtnRClick(View v, int pos, WaveData data, BoardManager bdManager) {
                if(data.getStatus() == 0) {
                    v.setBackground(getApplication().getDrawable(R.drawable.recordstarte));
                    data.setStatus(1);
                    record_list.add(bdManager);

                    if(RECORDING_STATUS){
                        data.startRecording();
                        bdManager.start();
                    }
                }
                else if(data.getStatus() == 1) {
                    v.setBackground(getApplication().getDrawable(R.drawable.recordstart));
                    data.setStatus(0);

                    int index = 0;
                    for(BoardManager bm : record_list){
                        if(bm.equals(bdManager)){
                            record_list.remove(index);
                            index = 0;
                            break;
                        }
                        index++;
                    }

                    if(RECORDING_STATUS){
                        data.stopRecording();
                        bdManager.Pause();

                        if(data.getStatus() != 2){
                            data.startPlaying();
                        }
                    }
                }
                else {
                    v.setBackground(getApplication().getDrawable(R.drawable.recordstarte));
                    data.setStatus(1);
                    record_list.add(bdManager);

                    if(RECORDING_STATUS){
                        data.startRecording();
                        bdManager.start();
                    }
                }
            }
        });

        trackRecyclerAdapter.setOnTrackBtnSClickListenrer(new TrackRecyclerAdapter.OnTrackBtnSClickListener() {
            @Override
            public void onTrackBtnSClick(View v, int pos, WaveData data) {
                if(data.getStatus() == 0) {
                    v.setBackground(getApplication().getDrawable(R.drawable.recordstope));
                    data.setStatus(2);

                    if(RECORDING_STATUS){
                        data.stopPlaying();
                    }
                }
                else if(data.getStatus() == 2) {
                    v.setBackground(getApplication().getDrawable(R.drawable.recordstop));
                    data.setStatus(0);

                    if(RECORDING_STATUS){
                        data.startPlaying();
                    }
                }
                else {
                    v.setBackground(getApplication().getDrawable(R.drawable.recordstope));
                    data.setStatus(2);

                    if(RECORDING_STATUS){
                        data.stopPlaying();
                    }
                }
            }
        });

        trackRecyclerAdapter.setOnTrackBtnLClickListenrer(new TrackRecyclerAdapter.OnTrackBtnLClickListener() {
            @Override
            public void onTrackBtnLClick(View v, int pos, WaveData data) {
                data.setLooping();
                if(data.isLooping())
                    v.setBackground(getApplication().getDrawable(R.drawable.recordloope));
                else
                    v.setBackground(getApplication().getDrawable(R.drawable.recordloop));
            }
        });

    }

    private void setMenuBtn(){
         menu_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 PopupMenu pm = new PopupMenu(getApplicationContext(), v);
                 pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                         if(item.getItemId() == 1) {
                            menuPopup();
                         }
                         else{
                             Toast.makeText(getApplicationContext(), "Popup Select Error", Toast.LENGTH_SHORT).show();
                         }
                         return false;
                     }
                 });
                 Menu menu = pm.getMenu();
                 menu.add(0, 1, 0, "Add Track");
                 pm.show();
             }
         });
    }

    private void setBpm(){
         bpm.setMax(200);
         bpm.setProgress(60);

         bpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm_status.setText("BPM : " + String.valueOf(progress));
                metronome_period = (int)(1000.0 * (60.0/(double)progress));
             }
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) { }
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) { }
         });
    }

    private void setPlay(){
         play_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(RECORDING_STATUS){
                     play_btn.setBackground(getResources().getDrawable(R.drawable.record));

                     stopInst();
                     for(WaveData track : trackList){
                         if(track.getStatus() == 0){
                             track.stopPlaying();
                         }
                         else if(track.getStatus() == 1){
                             track.stopRecording();
                         }
                         else if(track.getStatus() == 2){ }
                         else{
                             Toast.makeText(getApplicationContext(), "Track Status Error. Please Restart.", Toast.LENGTH_LONG);
                         }
                     }

                     for(BoardManager bm : record_list)
                         bm.Pause();

                     RECORDING_STATUS = false;
                 }
                 else{
                     play_btn.setBackground(getResources().getDrawable(R.drawable.stop));

                     setInst(inst_path.getText().toString());
                     for(WaveData track : trackList){
                         if(track.getStatus() == 0){
                             track.startPlaying();
                         }
                         else if(track.getStatus() == 1){
                             track.startRecording();
                         }
                         else if(track.getStatus() == 2){ }
                         else{
                             Toast.makeText(getApplicationContext(), "Track Status Error. Please Restart.", Toast.LENGTH_LONG);
                         }
                     }

                     for(BoardManager bm : record_list)
                         bm.start();

                     RECORDING_STATUS = true;
                 }
             }
         });
    }

    private void setInst(String path){
        instPlayer = new MediaPlayer();
         if(path == null)
             return;
         if(path.equals(""))
            return;

         PATH_TO_INST = Environment.getExternalStorageDirectory() + "/" + path;
         Log.d("PATH", PATH_TO_INST);
         try{
             instPlayer.setDataSource(PATH_TO_INST);
             Log.d("Check", "Checking1");
             instPlayer.prepareAsync();
             Log.d("Check", "Checking2");
             instPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                 @Override
                 public void onPrepared(MediaPlayer mp) {
                     Log.d("Check", "Checking3");
                     mp.start();
                     Log.d("Check", "Checking4");
                 }
             });
         }catch(IOException e){
             Toast.makeText(getApplicationContext(), "Useable Inst Path", Toast.LENGTH_LONG).show();
         }
    }
    private void stopInst(){
         instPlayer.release();
         instPlayer = null;
    }

    private void menuPopup(){
        final EditText editText = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Track");
        builder.setMessage("Please write the name of the new track");
        builder.setView(editText);
        builder.setPositiveButton("추가",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WaveData data = new WaveData(editText.getText().toString(), absoluteExternalPath);
                        trackList.add(data);
                        trackRecyclerAdapter.notifyItemInserted(trackList.size() - 1);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    ///////////// Metronome /////////////
    private void setMetronome_btn(){
         metronome_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(metronome_status) {
                     metronome_btn.setBackground(getResources().getDrawable(R.drawable.metronome));
                     metronome_status = false;
                     start_metronome(metronome_status);
                 }
                 else {
                     metronome_btn.setBackground(getResources().getDrawable(R.drawable.metronome2));
                     metronome_status = true;
                     start_metronome(metronome_status);
                 }
             }
         });
    }

    private void start_metronome(boolean status){
        Log.d("Metronome", Integer.toString(metronome_period));
        metronome_pool.play(metronome_sound, 1f,1f,0,0,1f);
        if(status){
            metronome_handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    start_metronome(metronome_status);
                }
            }, metronome_period);
        }
        else{
            return;
        }
    }
}
