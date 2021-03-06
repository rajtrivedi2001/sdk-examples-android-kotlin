package com.gruveo.sdk.kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gruveo.sdk.Gruveo
import com.gruveo.sdk.model.CallEndReason
import com.gruveo.sdk.model.CallEndReason.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class MainActivity : AppCompatActivity() {
    private val REQUEST_CALL = 1
    private val SIGNER_URL = "https://api-demo.gruveo.com/signer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_video_button.setOnClickListener {
            initCall(true)
        }

        main_voice_button.setOnClickListener {
            initCall(false)
        }
    }

    private fun initCall(videoCall: Boolean) {
        val otherExtras = Bundle().apply {
            putBoolean(Gruveo.GRV_EXTRA_VIBRATE_IN_CHAT, false)
            putBoolean(Gruveo.GRV_EXTRA_DISABLE_CHAT, false)
        }

        val code = main_edittext.text.toString()
        val result = Gruveo.Builder(this)
                .callCode(code)
                .videoCall(videoCall)
                .clientId("demo")
                .requestCode(REQUEST_CALL)
                .otherExtras(otherExtras)
                .eventsListener(eventsListener)
                .build()

        when (result) {
            Gruveo.GRV_INIT_MISSING_CALL_CODE -> { }
            Gruveo.GRV_INIT_INVALID_CALL_CODE -> { }
            Gruveo.GRV_INIT_MISSING_CLIENT_ID -> { }
            Gruveo.GRV_INIT_OFFLINE -> { }
            else -> { }
        }
    }

    private val eventsListener = object : Gruveo.EventsListener {
        override fun callInit(videoCall: Boolean, code: String) {
        }

        override fun requestToSignApiAuthToken(token: String) {
            Gruveo.authorize(signToken(token))
        }

        override fun callEstablished(code: String) {
        }

        override fun callEnd(data: Intent, isInForeground: Boolean) {
            parseCallExtras(data)
        }

        override fun recordingStateChanged(us: Boolean, them: Boolean) {
        }
    }

    private fun signToken(token: String): String {
        val body = RequestBody.create(MediaType.parse("text/plain"), token)
        val request = Request.Builder()
                .url(SIGNER_URL)
                .post(body)
                .build()

        val response = OkHttpClient().newCall(request).execute()
        return response.body()?.string() ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CALL && resultCode == Activity.RESULT_OK && data != null) {
            parseCallExtras(data)
        }
    }

    private fun parseCallExtras(data: Intent) {
        val endReason = data.getSerializableExtra(Gruveo.GRV_RES_CALL_END_REASON)
        val callCode = data.getStringExtra(Gruveo.GRV_RES_CALL_CODE)
        val leftMessageTo = data.getStringExtra(Gruveo.GRV_RES_LEFT_MESSAGE_TO)
        val duration = data.getIntExtra(Gruveo.GRV_RES_CALL_DURATION, 0)
        val messagesSent = data.getIntExtra(Gruveo.GRV_RES_MESSAGES_SENT, 0)

        when (endReason as CallEndReason) {
            BUSY -> { }
            HANDLE_BUSY -> {}
            HANDLE_UNREACHABLE -> { }
            HANDLE_NONEXIST -> { }
            FREE_DEMO_ENDED -> { }
            ROOM_LIMIT_REACHED -> { }
            NO_CONNECTION -> { }
            INVALID_CREDENTIALS -> { }
            UNSUPPORTED_PROTOCOL_VERSION -> { }
            OTHER_PARTY -> { }
            else -> { }     // USER - we hanged up the call
        }
    }
}
