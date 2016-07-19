package xiaodou.music.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import xiaodou.music.R;
import xiaodou.music.service.MediaPlayerService;

public class MainActivity extends AppCompatActivity {

    private ImageButton mBtnMediaPlayPause;
    private ImageButton mBtnMediaStop;
    private ImageButton mBtnMediaGoto;
    private ToggleButton mBtnMediaRepeat;
    private EditText mEditMediaGoto;

    //用来判断当前是播放状态，还是暂停状态。因为在MainActivity中获取不到Service的mMediaPlayer对象。
    private boolean MusicIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnMediaPlayPause = (ImageButton) findViewById(R.id.btnMediaPlayPause);
        mBtnMediaStop = (ImageButton) findViewById(R.id.btnMediaStop);
        mBtnMediaRepeat = (ToggleButton) findViewById(R.id.btnMediaRepeat);
        mBtnMediaGoto = (ImageButton) findViewById(R.id.btnMediaGoto);
        mEditMediaGoto = (EditText) findViewById(R.id.edtMediaGoto);

        mBtnMediaPlayPause.setOnClickListener(btnMediaPlayPauseOnClick);
        mBtnMediaStop.setOnClickListener(btnMediaStopOnClick);
        mBtnMediaRepeat.setOnClickListener(btnMediaRepeatOnClick);
        mBtnMediaGoto.setOnClickListener(btnMediaGotoOnClick);

    }

    //绑定btnMediaPlayPauseOnClick的点击事件
    private View.OnClickListener btnMediaPlayPauseOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if(MusicIsPlaying == true){ //假如原来正在播放，现在就要设置成暂停
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);//暂停图标
//                mMediaPlayer.pause();
                Intent it = new Intent(MainActivity.this, MediaPlayerService.class);
                it.setAction(MediaPlayerService.ACTION_PAUSE);
                startService(it);
                MusicIsPlaying = false;
            }else {             //假如原来是暂停状态，现在就要设置成播放
                mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_play);//播放图标

                Intent it = new Intent(MainActivity.this, MediaPlayerService.class);
                it.setAction(MediaPlayerService.ACTION_PLAY);
                startService(it);
                MusicIsPlaying = true;
            }
        }
    };

    //绑定btnMediaStopOnClick的点击事件
    private View.OnClickListener btnMediaStopOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            mMediaPlayer.stop();

            //停止播放后必须再运行prepare()或prepareAsync()才能重新播放
//            mbIsInitial = true;
            //把前一个播放/暂停按钮的图标重新设置成暂停图标
            mBtnMediaPlayPause.setImageResource(android.R.drawable.ic_media_pause);//暂停图标

            Intent it = new Intent(MainActivity.this, MediaPlayerService.class);
            it.setAction(MediaPlayerService.ACTION_STOP);
            startService(it);

            MusicIsPlaying = false;
        }
    };

    //绑定btnMediaRepeatOnClick的点击事件
    private View.OnClickListener btnMediaRepeatOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (((ToggleButton)view).isChecked()){
//                mMediaPlayer.setLooping(true);//循环
                Intent it = new Intent(MainActivity.this, MediaPlayerService.class);
                it.setAction(MediaPlayerService.ACTION_SET_REPEAT);
                startService(it);
            }else{
//                mMediaPlayer.setLooping(false);
                Intent it = new Intent(MainActivity.this, MediaPlayerService.class);
                it.setAction(MediaPlayerService.ACTION_CANCEL_REPEAT);
                startService(it);
            }
        }
    };

    //绑定btnMediaGotoOnClick的点击事件
    private View.OnClickListener btnMediaGotoOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEditMediaGoto.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "请先输入要播放的位置(单位是秒)", Toast.LENGTH_SHORT).show();
            }

            int seconds = Integer.parseInt(mEditMediaGoto.getText().toString());

            Intent it = new Intent(MainActivity.this, MediaPlayerService.class);
            it.setAction(MediaPlayerService.ACTION_GOTO);
            it.putExtra("GOTO_POSITION_SECONDS", seconds);
            startService(it);

            mEditMediaGoto.setText("");

//            mMediaPlayer.seekTo(seconds * 1000);
        }
    };


}
