package com.sample.zoomsample.audio;


import com.sample.zoomsample.BaseCallback;
import com.sample.zoomsample.BaseEvent;
import com.sample.zoomsample.SimpleInMeetingListener;

import us.zoom.sdk.ZoomSDK;

public class MeetingAudioCallback extends BaseCallback<MeetingAudioCallback.AudioEvent> {

    public interface AudioEvent extends BaseEvent {

        void onUserAudioStatusChanged(long userId);

        void onUserAudioTypeChanged(long userId);

        void onMyAudioSourceTypeChanged(int type);
    }

    static MeetingAudioCallback instance;

    private MeetingAudioCallback() {
        init();
    }

    protected void init() {
        ZoomSDK.getInstance().getInMeetingService().addListener(audioListener);
    }

    public static MeetingAudioCallback getInstance() {
        if (null == instance) {
            synchronized (MeetingAudioCallback.class) {
                if (null == instance) {
                    instance = new MeetingAudioCallback();
                }
            }
        }
        return instance;
    }


    SimpleInMeetingListener audioListener = new SimpleInMeetingListener() {

        @Override
        public void onUserAudioStatusChanged(long userId) {

            for (AudioEvent event : callbacks) {
                event.onUserAudioStatusChanged(userId);
            }
        }

        @Override
        public void onUserAudioTypeChanged(long userId) {
            for (AudioEvent event : callbacks) {
                event.onUserAudioTypeChanged(userId);
            }
        }

        @Override
        public void onMyAudioSourceTypeChanged(int type) {
            for (AudioEvent event : callbacks) {
                event.onMyAudioSourceTypeChanged(type);
            }
        }
    };


}
