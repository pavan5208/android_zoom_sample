package com.sample.zoomsample

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.sample.zoomsample.audio.MeetingAudioCallback
import com.sample.zoomsample.audio.MeetingAudioHelper
import com.sample.zoomsample.share.MeetingShareCallback
import com.sample.zoomsample.share.MeetingShareHelper
import com.sample.zoomsample.video.MeetingVideoCallback
import com.sample.zoomsample.video.MeetingVideoHelper
import kotlinx.android.synthetic.main.activity_main.*
import us.zoom.sdk.*
import us.zoom.sdk.MeetingError.*
import us.zoom.videomeetings.BuildConfig

class MainActivity : Activity(), MeetingAudioCallback.AudioEvent, MeetingVideoCallback.VideoEvent,
    MeetingShareCallback.ShareEvent {

    private var mZoomSDK: ZoomSDK? = null
    private var mDefaultVideoView: MobileRTCVideoView? = null
    private var mDefaultVideoViewMgr: MobileRTCVideoViewManager? = null
    private var renderInfo: MobileRTCVideoUnitRenderInfo? = null
    var meetingOptionBar: MeetingOptionBar? = null
    private var meetingAudioHelper: MeetingAudioHelper? = null

    private var mShareView: MobileRTCShareView? = null

    private val LAYOUT_TYPE_WAITHOST = 1
    private val LAYOUT_TYPE_IN_WAIT_ROOM = 2

    private val LAYOUT_TYPE_VIEW_SHARE = 6
    private val LAYOUT_TYPE_SHARING_VIEW = 7
    private var currentLayoutType = -1
    private var meetingShareHelper: MeetingShareHelper? = null
    private var meetingVideoHelper: MeetingVideoHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        loading_bar?.visibility = View.VISIBLE
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
                updateStatus("Initialize Zoom SDK successfully.")
                //Auto login by caling Zoom api's by providing email we get
                // Zoom Token and Zoom Access Token, etc
                showCustomUI()
            }
        }

        override fun onZoomAuthIdentityExpired() {
            showSuccessToast("Zoom AuthIdentity Expired")
        }
    }

    fun updateStatus(status: String) {
        txt_status_change?.text = status
    }

    private fun showCustomUI() {
        ZoomSDK.getInstance().meetingSettingsHelper.enable720p(false)
        ZoomSDK.getInstance().meetingService.addListener(meetServiceListener)
        ZoomSDK.getInstance().meetingSettingsHelper.setCustomizedNotificationData(null, handle)

        ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled = true
        val params = JoinMeetingParams()
        params.meetingNo = "76338698732"
        params.password = "cXBV47"
        params.displayName = "veera"
        ZoomSDK.getInstance().meetingService?.joinMeetingWithParams(
            this,
            params,
            ZoomMeetingUISettingHelper.getJoinMeetingOptions()
        )
    }


    var handle = InMeetingNotificationHandle { context, intent ->
        var intent = intent
        intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.action = InMeetingNotificationHandle.ACTION_RETURN_TO_CONF
        context.startActivity(intent)
        true
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
            updateStatus("onMeetingStatusChanged $meetingStatus $errorCode")

            if (errorCode == MEETING_ERROR_INCORRECT_MEETING_NUMBER || errorCode == MEETING_ERROR_MEETING_OVER || errorCode == MEETING_ERROR_MEETING_NOT_EXIST) {
                Toast.makeText(this@MainActivity, "The meeting doesn't exist", Toast.LENGTH_SHORT)
                    .show()
                finish()
            } else if (errorCode == MEETING_ERROR_NETWORK_UNAVAILABLE || errorCode == MEETING_ERROR_NETWORK_ERROR) {
                Toast.makeText(this@MainActivity, "Internet Issue", Toast.LENGTH_SHORT).show()
                finish()
            } else if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
                showSuccessToast("Version of ZoomSDK is too low!")
            } else if (meetingStatus == MeetingStatus.MEETING_STATUS_IDLE || meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
                showSuccessToast("MEETING_STATUS_IDLE")
            }else if(errorCode == MEETING_ERROR_SUCCESS && meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
//                onMeetingJoined()
                onMeetingJoined()
            }
        }

    }

    private fun onMeetingJoined() {
        updateStatus("onMeetingJoined")

        if (ZoomSDK.getInstance().meetingService == null || ZoomSDK.getInstance().inMeetingService == null) {
            Toast.makeText(this@MainActivity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        updateStatus("onMeetingJoined check done")
        meetingShareHelper = MeetingShareHelper(this, shareCallBack)
        MeetingShareCallback.getInstance().addListener(this)
        MeetingAudioCallback.getInstance().addListener(this)
        MeetingVideoCallback.getInstance().addListener(this)
        meetingAudioHelper =
            MeetingAudioHelper(audioCallBack)
        meetingVideoHelper =
            MeetingVideoHelper(this, videoCallBack)
        ZoomSDK.getInstance().meetingSettingsHelper.enable720p(true)

        val mMeetingOptionBar = MeetingOptionBar(this)
        meeting_option_contain?.removeAllViews()
        meeting_option_contain?.addView(mMeetingOptionBar)
        meetingOptionBar = mMeetingOptionBar
        meetingOptionBar?.setCallBack(callBack)
        updateStatus("before inMeetServiceListener set")

        ZoomSDK.getInstance().inMeetingService.addListener(inMeetServiceListener)
        updateStatus("after inMeetServiceListener set")

        checkShowVideoLayout()

    }

    val REQUEST_SHARE_SCREEN_PERMISSION = 1001

    val REQUEST_SYSTEM_ALERT_WINDOW = 1002
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SHARE_SCREEN_PERMISSION -> {
                if (resultCode != RESULT_OK) {
                    if (BuildConfig.DEBUG) Log.d(
                        "TAG",
                        "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION no ok "
                    )
                }
                startShareScreen(data)
            }
            REQUEST_SYSTEM_ALERT_WINDOW -> meetingShareHelper?.startShareScreenSession(
                mScreenInfoData
            )
        }
    }

    private var mScreenInfoData: Intent? = null

    @SuppressLint("NewApi")
    protected fun startShareScreen(data: Intent?) {
        if (data == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= 24 && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            mScreenInfoData = data
            startActivityForResult(
                intent,
                REQUEST_SYSTEM_ALERT_WINDOW
            )
        } else {
            meetingShareHelper!!.startShareScreenSession(data)
        }
    }

    private fun showViewShareLayout() {
        if (!isMySelfWebinarAttendee()) {
            mDefaultVideoView!!.visibility = View.VISIBLE
            mDefaultVideoView!!.setOnClickListener(null)
            mDefaultVideoView!!.setGestureDetectorEnabled(true)
            val shareUserId: Long = ZoomSDK.getInstance().getInMeetingService().activeShareUserID()
            val renderInfo1 = MobileRTCRenderInfo(0, 0, 100, 100)
            mDefaultVideoViewMgr!!.addShareVideoUnit(shareUserId, renderInfo1)
//            updateAttendeeVideos(mInMeetingService.getInMeetingUserList(), 0)
//            customShareView.setMobileRTCVideoView(mDefaultVideoView)
//            remoteControlHelper.refreshRemoteControlStatus()
        } else {
            mDefaultVideoView!!.visibility = View.VISIBLE
            mDefaultVideoView!!.setOnClickListener(null)
            mDefaultVideoView!!.setGestureDetectorEnabled(true)
            val shareUserId: Long = ZoomSDK.getInstance().getInMeetingService().activeShareUserID()
            val renderInfo1 = MobileRTCRenderInfo(0, 0, 100, 100)
            mDefaultVideoViewMgr!!.addShareVideoUnit(shareUserId, renderInfo1)
        }
//        mAdapter.setUserList(null)
//        mAdapter.notifyDataSetChanged()
//        videoListLayout.setVisibility(View.INVISIBLE)
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

        override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {
            showSuccessToast("onMeetingFail $errorCode")
            Log.v("onMeetingFail", "onMeetingFail :" + errorCode)
            video_view?.setVisibility(View.GONE)
            showJoinFailDialog(errorCode)
        }

        override fun onUserAudioTypeChanged(p0: Long) {
            showSuccessToast("onUserAudioTypeChanged $p0")
            Log.v("onUserAudioTypeChanged", "onUserAudioTypeChanged :" + p0)
        }

        override fun onMyAudioSourceTypeChanged(type: Int) {
            meetingAudioHelper?.onMyAudioSourceTypeChanged(type)
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

    private fun showJoinFailDialog(errorCode: Int) {
        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Meeting Fail")
            .setMessage("Error:$errorCode")
            .setPositiveButton(
                "Ok"
            ) { dialog, which -> finish() }.create()
        dialog.show()
    }

    private fun refreshVideo(activeUserId: Long) {
//        val activeUserId = mInMeetingService?.activeShareUserID()
        if (ZoomSDK.getInstance().inMeetingService.getUserInfoById(activeUserId).inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_HOST) {

            mDefaultVideoView = MobileRTCVideoView(applicationContext)
            mDefaultVideoViewMgr = mDefaultVideoView?.videoViewManager
            mDefaultVideoView?.setZOrderMediaOverlay(true)
            renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
            renderInfo!!.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_ORIGINAL
            renderInfo!!.is_border_visible = true
            mDefaultVideoViewMgr?.removeAllVideoUnits()
//            mDefaultVideoViewMgr?.addActiveVideoUnit(renderInfo)
            mDefaultVideoViewMgr?.addAttendeeVideoUnit(activeUserId, renderInfo)
            video_view?.visibility = View.VISIBLE
            mShareView = sharingView
            meeting_option_contain?.visibility = View.VISIBLE
            video_view?.removeAllViews()
            video_view?.addView(mDefaultVideoView)
            loading_bar?.visibility = View.GONE
        }
    }

    var callBack: MeetingOptionBar.MeetingOptionBarCallBack =
        object : MeetingOptionBar.MeetingOptionBarCallBack {
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
                meetingShareHelper?.onClickShare()
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
                if (ZoomSDK.getInstance().inMeetingService?.lowerAllHands() == MobileRTCSDKError.SDKERR_SUCCESS) Toast.makeText(
                    this@MainActivity,
                    "Lower all hands successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onClickReclaimHost() {
                if (ZoomSDK.getInstance().inMeetingService?.reclaimHost() == MobileRTCSDKError.SDKERR_SUCCESS) Toast.makeText(
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

    private fun isMySelfWebinarAttendee(): Boolean {
        val myUserInfo: InMeetingUserInfo =  ZoomSDK.getInstance().inMeetingService.getMyUserInfo()
        return if (myUserInfo != null &&  ZoomSDK.getInstance().inMeetingService.isWebinarMeeting()) {
            myUserInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_ATTENDEE
        } else false
    }

    private fun isMySelfWebinarHostCohost(): Boolean {
        val myUserInfo: InMeetingUserInfo = ZoomSDK.getInstance().inMeetingService.getMyUserInfo()
        return if (myUserInfo != null && ZoomSDK.getInstance().inMeetingService.isWebinarMeeting()) {
            (myUserInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_HOST
                    || myUserInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_COHOST)
        } else false
    }

    var shareCallBack: MeetingShareHelper.MeetingShareUICallBack =
        object : MeetingShareHelper.MeetingShareUICallBack {
            override fun showShareMenu(popupWindow: PopupWindow) {
                popupWindow.showAtLocation(
                    meetingOptionBar!!.parent as View,
                    Gravity.BOTTOM or Gravity.CENTER,
                    0,
                    150
                )
            }

            override fun getShareView(): MobileRTCShareView {
                return mShareView!!
            }
        }

    var videoCallBack: MeetingVideoHelper.VideoCallBack =
        object : MeetingVideoHelper.VideoCallBack {
            override fun requestVideoPermission(): Boolean {
                if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        1010
                    )
                    return false
                }
                return true
            }

            override fun showCameraList(popupWindow: PopupWindow) {
                popupWindow.showAsDropDown(meetingOptionBar!!.switchCameraView, 0, 20)
            }
        }

    var audioCallBack: MeetingAudioHelper.AudioCallBack =
        object : MeetingAudioHelper.AudioCallBack {
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

    override fun onUserVideoStatusChanged(userId: Long) {
        meetingOptionBar?.updateVideoButton()
    }

    override fun onResume() {
        super.onResume()
        checkShowVideoLayout()
        meetingVideoHelper?.checkVideoRotation(this)
    }

    private fun checkShowVideoLayout() {
        mDefaultVideoViewMgr = mDefaultVideoView?.videoViewManager
        updateStatus("checkShowVideoLayout")
        meeting_option_contain?.visibility = View.VISIBLE
        if (mDefaultVideoViewMgr != null) {
            val newLayoutType: Int = getNewVideoMeetingLayout()
            if (currentLayoutType != newLayoutType) {
                removeOldLayout(currentLayoutType)
                currentLayoutType = newLayoutType
                addNewLayout(newLayoutType)
            }
        }

    }

    private fun getNewVideoMeetingLayout(): Int {
        var newLayoutType = -1
        if (ZoomSDK.getInstance().meetingService.meetingStatus == MeetingStatus.MEETING_STATUS_WAITINGFORHOST) {
            newLayoutType = LAYOUT_TYPE_WAITHOST
            return newLayoutType
        }
        if (ZoomSDK.getInstance().meetingService.meetingStatus == MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM) {
            newLayoutType = LAYOUT_TYPE_IN_WAIT_ROOM
            return newLayoutType
        }
        if (meetingShareHelper!!.isOtherSharing) {
            newLayoutType = LAYOUT_TYPE_VIEW_SHARE
        } else if (meetingShareHelper!!.isSharingOut && !meetingShareHelper!!.isSharingScreen) {
            newLayoutType = LAYOUT_TYPE_SHARING_VIEW
        }
        return newLayoutType
    }


    private fun removeOldLayout(type: Int) {
        if (type == LAYOUT_TYPE_WAITHOST) {
            waitJoinView?.visibility = View.GONE
            video_view?.visibility = View.VISIBLE
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            waitingRoom?.visibility = View.GONE
            video_view?.visibility = View.VISIBLE
        }
        else if (type == LAYOUT_TYPE_SHARING_VIEW)
        {
            mShareView?.visibility = View.GONE
            video_view.visibility = View.VISIBLE
        }
    }

    private fun addNewLayout(type: Int) {
        if (type == LAYOUT_TYPE_WAITHOST) {
            waitJoinView.visibility = View.VISIBLE
            video_view?.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_IN_WAIT_ROOM) {
            waitingRoom?.visibility = View.VISIBLE
            video_view?.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_SHARING_VIEW) {
            mShareView?.visibility = View.VISIBLE
            video_view.visibility = View.GONE
        } else if (type == LAYOUT_TYPE_VIEW_SHARE) {
            showViewShareLayout()
        }
    }

    var mCurShareUserId: Long = -1

    override fun onShareActiveUser(userId: Long) {
        meetingShareHelper!!.onShareActiveUser(
            mCurShareUserId,
            userId
        )
        mCurShareUserId = userId
        meetingOptionBar!!.updateShareButton()
        checkShowVideoLayout()
    }

    override fun onShareUserReceivingStatus(userId: Long) {
    }
}