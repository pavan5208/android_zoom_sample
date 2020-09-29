package com.sample.zoomsample

import android.os.Bundle
import android.widget.Toast
import us.zoom.sdk.*

class ZoomCustomActivity: MeetingActivity() {

    var mZoomSDK : ZoomSDK ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layou_custom_ui)
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
        /**
         * @param errorCode [us.zoom.sdk.ZoomError.ZOOM_ERROR_SUCCESS] if the SDK has been initialized successfully.
         */
        override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
            if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                showSuccessToast( "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode)
            } else {
                showSuccessToast( "Initialize Zoom SDK successfully.")
                //Auto login by caling Zoom api's by providing email we get
                // Zoom Token and Zoom Access Token, etc
                ZoomSDK.getInstance().smsService.enableZoomAuthRealNameMeetingUIShown(true)

                val params = JoinMeetingParams()
                params.meetingNo = "72873628611"
                params.password = "VcH4L3"
                params.displayName = "Satyaaaa"
                val options = JoinMeetingOptions()
                ZoomSDK.getInstance().meetingService.joinMeetingWithParams(this@ZoomCustomActivity, params, ZoomMeetingUISettingHelper.getJoinMeetingOptions())

            }
        }
        override fun onZoomAuthIdentityExpired() {
            showSuccessToast("Zoom AuthIdentity Expired")
        }
    }
    private fun showSuccessToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}