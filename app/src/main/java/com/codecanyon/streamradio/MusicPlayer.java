package com.codecanyon.streamradio;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.widget.MediaController;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import com.codecanyon.radio.R;

/**
 * Created by User on 2014.07.03..
 */
public class MusicPlayer  {
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private static boolean isStarted = false;
    private static String trackTitle = "";
    private static String radioName = "";
    private Context context;
    private static SimpleExoPlayer player;
    private ConnectivityManager cm;
    private NetworkInfo netInfo;
    private RadioListElement radioListElement;
    private Timer timer = new Timer();
    private boolean timerIndicator = false;
    private static boolean isPaused = false;
    public static boolean isWorking() {
        return isWorking;
    }
    public static boolean isPaused(){return  isPaused;}
    public static void setIsWorking(boolean isWorking) {
        MusicPlayer.isWorking = isWorking;
    }
    public static MediaSource audioSource;
    private static boolean isWorking = true;

    public static String getRadioName() {
        return radioName;
    }

    public static String getTrackTitle() {
        return trackTitle;
    }

    public static boolean isStarted() {
        return isStarted;
    }
    public static void pauseMediaPlayer(){

        isStarted  = false;
        isPaused = true;
        if(player != null) {
            player.setPlayWhenReady(false);
        }
        else{
            player.prepare(audioSource,true);
        }
        //audioSource.releasePeriod ();

    }
    public static void stopMediaPlayer() {
        isStarted = false;
        player.setPlayWhenReady(false);
        player.stop();
    }
    //joket code
    public static long getCurrentPosition(){
        if(player == null ) return 0;
        else  return player.getCurrentPosition();
    }
    public static int getCurrentPercent(){
        if(player == null) return 0;
        else return player.getBufferedPercentage();
    }
    //public static long getBufferedPosition{return player.getBufferedPosition ();}
    public void resumePlay(RadioListElement rle, long currentPosition){
        if (player != null) {
            //player.prepare(audioSource,false);
            isWorking = true;
            isStarted = true;
            isPaused = false;
            //player.seekTo (currentPosition);
            player.setPlayWhenReady (true);
            startThread();
        }
        else play(rle,currentPosition);
    }
    public void play(RadioListElement rle, long currentPosition) {
        startThread();
        isPaused = false;
        isWorking = true;
        isStarted = true;
        radioListElement = rle;
        context = radioListElement.getContext();
        MainActivity.setViewPagerSwitch();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new Handler(), new AdaptiveVideoTrackSelection.Factory(bandwidthMeter));

        DefaultLoadControl loadControl = new DefaultLoadControl ();
       // loadControl.shouldContinueLoading (100000);
       // loadControl.shouldStartPlayback (100000, true);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector,loadControl, context.getString(R.string.item_purchase_code));
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "streamradio"), bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        audioSource = new ExtractorMediaSource(Uri.parse(radioListElement.getUrl()), dataSourceFactory, extractorsFactory, 1000000,null, null);

        //audioSource.releasePeriod ();
        player.prepare(audioSource,false);
        //player.seekToDefaultPosition ();
        //player.seekTo(currentPosition);
        player.setPlayWhenReady(true);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                if(isLoading){
                    MainActivity.startBufferingAnimation();
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState==3){
                    MainActivity.stopBufferingAnimation();
                    isStarted = true;
                }else{
                    isStarted = false;
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                System.out.println("Source Error: "+error);
                isWorking = false;
                try {
                    cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    netInfo = cm.getActiveNetworkInfo();
                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                        MainActivity.stopBufferingAnimation();
                    } else {
                        MainActivity.stopBufferingAnimation();
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

            @Override
            public void onPositionDiscontinuity() {

            }

        });
        radioListElement.getName();
        radioName = radioListElement.getName();
    }
    public void startThread() {
        if (!timerIndicator) {
            timerIndicator = true;
            timer.schedule(new TimerTask() {
                public void run() {
                    if (isStarted) {
                        URL url;
                        try {
                            url = new URL(radioListElement.getUrl());
                            IcyStreamMeta icy = new IcyStreamMeta(url);
                            if (icy.getArtist().length() > 0 && icy.getTitle().length() > 0) {
                                String title = icy.getArtist() + " - " + icy.getTitle();
                                trackTitle = new String(title.getBytes("ISO-8859-1"), "UTF-8");
                            } else {
                                String title = icy.getArtist() + "" + icy.getTitle();
                                trackTitle = new String(title.getBytes("ISO-8859-1"), "UTF-8");
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 1000);
        }
    }
}
