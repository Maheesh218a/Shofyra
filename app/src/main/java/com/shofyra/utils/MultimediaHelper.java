package com.shofyra.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * MultimediaHelper — camera capture, gallery picker, audio & video playback.
 * Covers: Multimedia
 *
 * Required permissions:
 *   <uses-permission android:name="android.permission.CAMERA"/>
 *   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 *   <uses-permission android:name="android.permission.RECORD_AUDIO"/>
 */
public class MultimediaHelper {

    public static final int REQUEST_CAMERA  = 401;
    public static final int REQUEST_GALLERY = 402;
    public static final int REQUEST_AUDIO   = 403;

    private final Activity activity;
    private MediaPlayer mediaPlayer;

    public MultimediaHelper(Activity activity) {
        this.activity = activity;
    }

    // ── Camera ────────────────────────────────────

    /** Open camera for photo capture. Returns Bitmap in onActivityResult via REQUEST_CAMERA */
    public void openCamera() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    /** Open camera with file URI — full resolution photo */
    public void openCameraToFile(Uri outputUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }

    // ── Gallery ───────────────────────────────────

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_GALLERY);
    }

    /** Multi-select gallery picker */
    public void openGalleryMultiSelect() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(intent, REQUEST_GALLERY);
    }

    // ── Audio Playback ────────────────────────────

    public void playNotificationSound() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(activity, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mp.release());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playAudioFromUrl(String url) {
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                mp.release();
                return true;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // ── Video Playback ────────────────────────────

    public void playVideo(VideoView videoView, String url) {
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.start();
        });
    }

    public void playLocalVideo(VideoView videoView, Uri fileUri) {
        videoView.setVideoURI(fileUri);
        videoView.start();
    }

    // ── Cleanup ───────────────────────────────────

    public void release() {
        stopAudio();
    }
}