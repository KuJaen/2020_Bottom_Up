package com.ollive.loopstation.waveform;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ShortBuffer;

public class BoardManager extends SurfaceView implements SurfaceHolder.Callback {
    // 레이아웃 화면의 크기를 받아와 저장하고 주파수를 그릴 Canvas 보드에 대한 상태를 저장한다.
    int ScreenWidth, ScreenHeight, BoardWidth, BoardHeight;
    int BoardStartX,BoardStartY,BoardMiddleWidth,
            BoardMiddleHeight,BoardEndX,BoardEndY;

    Canvas canvas; // 주파수가 그려질 Canvas객체이다.

    double RatioX,RatioY; // Canvas에 그려질 주파수 데이터이다.
    int TimeDiv,MaxHeight,SamplingRate,VRange; // Canvas에 그려질 주파수에 대한 기본 정보이다.

    // 실시간으로 녹음기능을 통해 받아올 데이터의 길이와 버퍼를 저장할 필드이다.
    int DataLength;
    ShortBuffer Buffer,tempBuffer,ReadBuffer;
    SurfaceHolder mHolder;

    // RecordManager객체는 이곳에서 형성된다.
    // RecordManager를 통해 녹음기능을 통해 받아오는 데이터를 실시간으로 갱신할 것이다.
    RecordManager recordManager;
    boolean isData; // Flag

    // 객체 생성자
    public BoardManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BoardManager(Context context) {
        super(context);
        init();
    }

    // SurfaceView를 상속받아 Override된 메서드들이다.
    // BoardManager가 생성 될때 SurfaceView를 형성하고 설정한다.
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder=holder;
        getScreenInfo();
        canvas=holder.lockCanvas();
        drawBoard();
        holder.unlockCanvasAndPost(canvas);

        recordManager = new RecordManager(this);
        recordManager.setBoardManager(this);
        recordManager.start();
    }

    // SurfaceView의 설정값이 변할 때 실행된다. 해당 프로젝트에서는 사용하지 않는다.
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    // SurfaceView가 사라질 때 실행된다. 해당 프로젝트에서는 사용하지 않는다.
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }

    //BoardManager의 초기설정 부분이다.
    void init(){
        SurfaceHolder mHolder=getHolder(); // SurfaceHolder객체를 받아온다.
                                    // SurfaceHolder는 말그대로 SurfaceView의 데이터를 담고있는 객체이다.
        mHolder.addCallback(this); // 콜백 Context설정

        SamplingRate=44100; // SamplingRate를 44100으로 설정한다.
        // SamplingRate들을 Buffer에 적용시킨다.
        Buffer=ShortBuffer.allocate(SamplingRate);
        tempBuffer=ShortBuffer.allocate(SamplingRate);

        //화면에 표시할 시간
        TimeDiv=1000;
        //표시할 최대값
        MaxHeight=32767;
    }

    // Track에 데이터를 실시간으로 받아온다.
    public void start(){
        recordManager.onStart();
    }
    // Track에 데이터를 받아오는 것을 멈춘다.
    public void Pause(){
        recordManager.onPause();
    }
    // Track에 데이터를 받아오는 것을 끝낸다.
    public void Stop(){
        recordManager.onStop();
    }

    // SurfaceView Screen에 대한 정보를 설정한다.
    void getScreenInfo(){
        //화면의 크기를 얻어옴
        ScreenWidth=getWidth();
        ScreenHeight=getHeight();

        //박스의 크기를 화면의 90%크기로 설정
        BoardWidth=(int)(ScreenWidth*0.9);
        BoardHeight=(int)(ScreenHeight*0.9);

        //박스의 그릴 지점 설정
        BoardStartX=(ScreenWidth-BoardWidth)/2;
        BoardStartY=(ScreenHeight-BoardHeight)/2;
        BoardEndX=BoardStartX+BoardWidth;
        BoardEndY=BoardStartY+BoardHeight+2;

        //박스의 중앙선
        BoardMiddleHeight=BoardHeight/2;
        BoardMiddleWidth=BoardWidth/2;

        //표시 배율

        RatioY=(BoardHeight-2)/(MaxHeight*2.0f);
        RatioX=(BoardWidth-2)*1000/(double)(TimeDiv);

        isData = false;
    }

    // SurfaceView를 통해 실질적으로 트랙을 그리는 메서드이다.
    void drawBoard(){
        Paint paint=new Paint();

        //배경화면 힌색으로
        paint.setColor(Color.BLACK);
        canvas.drawRect(1,1,ScreenWidth,ScreenHeight,paint);

        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        //보드 외곽선
        canvas.drawRect(BoardStartX-1,BoardStartY,BoardEndX+2,BoardEndY,paint);

        //보드 십자선
        paint.setStrokeWidth(1);
        canvas.drawLine(BoardStartX+BoardMiddleWidth,BoardStartY,
                BoardStartX+BoardMiddleWidth,BoardEndY,paint);
        canvas.drawLine(BoardStartX,BoardStartY+BoardMiddleHeight
                ,BoardEndX,BoardStartY+BoardMiddleHeight,paint);

    }

    void setData(){
        DataLength=3000;
        //AudioRecord로 부터 넘겨 받은 버퍼
        ReadBuffer= ShortBuffer.allocate(3000);
        //임의의 데이터로 버퍼를 채움
        int i;
        for(i=0;i<=DataLength-1;i++){
            ReadBuffer.put((short)i);
        }
        canvas=mHolder.lockCanvas();
        drawBoard();

        for (i=0;i<=5;i++){
            drawData();
        }

        mHolder.unlockCanvasAndPost(canvas);
    }

    // Track에 그려질 주파수 데이터를 받아오는 메서드이다.
    void setData(ShortBuffer readBuffer, int dataLength){
        this.ReadBuffer = readBuffer;
        this.DataLength = dataLength;

        canvas = mHolder.lockCanvas();
        drawBoard();
        drawData();
        mHolder.unlockCanvasAndPost(canvas);
        isData=true;
    }

    // Track에 녹음기능을 통해 받아온 데이터를 주파수로 그리는 메서드이다.
    void drawData(){
        double data, Stime, Ttime;
        int x, y, i;

        if(isData){
            Paint paint=new Paint();
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(5);

            tempBuffer.position(0);
            tempBuffer.put(Buffer.array(),0,SamplingRate);//버퍼 복사
            Buffer.position(0);
            //앞부분을 readBuffer의 내용으로 채움
            Buffer.put(ReadBuffer.array(),0,DataLength-1);
            //readBuffer의 내용 뒷부분을 원래의 값으로 채움-> 쉬프트
            Buffer.put(tempBuffer.array(),0,SamplingRate-DataLength-1);

            //샘플링 한주기의 시간을 구함
            Stime=1.0f/SamplingRate;
            //그려야할 전체 시간(1초) - TimeDiv/1000
            Ttime=TimeDiv/(Stime*1000);

            //데이터를 읽어와서 화면에 출력
            Buffer.position(0);
            for (i=0;i<(int)Ttime;i++) {
                //그려질 x 좌표 구하기
                x = (int) ((i + 1) * Stime * RatioX) + BoardStartX + 1;

                //y값 구하고 역상만들기 및 아래로 내리기
                data = -RatioY * Buffer.get(i);
                y = BoardMiddleHeight + (int) data + BoardStartY;

                //그리기
                canvas.drawPoint(x, y, paint);
            }
        }
        isData = false;
    }
}
