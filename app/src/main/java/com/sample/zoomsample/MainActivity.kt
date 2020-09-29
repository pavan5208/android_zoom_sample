package com.sample.zoomsample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import us.zoom.sdk.*

class MainActivity : Activity() {

    private var mZoomSDK: ZoomSDK? = null
    private var mMeetingService: MeetingService? = null
    private var mInMeetingService: InMeetingService? = null

    private var mDefaultVideoView: MobileRTCVideoView? = null
    private var mDefaultVideoViewMgr: MobileRTCVideoViewManager? = null
    private var renderInfo: MobileRTCVideoUnitRenderInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initZoomSDK()
    }

    private fun initZoomSDK() {
        mZoomSDK = ZoomSDK.getInstance()
        // TODO: For the purpose of this demo app, we are storing the credentials in the client app itself. However, you should not use hard-coded values for your key/secret in your app in production.
        val params = ZoomSDKInitParams()
        params.appKey = "JC1AUOAHG484oTQrOEMkX3wySaO1NooPmRtZ"
        params.appSecret = "GFW8hOU1F0vHNfxoss3jxUgX9NMcxidegCKm"
        params.domain = "zoom.us"
        params.enableLog = true
        mZoomSDK?.initialize(this, listener, params)
    }

    val listener: ZoomSDKInitializeListener = object : ZoomSDKInitializeListener {

        override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
            if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                showSuccessToast("Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode)
            } else {
                showSuccessToast("Initialize Zoom SDK successfully.")
                //Auto login by caling Zoom api's by providing email we get
                // Zoom Token and Zoom Access Token, etc
                showCustomUI()
            }
        }

        override fun onZoomAuthIdentityExpired() {
            showSuccessToast("Zoom AuthIdentity Expired")
        }
    }

    private fun showCustomUI() {
        ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled = true
        val params = JoinMeetingParams()
        params.meetingNo = "76339632436"
        params.password = "BnbMA1"
        params.displayName = "nani"
        ZoomSDK.getInstance().meetingService?.joinMeetingWithParams(this, params, ZoomMeetingUISettingHelper.getJoinMeetingOptions())

        ZoomSDK.getInstance().meetingService.addListener(meetServiceListener)
        ZoomSDK.getInstance().inMeetingService.addListener(inMeetServiceListener)
//        ZoomSDK.getInstance().meetingSettingsHelper.enable720p(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        ZoomSDK.getInstance().inMeetingService?.removeListener(inMeetServiceListener)
        ZoomSDK.getInstance().meetingService?.removeListener(meetServiceListener);
    }

    private fun showSuccessToast(msg: String) {
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    val meetServiceListener = object : MeetingServiceListener {
        override fun onMeetingStatusChanged(
            meetingStatus: MeetingStatus?,
            errorCode: Int,
            internalErrorCode: Int
        ) {
            Log.d("meetServiceListener.TAG", "onMeetingStatusChanged $meetingStatus:$errorCode")

            if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
                showSuccessToast("Version of ZoomSDK is too low!")
            }

            if (meetingStatus == MeetingStatus.MEETING_STATUS_IDLE || meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
                showSuccessToast("MEETING_STATUS_IDLE")
                onMeetingJoined()
            }
        }

    }

    private fun onMeetingJoined() {

        if (ZoomSDK.getInstance().meetingService == null || ZoomSDK.getInstance().inMeetingService == null) {
            Toast.makeText(this@MainActivity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

    }

    val inMeetServiceListener = object : InMeetingServiceListener {
        override fun onMeetingActiveVideo(activeUserId: Long) {
            showSuccessToast("Version activeUserId")
            refreshVideo(activeUserId)
            Log.v("onMeetingActiveVideo","onMeetingActiveVideo :"+activeUserId)
        }

        override fun onFreeMeetingReminder(p0: Boolean, p1: Boolean, p2: Boolean) {
            showSuccessToast("onFreeMeetingReminder $p0")
            Log.v("onFreeMeetingReminder","onFreeMeetingReminder :"+p0)
        }

        override fun onJoinWebinarNeedUserNameAndEmail(p0: InMeetingEventHandler?) {
            showSuccessToast("onJoinWebinarNeedUserNameAndEmail $p0")
            Log.v("onJoinWebinarNe","onJoinWebinarNeedUserNameAndEmail :"+p0)
        }

        override fun onActiveVideoUserChanged(activeUserId: Long) {
            showSuccessToast("onActiveVideoUserChanged $activeUserId")
            refreshVideo(activeUserId)
            Log.v("onActiveVideoUse","onActiveVideoUserChanged :"+activeUserId)

        }

        override fun onActiveSpeakerVideoUserChanged(p0: Long) {
            showSuccessToast("onActiveSpeakerVideoUserChanged $p0")
            Log.v("onActiveSpeakerVideoUse","onActiveSpeakerVideoUserChanged :"+p0)
        }

        override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
            showSuccessToast("onChatMessageReceived $p0")
            Log.v("onChatMessageReceived","onChatMessageReceived :"+p0)
        }

        override fun onUserNetworkQualityChanged(p0: Long) {
            showSuccessToast("onUserNetworkQualityChanged $p0")
            Log.v("onUserNetworkQualit","onUserNetworkQualityChanged :"+p0)
        }

        override fun onMeetingUserJoin(p0: MutableList<Long>?) {
            showSuccessToast("onMeetingUserJoin $p0")
            Log.v("onMeetingUserJoin","onMeetingUserJoin :"+p0)
        }

        override fun onMeetingUserLeave(p0: MutableList<Long>?) {
            showSuccessToast("onMeetingUserLeave $p0")
            Log.v("onMeetingUserLeave","onMeetingUserLeave :"+p0)
        }

        override fun onMeetingFail(p0: Int, p1: Int) {
            showSuccessToast("onMeetingFail $p0")
            Log.v("onMeetingFail","onMeetingFail :"+p0)
        }

        override fun onUserAudioTypeChanged(p0: Long) {
            showSuccessToast("onUserAudioTypeChanged $p0")
            Log.v("onUserAudioTypeChanged","onUserAudioTypeChanged :"+p0)
        }

        override fun onMyAudioSourceTypeChanged(p0: Int) {
            showSuccessToast("onMyAudioSourceTypeChanged $p0")
            Log.v("onMyAudioSourceTd","onMyAudioSourceTypeChanged :"+p0)
        }

        override fun onSilentModeChanged(p0: Boolean) {
            showSuccessToast("onSilentModeChanged $p0")
            Log.v("onSilentModeChanged","onSilentModeChanged :"+p0)
        }

        override fun onMeetingCoHostChanged(p0: Long) {
            showSuccessToast("onMeetingCoHostChanged $p0")
            Log.v("onMeetingCoHostChan","onMeetingCoHostChanged :"+p0)
        }

        override fun onLowOrRaiseHandStatusChanged(p0: Long, p1: Boolean) {
            showSuccessToast("onLowOrRaiseHandStatusChanged $p0")
            Log.v("onLowOrRaiseHandSt","onLowOrRaiseHandStatusChanged :"+p0)
        }

        override fun onSinkAttendeeChatPriviledgeChanged(p0: Int) {
            showSuccessToast("onSinkAttendeeChatPriviledgeChanged $p0")
            Log.v("onSinkAttendeeChatPriv","onSinkAttendeeChatPriviledgeChanged :"+p0)

        }

        override fun onMeetingUserUpdated(p0: Long) {
            showSuccessToast("onMeetingUserUpdated $p0")
            Log.v("onMeetingUserUpdated","onMeetingUserUpdated :"+p0)
        }

        override fun onMeetingSecureKeyNotification(p0: ByteArray?) {
            Log.v("onMeetingSecureKeyNot","onMeetingSecureKeyNotification :"+p0)

        }

        override fun onMeetingNeedColseOtherMeeting(p0: InMeetingEventHandler?) {
            showSuccessToast("onMeetingNeedColseOtherMeeting $p0")
            Log.v("onMeetingNeedColseOt","onMeetingNeedColseOtherMeeting :"+p0)
        }

        override fun onMicrophoneStatusError(p0: InMeetingAudioController.MobileRTCMicrophoneError?) {
            showSuccessToast("onMicrophoneStatusError $p0")
            Log.v("onMicrophoneStatusError","onMicrophoneStatusError :"+p0)
        }

        override fun onHostAskStartVideo(p0: Long) {
            showSuccessToast("onHostAskStartVideo $p0")
            Log.v("onHostAskStartVideo","onHostAskStartVideo :"+p0)

        }

        override fun onSinkAllowAttendeeChatNotification(p0: Int) {
            showSuccessToast("onSinkAllowAttendeeChatNotification")
            Log.v("onSinkAllowAtt","onSinkAllowAttendeeChatNotification :"+p0)
        }

        override fun onWebinarNeedRegister() {
            showSuccessToast("onWebinarNeedRegister")
            Log.v("onWebinarNeedRegister","onWebinarNeedRegister :")
        }

        override fun onSpotlightVideoChanged(p0: Boolean) {
            showSuccessToast("onSpotlightVideoChanged $p0")
            Log.v("onSpotlightVideoChanged","onSpotlightVideoChanged :")
        }

        override fun onMeetingHostChanged(p0: Long) {
            showSuccessToast("onMeetingHostChanged $p0")
            Log.v("onMeetingHostChanged","onMeetingHostChanged : $p0")
        }

        override fun onMeetingLeaveComplete(p0: Long) {
            showSuccessToast("onMeetingLeaveComplete $p0")
            Log.v("onMeetingLeaveComplete","onMeetingLeaveComplete : $p0")
        }

        override fun onHostAskUnMute(p0: Long) {
            showSuccessToast("onHostAskUnMute $p0")
            Log.v("onHostAskUnMute","onHostAskUnMute : $p0")
        }

        override fun onUserAudioStatusChanged(p0: Long) {
            showSuccessToast("onUserAudioStatusChanged $p0")
            Log.v("onUserAudnged","onUserAudioStatusChanged : $p0")
        }

        override fun onUserNameChanged(p0: Long, p1: String?) {
            showSuccessToast("onUserNameChanged $p1")
            Log.v("onUserNameChanged","onUserNameChanged : $p1")
        }

        override fun onMeetingNeedPasswordOrDisplayName(
            p0: Boolean,
            p1: Boolean,
            p2: InMeetingEventHandler?
        ) {
            showSuccessToast("onMeetingNeedPasswordOrDisplayName $p0")
            Log.v("onMeetingNeedPasD","onMeetingNeedPasswordOrDisplayName : $p1")
        }

        override fun onUserVideoStatusChanged(p0: Long) {
            showSuccessToast("onUserVideoStatusChanged $p0")
            Log.v("onUserVideoStatusC","onUserVideoStatus : $p0")

        }

    }

    private fun refreshVideo(activeUserId: Long) {
//        val activeUserId = mInMeetingService?.activeShareUserID()
        activeUserId.let {
            mDefaultVideoView = MobileRTCVideoView(applicationContext)
            mDefaultVideoViewMgr = mDefaultVideoView?.videoViewManager
            mDefaultVideoView?.setZOrderMediaOverlay(true)
            renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
            renderInfo!!.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_FULL_FILLED
            renderInfo!!.is_border_visible = true
            mDefaultVideoViewMgr?.removeAllVideoUnits()
//            mDefaultVideoViewMgr?.addActiveVideoUnit(renderInfo)
            mDefaultVideoViewMgr?.addAttendeeVideoUnit(activeUserId, renderInfo)
            parent_layout?.removeAllViews()
            parent_layout?.addView(mDefaultVideoView)

        }

    }

}