package xiaodou.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

import xiaodou.music.R;
import xiaodou.music.activity.MainActivity;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener{

    //MediaPlayer对象
    private MediaPlayer mMediaPlayer = null;

    //用来记录MediaPlayer对象是否需要运行prepareAsync()
    private Boolean mbIsInitial = true;
//    private Boolean mbAudioFileFound = false;

    public static final String ACTION_PLAY = "xiaodou.music.action.PLAY";
    public static final String ACTION_PAUSE = "xiaodou.music.action.PAUSE";
    public static final String ACTION_STOP = "xiaodou.music.action.STOP";
    public static final String ACTION_SET_REPEAT = "xiaodou.music.action.SET_REPEAT";
    public static final String ACTION_CANCEL_REPEAT = "xiaodou.music.action.CANCEL_REPEAT";
    public static final String ACTION_GOTO = "xiaodou.music.action.GOTO";

//    public static final String
//            ACTION_PLAY = "tw.android.mediaplayer.action.PLAY",
//            ACTION_PAUSE = "tw.android.mediaplayer.action.PAUSE",
//            ACTION_SET_REPEAT = "tw.android.mediaplayer.action.SET_REPEAT",
//            ACTION_CANCEL_REPEAT = "tw.android.mediaplayer.action.CANCEL_REPEAT",
//            ACTION_GOTO = "tw.android.mediaplayer.action.GOTO";


    public MediaPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.yanyuan);

        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(this, uri);
        } catch (IOException e) {
            //e.printStackTrace();
            Toast.makeText(MediaPlayerService.this, "音乐播放错误！", Toast.LENGTH_SHORT).show();
        }

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (mbAudioFileFound){
            mMediaPlayer.release();
            mMediaPlayer = null;
//        }

        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (!mbAudioFileFound){
//            stopSelf();
//            return super.onStartCommand(intent, flags, startId);
//        }

        if (intent.getAction().equals(ACTION_PLAY)){
            if(mbIsInitial){
                mMediaPlayer.prepareAsync();
                mbIsInitial = false;
            }else{
                mMediaPlayer.start();
            }
        }else if (intent.getAction().equals(ACTION_PAUSE)){
            mMediaPlayer.pause();
        }else if (intent.getAction().equals(ACTION_SET_REPEAT)){
            mMediaPlayer.setLooping(true);
        }else if (intent.getAction().equals(ACTION_STOP)){
            mMediaPlayer.stop();
            mbIsInitial = true;//停止播放后必须再运行prepare()或prepareAsync()才能重新播放
        }else if (intent.getAction().equals(ACTION_CANCEL_REPEAT)){
            mMediaPlayer.setLooping(false);
        }else if (intent.getAction().equals(ACTION_GOTO)){
            int seconds = intent.getIntExtra("GOTO_POSITION_SECONDS", 0);
            mMediaPlayer.seekTo(seconds * 1000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //是否取得audio focus
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int r = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (r != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mp.setVolume(0.1f, 0.1f);//降低音量
        }
        mp.start();

        Intent it = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, it,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification noti = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker("播放背景音乐")
                .setContentTitle("Music")
                .setContentText("正在播放背景音乐")
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, noti);

        Toast.makeText(MediaPlayerService.this, "开始播放", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.release();
        mp = null;

        Toast.makeText(MediaPlayerService.this, "发生错误，停止播放！", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mMediaPlayer == null){return;}

        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                //程序取得声音播放权
                mMediaPlayer.setVolume(0.8f, 0.8f);
                mMediaPlayer.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //程序尚无声音播放权，而且时间可能很久
                stopSelf();  //结束这个Service
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //程序尚无声音播放权，但预期很快就会取得
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //程序尚无声音播放权，但是可以用很小的音量继续播放
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;

        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mMediaPlayer.isLooping()){  //循环播放
            Toast.makeText(MediaPlayerService.this, "循环开始", Toast.LENGTH_SHORT).show();
        }else{                          //不循环
            Toast.makeText(MediaPlayerService.this, "播放停止", Toast.LENGTH_SHORT).show();
            //这个地方还应该把第一个播放/暂停按钮的图标重新设置成暂停图标，省略了。。。
        }


    }




}
