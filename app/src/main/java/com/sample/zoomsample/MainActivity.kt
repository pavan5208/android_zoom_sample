package com.sample.zoomsample

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import us.zoom.sdk.*

class MainActivity : Activity(), MeetingAudioCallback.AudioEvent {

    private var mZoomSDK: ZoomSDK? = null
    private var mMeetingService: MeetingService? = null
    private var mInMeetingService: InMeetingService? = null

    private var mDefaultVideoView: MobileRTCVideoView? = null
    private var mDefaultVideoViewMgr: MobileRTCVideoViewManager? = null
    private var renderInfo: MobileRTCVideoUnitRenderInfo? = null
    var meetingOptionBar: MeetingOptionBar? = null
    private var meetingAudioHelper: MeetingAudioHelper? = null

    private val meetingVideoHelper: MeetingVideoHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        params.meetingNo = "72182278887"
        params.password = "MBe0SC"
        params.displayName = "veera"
        ZoomSDK.getInstance().meetingService?.joinMeetingWithParams(
            this,
            params,
            ZoomMeetingUISettingHelper.getJoinMeetingOptions()
        )

        ZoomSDK.getInstance().meetingService.addListener(meetServiceListener)
        ZoomSDK.getInstance().inMeetingService.addListener(inMeetServiceListener)
        MeetingAudioCallback.getInstance().addListener(this)
        meetingAudioHelper = MeetingAudioHelper(audioCallBack)
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
            Log.v("onMeetingActiveVideo", "onMeetingActiveVideo :" + activeUserId)
        }

        override fun onFreeMeetingReminder(p0: Boolean, p1: Boolean, p2: Boolean) {
            showSuccessToast("onFreeMeetingReminder $p0")
            Log.v("onFreeMeetingReminder", "onFreeMeetingReminder :" + p0)
        }

        override fun onJoinWebinarNeedUserNameAndEmail(p0: InMeetingEventHandler?) {
            showSuccessToast("onJoinWebinarNeedUserNameAndEmail $p0")
            Log.v("onJoinWebinarNe", "onJoinWebinarNeedUserNameAndEmail :" + p0)
        }

        override fun onActiveVideoUserChanged(activeUserId: Long) {
            showSuccessToast("onActiveVideoUserChanged $activeUserId")
            refreshVideo(activeUserId)
            Log.v("onActiveVideoUse", "onActiveVideoUserChanged :" + activeUserId)

        }

        override fun onActiveSpeakerVideoUserChanged(p0: Long) {
            showSuccessToast("onActiveSpeakerVideoUserChanged $p0")
            Log.v("onActiveSpeakerVideoUse", "onActiveSpeakerVideoUserChanged :" + p0)
        }

        override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
            showSuccessToast("onChatMessageReceived $p0")
            Log.v("onChatMessageReceived", "onChatMessageReceived :" + p0)
        }

        override fun onUserNetworkQualityChanged(p0: Long) {
            showSuccessToast("onUserNetworkQualityChanged $p0")
            Log.v("onUserNetworkQualit", "onUserNetworkQualityChanged :" + p0)
        }

        override fun onMeetingUserJoin(p0: MutableList<Long>?) {
            showSuccessToast("onMeetingUserJoin $p0")
            Log.v("onMeetingUserJoin", "onMeetingUserJoin :" + p0)
        }

        override fun onMeetingUserLeave(p0: MutableList<Long>?) {
            showSuccessToast("onMeetingUserLeave $p0")
            Log.v("onMeetingUserLeave", "onMeetingUserLeave :" + p0)
        }

        override fun onMeetingFail(p0: Int, p1: Int) {
            showSuccessToast("onMeetingFail $p0")
            Log.v("onMeetingFail", "onMeetingFail :" + p0)
        }

        override fun onUserAudioTypeChanged(p0: Long) {
            showSuccessToast("onUserAudioTypeChanged $p0")
            Log.v("onUserAudioTypeChanged", "onUserAudioTypeChanged :" + p0)
        }

        override fun onMyAudioSourceTypeChanged(p0: Int) {
            showSuccessToast("onMyAudioSourceTypeChanged $p0")
            Log.v("onMyAudioSourceTd", "onMyAudioSourceTypeChanged :" + p0)
        }

        override fun onSilentModeChanged(p0: Boolean) {
            showSuccessToast("onSilentModeChanged $p0")
            Log.v("onSilentModeChanged", "onSilentModeChanged :" + p0)
        }

        override fun onMeetingCoHostChanged(p0: Long) {
            showSuccessToast("onMeetingCoHostChanged $p0")
            Log.v("onMeetingCoHostChan", "onMeetingCoHostChanged :" + p0)
        }

        override fun onLowOrRaiseHandStatusChanged(p0: Long, p1: Boolean) {
            showSuccessToast("onLowOrRaiseHandStatusChanged $p0")
            Log.v("onLowOrRaiseHandSt", "onLowOrRaiseHandStatusChanged :" + p0)
        }

        override fun onSinkAttendeeChatPriviledgeChanged(p0: Int) {
            showSuccessToast("onSinkAttendeeChatPriviledgeChanged $p0")
            Log.v("onSinkAttendeeChatPriv", "onSinkAttendeeChatPriviledgeChanged :" + p0)

        }

        override fun onMeetingUserUpdated(p0: Long) {
            showSuccessToast("onMeetingUserUpdated $p0")
            Log.v("onMeetingUserUpdated", "onMeetingUserUpdated :" + p0)
        }

        override fun onMeetingSecureKeyNotification(p0: ByteArray?) {
            Log.v("onMeetingSecureKeyNot", "onMeetingSecureKeyNotification :" + p0)

        }

        override fun onMeetingNeedColseOtherMeeting(p0: InMeetingEventHandler?) {
            showSuccessToast("onMeetingNeedColseOtherMeeting $p0")
            Log.v("onMeetingNeedColseOt", "onMeetingNeedColseOtherMeeting :" + p0)
        }

        override fun onMicrophoneStatusError(p0: InMeetingAudioController.MobileRTCMicrophoneError?) {
            showSuccessToast("onMicrophoneStatusError $p0")
            Log.v("onMicrophoneStatusError", "onMicrophoneStatusError :" + p0)
        }

        override fun onHostAskStartVideo(p0: Long) {
            showSuccessToast("onHostAskStartVideo $p0")
            Log.v("onHostAskStartVideo", "onHostAskStartVideo :" + p0)

        }

        override fun onSinkAllowAttendeeChatNotification(p0: Int) {
            showSuccessToast("onSinkAllowAttendeeChatNotification")
            Log.v("onSinkAllowAtt", "onSinkAllowAttendeeChatNotification :" + p0)
        }

        override fun onWebinarNeedRegister() {
            showSuccessToast("onWebinarNeedRegister")
            Log.v("onWebinarNeedRegister", "onWebinarNeedRegister :")
        }

        override fun onSpotlightVideoChanged(p0: Boolean) {
            showSuccessToast("onSpotlightVideoChanged $p0")
            Log.v("onSpotlightVideoChanged", "onSpotlightVideoChanged :")
        }

        override fun onMeetingHostChanged(p0: Long) {
            showSuccessToast("onMeetingHostChanged $p0")
            Log.v("onMeetingHostChanged", "onMeetingHostChanged : $p0")
        }

        override fun onMeetingLeaveComplete(p0: Long) {
            showSuccessToast("onMeetingLeaveComplete $p0")
            Log.v("onMeetingLeaveComplete", "onMeetingLeaveComplete : $p0")
        }

        override fun onHostAskUnMute(p0: Long) {
            showSuccessToast("onHostAskUnMute $p0")
            Log.v("onHostAskUnMute", "onHostAskUnMute : $p0")
        }

        override fun onUserAudioStatusChanged(p0: Long) {
            showSuccessToast("onUserAudioStatusChanged $p0")
            Log.v("onUserAudnged", "onUserAudioStatusChanged : $p0")
        }

        override fun onUserNameChanged(p0: Long, p1: String?) {
            showSuccessToast("onUserNameChanged $p1")
            Log.v("onUserNameChanged", "onUserNameChanged : $p1")
        }

        override fun onMeetingNeedPasswordOrDisplayName(
            p0: Boolean,
            p1: Boolean,
            p2: InMeetingEventHandler?
        ) {
            showSuccessToast("onMeetingNeedPasswordOrDisplayName $p0")
            Log.v("onMeetingNeedPasD", "onMeetingNeedPasswordOrDisplayName : $p1")
        }

        override fun onUserVideoStatusChanged(p0: Long) {
            showSuccessToast("onUserVideoStatusChanged $p0")
            Log.v("onUserVideoStatusC", "onUserVideoStatus : $p0")

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
            video_view?.removeAllViews()
            video_view?.addView(mDefaultVideoView)
            val mMeetingOptionBar = MeetingOptionBar(this)
            meeting_option_contain?.removeAllViews()
            meeting_option_contain?.addView(mMeetingOptionBar)
            meetingOptionBar = mMeetingOptionBar
            meetingOptionBar?.setCallBack(callBack)
        }

    }

    var callBack: MeetingOptionBar.MeetingOptionBarCallBack = object : MeetingOptionBar.MeetingOptionBarCallBack {
        override fun onClickBack() {
//            onClickMiniWindow()
        }

        override fun onClickSwitchCamera() {
            meetingVideoHelper?.switchCamera()
        }

        override fun onClickLeave() {
//            showLeaveMeetingDialog()
        }

        override fun onClickAudio() {
            meetingAudioHelper?.switchAudio()
        }

        override fun onClickVideo() {
            meetingVideoHelper?.switchVideo()
        }

        override fun onClickShare() {
//            meetingShareHelper.onClickShare()
        }

        override fun onClickChats() {
//            mInMeetingService!!.showZoomParticipantsUI(
//                this@MainActivity,
//                us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity.REQUEST_PLIST
//            )
        }

        override fun onClickDisconnectAudio() {
            meetingAudioHelper?.disconnectAudio()
        }

        override fun onClickSwitchLoudSpeaker() {
            meetingAudioHelper?.switchLoudSpeaker()
        }

        override fun onClickAdminBo() {
//            val intent = Intent(this@MainActivity, BreakoutRoomsAdminActivity::class.java)
//            startActivity(intent)
        }

        override fun onClickLowerAllHands() {
            if (mInMeetingService!!.lowerAllHands() == MobileRTCSDKError.SDKERR_SUCCESS) Toast.makeText(
                this@MainActivity,
                "Lower all hands successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onClickReclaimHost() {
            if (mInMeetingService!!.reclaimHost() == MobileRTCSDKError.SDKERR_SUCCESS) Toast.makeText(
                this@MainActivity,
                "Reclaim host successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun showMoreMenu(popupWindow: PopupWindow) {
            popupWindow.showAtLocation(
                meetingOptionBar?.getParent() as View,
                Gravity.BOTTOM or Gravity.RIGHT,
                0,
                150
            )
        }

        override fun onHidden(hidden: Boolean) {
//            updateVideoListMargin(hidden)
        }
    }

    var audioCallBack: MeetingAudioHelper.AudioCallBack = object : MeetingAudioHelper.AudioCallBack {
        override fun requestAudioPermission(): Boolean {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1011
                )
                return false
            }
            return true
        }

        override fun updateAudioButton() {
            meetingOptionBar!!.updateAudioButton()
        }
    }

    override fun onUserAudioStatusChanged(userId: Long) {
        meetingAudioHelper?.onUserAudioStatusChanged(userId)
    }

    override fun onUserAudioTypeChanged(userId: Long) {
        meetingAudioHelper?.onUserAudioTypeChanged(userId)
    }

    override fun onMyAudioSourceTypeChanged(type: Int) {
        meetingAudioHelper?.onMyAudioSourceTypeChanged(type)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions == null || grantResults == null) {
            return
        }
        for (i in permissions.indices) {
            if (Manifest.permission.RECORD_AUDIO == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    meetingAudioHelper!!.switchAudio()
                }
            } else if (Manifest.permission.CAMERA == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    meetingVideoHelper!!.switchVideo()
                }
            }
        }
    }


}