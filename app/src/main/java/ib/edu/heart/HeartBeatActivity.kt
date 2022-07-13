package ib.edu.heart

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.polar.androidblesdk.R
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarHrData
import java.util.*

class HeartBeatActivity : AppCompatActivity() {

    private var seconds = 0
    private var running = false
    private var intervals = 0
    private var heartBeat = 0
    private val waiting = "Oczekiwanie na rozpoczęcie badania ..."

    private var sensorRecord = arrayListOf<Int>()

    private var userRecord = arrayListOf<String>()

    private lateinit var textView: TextView
    private lateinit var userTextView: TextView
    private lateinit var buttonStart: Button
    private lateinit var userText: EditText
    private lateinit var submit: Button


    companion object {
        private const val TAG = "HeartBeatActivity"
        private const val API_LOGGER_TAG = "API LOGGER"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    //zamiast tego będzie przekazana lista z interwałami
    private var intervalsTable = arrayListOf<Int>()

    private val api: PolarBleApi by lazy {
        // Notice PolarBleApi.ALL_FEATURES are enabled
        PolarBleApiDefaultImpl.defaultImplementation(applicationContext, PolarBleApi.ALL_FEATURES)
    }

    private var startButtonClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.heart_beat_activity)
        textView =findViewById(R.id.status)
        buttonStart = findViewById(R.id.start_button)
        userText = findViewById(R.id.userHeartBeat)
        submit = findViewById(R.id.submitButton)
        userTextView = findViewById(R.id.userTextView)
        //przekazane interwały
        intervalsTable.addAll(listOf(10,20,5,15))

        disableEditText(userText)



        api.setApiLogger { s: String -> Log.d(HeartBeatActivity.API_LOGGER_TAG, s) }
        api.setApiCallback(object : PolarBleApiCallback() {

            override fun hrNotificationReceived(identifier: String, data: PolarHrData) {
                Log.d(HeartBeatActivity.TAG, "HR wartosć: ${data.hr} rrsMs: ${data.rrsMs} rr: ${data.rrs} contact: ${data.contactStatus} , ${data.contactStatusSupported}")
                if(running){
                    heartBeat += data.rrs.size
                }
            }

        })

        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds")
            running = savedInstanceState.getBoolean("running")
        }
        runTimer()


        buttonStart.setOnClickListener {
            running = true
        }

        submit.setOnClickListener {
            userRecord.add(userText.text.toString())
            println(userRecord.toString())
            disableEditText(userText)
            userText.text.clear()
        }





    }

    private fun disableEditText(editText: EditText) {
        userTextView.text = ""
        editText.isFocusable = false
        editText.isEnabled = false
        editText.isCursorVisible = false
        editText.setBackgroundColor(Color.TRANSPARENT)
        editText.hint = ""
        submit.visibility = View.INVISIBLE
    }

    private fun enableEditText(editText: EditText) {
        userTextView.text = "Wpisz przewidywaną ilość uderzeń serca"
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isEnabled = true
        editText.isCursorVisible = true
        editText.setBackgroundColor(Color.WHITE)
        submit.visibility = View.VISIBLE

    }



    private fun runTimer() {
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun run() {

                if (running) {
                    textView.text = "Licz uderzenia serca ..."
                    seconds++
                }
                handler.postDelayed({ this.run() }, 1000)

                if(intervals < intervalsTable.size) {
                    if (seconds == intervalsTable.get(intervals)) {
                        running = false
                        seconds = 0
                        intervals++
                        sensorRecord.add(heartBeat)
                        heartBeat = 0
                        textView.text = "Interwał zakończony. Wpisz ilość uderzeń serca i wystartuj kolejny interwał"
                        enableEditText(userText)

                    }
                }

            }


        }
        handler.postDelayed(runnable, 1000)
    }





}