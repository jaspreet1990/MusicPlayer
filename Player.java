package jazz.music.musicowl.mcappmedia.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import jazz.music.musicowl.mcappmedia.R;
import jazz.music.musicowl.mcappmedia.gif.HtGifView;
import jazz.music.musicowl.mcappmedia.models.MusicModel;
import jazz.music.musicowl.mcappmedia.service.MediaPlaybackService;
import jazz.music.musicowl.mcappmedia.util.Utilities;


import static jazz.music.musicowl.mcappmedia.R.drawable.btn_play;

/**
 * Created by jaspreet on 5/10/17.
 */

public class Player extends BaseActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {


    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    //private SongsManager songManager;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    
    private HtGifView iv;

    ArrayList<MusicModel> musicModels;
    int playPos=0;
    ImageView album_img;


    boolean isBinded = false;
    MediaPlaybackService mediaPlaybackService;

    //

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlaybackService = ((MediaPlaybackService.IDBinder)service).getService();
            isBinded = true;
            //initInfos(mediaPlaybackService.getFile());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBinded = false;
        }
    };
    BroadcastReceiver receiverElapsedTime;
    BroadcastReceiver receiverCompleted;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);


        receiverElapsedTime = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               
            }
        };
 
        receiverCompleted = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              
            }
        };






        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.j0);
        ImageView img =(ImageView) findViewById(R.id.ivrl);

        
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        iv      = (HtGifView) findViewById(R.id.iv);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        iv.setAnimatedGif(R.drawable.gif,
                HtGifView.TYPE.FIT_CENTER);

        if (getIntent().hasExtra("list")){
            musicModels = (ArrayList<MusicModel>) getIntent().getSerializableExtra("list");
            playPos=getIntent().getIntExtra("pos",0);
        }



        
        mp = new MediaPlayer();
    
        utils = new Utilities();

       
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

       
        playSong(playPos);

      
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                int resId;
                if (mediaPlaybackService.isPlaying()) {
                    resId = btn_play;
                    mediaPlaybackService.pause();
                } else {
                    resId = R.drawable.btn_pause;
                    mediaPlaybackService.play();
                }
                btnPlay.setImageResource(resId);

            }
        });

        
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
              
                int currentPosition = mp.getCurrentPosition();
             
                if(currentPosition + seekForwardTime <= mp.getDuration()){
                 
                    mp.seekTo(currentPosition + seekForwardTime);
                }else{
                   
                    mp.seekTo(mp.getDuration());
                }
            }
        });

       
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
               
                int currentPosition = mp.getCurrentPosition();
             
                if(currentPosition - seekBackwardTime >= 0){
                 
                    mp.seekTo(currentPosition - seekBackwardTime);
                }else{
                   
                    mp.seekTo(0);
                }

            }
        });

        
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                
                if(currentSongIndex < (musicModels.size() - 1)){
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                }else{
                   
                    playSong(0);
                    currentSongIndex = 0;
                }

            }
        });

        
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(currentSongIndex > 0){
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                }else{
                   
                    playSong(musicModels.size() - 1);
                   
                    currentSongIndex = musicModels.size() - 1;
                }

            }
        });

       
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

      


    @Override
    protected void onResume() {
        
        getApplicationContext().bindService(new Intent(getApplicationContext(),
                MediaPlaybackService.class), connection, BIND_AUTO_CREATE);

      
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverElapsedTime,
                new IntentFilter(MediaPlaybackService.MPS_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverCompleted,
                new IntentFilter(MediaPlaybackService.MPS_COMPLETED)
        );

        super.onResume();
    }

    @Override
    protected void onPause() {
         LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverElapsedTime);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCompleted);
        super.onPause();
    }
 
    public void  playSong(int songIndex){
   
        try {
            mp.reset();
           
            mp.setDataSource(musicModels.get(songIndex).getPath());
            mp.prepare();
            mp.start();
          
            String songTitle = musicModels.get(songIndex).getName();
            songTitleLabel.setText(songTitle);

             btnPlay.setImageResource(R.drawable.btn_pause);

             songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

             updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

   
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

             songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
             songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

             int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
             songProgressBar.setProgress(progress);

             mHandler.postDelayed(this, 100);
        }
    };

  
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
         mHandler.removeCallbacks(mUpdateTimeTask);
    }

    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        
        mp.seekTo(currentPosition);

       
        updateProgressBar();
    }

    
    @Override
    public void onCompletion(MediaPlayer arg0) {

      
        if(isRepeat){
          
            playSong(currentSongIndex);
        } else if(isShuffle){
         
            Random rand = new Random();
           
            currentSongIndex = rand.nextInt((musicModels.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else{
          
            if(currentSongIndex < (musicModels.size() - 1)){
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            }else{
               
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.release();
    }

    @Override
    public void onBackPressed() {
 
        mHandler.removeCallbacks(mUpdateTimeTask);


        super.onBackPressed();

    }
}