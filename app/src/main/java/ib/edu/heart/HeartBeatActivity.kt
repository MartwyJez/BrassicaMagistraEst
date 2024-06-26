package ib.edu.heart

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarHrData
import java.lang.reflect.Modifier
import java.util.*

class HeartBeatActivity : AppCompatActivity() {
    private var mCsvLogger: CsvLogger? = null
    private var seconds = 0
    private var running = false
    private var intervals = 0
    private var heartBeat = 0
    private val waiting = "Oczekiwanie na rozpoczęcie badania ..."
    private var isLogSaved = false
    private var sensorRecord = arrayListOf<Int>()

    private var userRecord = arrayListOf<Int>()

    private val LOG_TAG: String = HeartBeatActivity::class.java.simpleName

    private lateinit var textView: TextView
    private lateinit var userTextView: TextView
    private lateinit var buttonStart: Button
    private lateinit var userText: EditText
    private lateinit var submit: Button

    private var badValues = 0


    companion object {
        private const val TAG = "HeartBeatActivity"
        private const val API_LOGGER_TAG = "API LOGGER"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    //zamiast tego będzie przekazana lista z interwałami
    private var intervalsTable = arrayListOf<CustomListElement>()

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
        val data:String = intent.getStringExtra("arrayIntervals").toString()
        val codeSession:String = intent.getStringExtra("codeSession").toString()
        val codePatient:String = intent.getStringExtra("codePatient").toString()
        println("CODE SESSION: $codeSession, CODE PATIENT: $codePatient")

        mCsvLogger = CsvLogger(codeSession, codePatient)


        mCsvLogger!!.checkRuntimeWriteExternalStoragePermission(this, this)
        intervalsTable = ArrayListObjectParser.fromJson(data) as ArrayList<CustomListElement>


        disableEditText(userText)

        mCsvLogger!!.appendHeader("Lp., Interval [s], User expectations, Real EKG score")

        api.setApiLogger { s: String -> Log.d(HeartBeatActivity.API_LOGGER_TAG, s) }
        api.setApiCallback(object : PolarBleApiCallback() {

            override fun hrNotificationReceived(identifier: String, data: PolarHrData) {
                Log.d(TAG, "HR wartosć: ${data.hr} rrsMs: ${data.rrsMs} rr: ${data.rrs} contact: ${data.contactStatus} , ${data.contactStatusSupported}")

                if(data.rrs.size == 0){
                    badValues += 1

                }

                if(badValues >= 3){

                    val toast = Toast.makeText(
                        applicationContext, "Należy upewnić się, czy czujnik jest podłączony prawidłowo!", Toast.LENGTH_LONG
                    )
                    toast.show()

                    badValues = 0
                }

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
            isLogSaved = false
            running = true
            buttonStart.visibility = View.INVISIBLE
        }
        var i = 0

        submit.setOnClickListener {
            if(userText.text.isEmpty()){
                val toast = Toast.makeText(
                    applicationContext, "Należy wpisać ilość udrzeń serca", Toast.LENGTH_LONG
                )
                toast.show()
            }
            else {
                textView.text = "Wystartuj kolejny interwał"
                buttonStart.visibility = View.VISIBLE
                userRecord.add(Integer.parseInt(userText.text.toString()))
//            userRecord.add(userText.text.toString())
                mCsvLogger!!.appendLine(
                    String.format(
                        Locale.getDefault(),
                        "%s,%s,%s,%s",
                        (i + 1).toString(),
                        intervalsTable.get(i).duration.toString(),
                        userRecord[i].toString(),
                        sensorRecord[i].toString()
                    )
                )
                i++
                if (intervals == intervalsTable.size) {
                    if (!isLogSaved) {
                        mCsvLogger!!.finishSavingLogs(this, LOG_TAG)
                        isLogSaved = true
                    }
                }
                disableEditText(userText)
                userText.text.clear()

                intent = Intent(this, Data::class.java)

                if (userRecord.size == intervalsTable.size) {
                    val gson = GsonBuilder()
                        .excludeFieldsWithModifiers(
                            Modifier.FINAL,
                            Modifier.TRANSIENT,
                            Modifier.STATIC
                        )
                        .serializeNulls()
                        .create()
                    val json = gson.toJson(userRecord)

                    intent.putExtra("user", json)

                    val gson1 = GsonBuilder()
                        .excludeFieldsWithModifiers(
                            Modifier.FINAL,
                            Modifier.TRANSIENT,
                            Modifier.STATIC
                        )
                        .serializeNulls()
                        .create()
                    val json1 = gson1.toJson(sensorRecord)

                    intent.putExtra("sensor", json1)

                    intent.putExtra("dataArray", data)


                    startActivity(intent)

                }
            }
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
        userTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        userTextView.width = 700
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isEnabled = true
        editText.isCursorVisible = true
        editText.setBackgroundResource(R.drawable.border)
        editText.hint = "Ilość uderzeń serca"
        editText.width = 275
        submit.visibility = View.VISIBLE
        submit.width = 275

    }



    private fun runTimer() {
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun run() {
                if(intervals < intervalsTable.size) {
                if (running) {
                    textView.text = "Licz uderzenia serca ..."
                    seconds++
                }
                handler.postDelayed({ this.run() }, 1000)


                    if (seconds == intervalsTable.get(intervals).duration) {
                        running = false
                        seconds = 0
                        intervals++
                        sensorRecord.add(heartBeat)
                        println(sensorRecord.toString())
                        heartBeat = 0
                        textView.text = "Interwał zakończony. Wpisz ilość uderzeń serca"
                        enableEditText(userText)

                    }
                }

            }

        }


        handler.postDelayed(runnable, 1000)


    }





}