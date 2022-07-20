package ib.edu.heart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.polar.androidblesdk.R

import java.util.*


class IntervalCountChooserActivity : AppCompatActivity() {

    private lateinit var submitInterval: Button
    private lateinit var submitIntervalWhole: Button
    private var intervalsListAdapter: IntervalsListAdapter? = null
    private lateinit var etIntervals: EditText
    var isAllow = 0.toString()

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

        etIntervals = findViewById(R.id.etIntervalCount)
        setInputTypeNumber()
        listview = findViewById<ListView>(R.id.listIntervals)
        submitInterval = findViewById<Button>(R.id.btnSubmitInterval)
        submitIntervalWhole = findViewById<Button>(R.id.btnSubmitInterval2)
        val filter = IntentFilter("custom-message")

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            filter
        )

        submitInterval.setOnClickListener {
            if (etIntervals.text.isEmpty()) {
                val toast = Toast.makeText(
                    applicationContext, "Najpierw należy wybrać " +
                            "ilość interwałów.", Toast.LENGTH_LONG
                )
                toast.show()
            } else {
                getData()
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