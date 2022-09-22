package ib.edu.heart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.polar.androidblesdk.R
import java.util.*


class IntervalCountChooserActivity : AppCompatActivity() {

    private lateinit var submitInterval: Button
    private lateinit var submitIntervalWhole: Button
    private lateinit var defaulSett: Button
    private var intervalsListAdapter: IntervalsListAdapter? = null
    private lateinit var etIntervals: EditText
    private lateinit var txt: TextView
    var isAllow = 0.toString()

    private lateinit var databaseHelper: DatabaseHelper
    val dataArray = arrayListOf<CustomListElement>()

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    "custom-message" -> {
                        val ok:String = intent.getStringExtra("allow").toString()
                        val data:String = intent.getStringExtra("elementsData").toString()
                        val gson = Gson()
                        var element = gson.fromJson(data, CustomListElement::class.java)

                        for (item in dataArray) {
                            if (item.countId == element.countId)
                                dataArray.remove(item)
                        }
                        dataArray.add(element)
                        println("data array: $dataArray")
                        isAllow = ok
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(mMessageReceiver)
    }

    private var listview: ListView? = null
    private var customElements = ArrayList<CustomListElement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_count_chooser)

        databaseHelper = DatabaseHelper(this)
        val codeSession:String = intent.getStringExtra("codeSession").toString()
        val codePatient:String = intent.getStringExtra("codePatient").toString()

        println("CODE SESSION: $codeSession, CODE PATIENT: $codePatient")
        etIntervals = findViewById(R.id.etCodePatient)
        setInputTypeNumber()
        listview = findViewById<ListView>(R.id.listIntervals)
        txt  = findViewById(R.id.tvIntervalChoose)
        submitInterval = findViewById<Button>(R.id.btnSubmitInterval)
        submitIntervalWhole = findViewById<Button>(R.id.btnSubmitCodes)
        defaulSett = findViewById(R.id.btnDalej)
        val filter = IntentFilter("custom-message")
        submitIntervalWhole.visibility = View.INVISIBLE


        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            filter
        )

        defaulSett.setOnClickListener {
            val intent = Intent(this, HeartBeatActivity::class.java)
            val dataJson = ArrayListObjectParser.toJson(dataArray)

            val data = databaseHelper.lastRow()

            if (data == null) {
                val toast = Toast.makeText(
                    applicationContext, "Brak zapisanych ustawień", Toast.LENGTH_LONG
                )
                toast.show()
            }
            else{
                intent.putExtra("arrayIntervals", databaseHelper.lastRow().toString())
                intent.putExtra("codeSession", codeSession)
                intent.putExtra("codePatient", codePatient)
                startActivity(intent)
            }



        }

        submitInterval.setOnClickListener {
            if (etIntervals.text.isEmpty()) {
                val toast = Toast.makeText(
                    applicationContext, "Najpierw należy wybrać " +
                            "ilość interwałów.", Toast.LENGTH_LONG
                )
                toast.show()
            } else {
                getData()
                submitIntervalWhole.visibility = View.VISIBLE
                submitInterval.visibility = View.INVISIBLE
                etIntervals.visibility = View.INVISIBLE

            }
        }

        submitIntervalWhole.setOnClickListener {
            if(isAllow == "0"){
                val toast = Toast.makeText(
                    applicationContext, "Należy wypełnić wszystkie pola długości" +
                            " interwałów.", Toast.LENGTH_LONG
                )
                toast.show()
            }else{
                val intent = Intent(this, HeartBeatActivity::class.java)
                val dataJson = ArrayListObjectParser.toJson(dataArray)
                intent.putExtra("arrayIntervals", dataJson)
                intent.putExtra("codeSession", codeSession)
                intent.putExtra("codePatient", codePatient)

                startActivity(intent)
            }
        }
    }

    private fun setInputTypeNumber() {
        etIntervals.inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun getData() {
        val intervalsCount = Integer.parseInt(etIntervals.text.toString())
        println(etIntervals.text.toString())
        customElements.clear()
        for (i in 0 until intervalsCount) {
            customElements.add(
                CustomListElement(
                    i + 1,
                    null
                )
            )
        }

        intervalsListAdapter = IntervalsListAdapter(customElements, this)

        listview!!.adapter = intervalsListAdapter
    }


}