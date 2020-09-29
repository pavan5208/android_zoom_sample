package com.sample.zoomsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import us.zoom.sdk.*

class MainActivity : AppCompatActivity() {

    private var mZoomSDK : ZoomSDK?= null
    private var mMeetingService: MeetingService? = null
    private var mInMeetingService: InMeetingService? = null

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
                showSuccessToast( "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode)
            } else {
                showSuccessToast( "Initialize Zoom SDK successfully.")
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
        params.meetingNo = "72873628611"
        params.password = "VcH4L3"
        params.displayName = "Sss"
        mMeetingService?.joinMeetingWithParams(this, params, ZoomMeetingUISettingHelper.getJoinMeetingOptions())

        mMeetingService = ZoomSDK.getInstance().meetingService
        mInMeetingService = ZoomSDK.getInstance().inMeetingService

        if (mMeetingService == null || mInMeetingService == null) {
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mInMeetingService?.addListener(inMeetServiceListener)

    }

    override fun onDestroy() {
        super.onDestroy()
        mInMeetingService?.removeListener(inMeetServiceListener);
    }
    private fun showSuccessToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    val meetServiceListener =object :MeetingServiceListener{
        override fun onMeetingStatusChanged(meetingStatus: MeetingStatus?, errorCode: Int, internalErrorCode: Int) {

            if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
                showSuccessToast("Version of ZoomSDK is too low!")
            }

            if (meetingStatus == MeetingStatus.MEETING_STATUS_IDLE || meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
                showSuccessToast("MEETING_STATUS_IDLE")
            }
        }

    }

    val inMeetServiceListener =object : InMeetingServiceListener{
        override fun onMeetingActiveVideo(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onFreeMeetingReminder(p0: Boolean, p1: Boolean, p2: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onJoinWebinarNeedUserNameAndEmail(p0: InMeetingEventHandler?) {
            TODO("Not yet implemented")
        }

        override fun onActiveVideoUserChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onActiveSpeakerVideoUserChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
            TODO("Not yet implemented")
        }

        override fun onUserNetworkQualityChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onMeetingUserJoin(p0: MutableList<Long>?) {
            TODO("Not yet implemented")
        }

        override fun onMeetingUserLeave(p0: MutableList<Long>?) {
            TODO("Not yet implemented")
        }

        override fun onMeetingFail(p0: Int, p1: Int) {
            TODO("Not yet implemented")
        }

        override fun onUserAudioTypeChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onMyAudioSourceTypeChanged(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun onSilentModeChanged(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onMeetingCoHostChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onLowOrRaiseHandStatusChanged(p0: Long, p1: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onSinkAttendeeChatPriviledgeChanged(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun onMeetingUserUpdated(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onMeetingSecureKeyNotification(p0: ByteArray?) {
            TODO("Not yet implemented")
        }

        override fun onMeetingNeedColseOtherMeeting(p0: InMeetingEventHandler?) {
            TODO("Not yet implemented")
        }

        override fun onMicrophoneStatusError(p0: InMeetingAudioController.MobileRTCMicrophoneError?) {
            TODO("Not yet implemented")
        }

        override fun onHostAskStartVideo(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onSinkAllowAttendeeChatNotification(p0: Int) {
            TODO("Not yet implemented")
        }

        override fun onWebinarNeedRegister() {
            TODO("Not yet implemented")
        }

        override fun onSpotlightVideoChanged(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onMeetingHostChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onMeetingLeaveComplete(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onHostAskUnMute(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onUserAudioStatusChanged(p0: Long) {
            TODO("Not yet implemented")
        }

        override fun onUserNameChanged(p0: Long, p1: String?) {
            TODO("Not yet implemented")
        }

        override fun onMeetingNeedPasswordOrDisplayName(
                p0: Boolean,
                p1: Boolean,
                p2: InMeetingEventHandler?
        ) {
            TODO("Not yet implemented")
        }

        override fun onUserVideoStatusChanged(p0: Long) {
            TODO("Not yet implemented")
        }

    }

}