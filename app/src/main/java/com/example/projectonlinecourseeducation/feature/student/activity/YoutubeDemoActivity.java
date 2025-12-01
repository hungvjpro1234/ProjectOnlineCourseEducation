package com.example.projectonlinecourseeducation.feature.student.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectonlinecourseeducation.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class YoutubeDemoActivity extends AppCompatActivity {

    // Video test
    private static final String VIDEO_ID = "mtL4fOWm3vY";

    private YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_demo);

        youTubePlayerView = findViewById(R.id.youtubePlayerView);

        // Để thư viện tự handle pause/resume/destroy theo lifecycle của Activity
        getLifecycle().addObserver(youTubePlayerView);

        // Nếu bạn muốn truyền videoId từ Intent:
        // String videoId = getIntent().getStringExtra("video_id");
        // final String finalVideoId = (videoId != null) ? videoId : VIDEO_ID;

        final String finalVideoId = VIDEO_ID;

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                // load video và auto play từ giây 0
                youTubePlayer.loadVideo(finalVideoId, 0f);
            }

            // TRACKING PROGRESS: thời gian hiện tại của video (giây)
            @Override
            public void onCurrentSecond(YouTubePlayer youTubePlayer, float second) {
                super.onCurrentSecond(youTubePlayer, second);
                Log.d("YT_PLAYER", "currentSecond = " + second);
                // TODO: update UI / lưu progress bài học ở đây
            }

            // TRACKING: tổng thời lượng video (giây)
            @Override
            public void onVideoDuration(YouTubePlayer youTubePlayer, float duration) {
                super.onVideoDuration(youTubePlayer, duration);
                Log.d("YT_PLAYER", "duration = " + duration);
                // TODO: dùng duration để tính phần trăm hoàn thành
            }

            // Bạn có thể override thêm onStateChange, onPlaybackRateChange,... nếu cần
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nếu không muốn phụ thuộc lifecycle observer thì vẫn có thể release thủ công
        youTubePlayerView.release();
    }
}
