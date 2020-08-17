package com.ollive.loopstation.waveform;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ShortBuffer;

class RecordManager extends Thread {
    boolean RunFlag, EndFlag;
    ShortBuffer Buffer, SendBuffer;
    int BufferSize, ReadSize;

    AudioRecord AR;
    BoardManager bdManager;

    public RecordManager(BoardManager bdManager) {
        //AudioRecord 초기화
        int SamplingRate = 44100;

        BufferSize = AudioRecord.getMinBufferSize(SamplingRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        Buffer = ShortBuffer.allocate(BufferSize);
        SendBuffer = ShortBuffer.allocate(BufferSize);

        AR = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SamplingRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, BufferSize);
        AR.startRecording();

        EndFlag = true;

        this.bdManager = bdManager;
    }

    void onStart() {
        RunFlag = true;
    }

    void onPause() {
        RunFlag = false;
    }

    void onStop() {
        RunFlag = false;
        EndFlag = false;

        try {
            join();
        } catch (Exception e) {
            Log.d("Test", e.toString());
        }

        AR.stop();
        AR.release();
    }

    void setBoardManager(BoardManager bdManager) {
        this.bdManager = bdManager;
    }

    @Override
    public void run() {
        super.run();
        while (EndFlag) {
            while (RunFlag) {
                ReadSize = AR.read(Buffer.array(), 0, BufferSize);
                //녹음이 끝난 버퍼를 그대로 전송하면 그래프가 그려지는 순간
                //다시 녹음 중인 데이터로 변경됨을 방지하기 복사해서 전송함
                SendBuffer.position(0);
                SendBuffer.put(Buffer.array(), 0, BufferSize);
                bdManager.setData(SendBuffer, ReadSize);
            }
        }
    }
}
