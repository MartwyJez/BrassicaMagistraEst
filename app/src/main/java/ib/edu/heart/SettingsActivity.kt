package ib.edu.heart

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.polar.androidblesdk.R
import edu.ib.heart.MainActivity


class SettingsActivity : AppCompatActivity() {

    private lateinit var submitInterval: Button
    private lateinit var show: Button
    private lateinit var submitIntervalWhole: Button
    private var intervalsListAdapter: IntervalsListAdapter? = null
    private lateinit var etIntervals: EditText
    private lateinit var databaseHelper: DatabaseHelper
    var isAllow = 0.toString()

    var dataArray = arrayListOf<CustomListElement>()

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
        setContentView(R.layout.settings)

        databaseHelper = DatabaseHelper(this)

        show = findViewById(R.id.btnShow)

        etIntervals = findViewById(R.id.etCodePatient)
        setInputTypeNumber()
        listview = findViewById<ListView>(R.id.listIntervals)
        submitInterval = findViewById<Button>(R.id.btnSubmitInterval)
        submitIntervalWhole = findViewById<Button>(R.id.btnSubmitCodes)
        val filter = IntentFilter("custom-message")

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            filter
        )


        show.setOnClickListener {

            val data = databaseHelper.lastRow()

            val gson = Gson()
            var values = gson.fromJson(data, Array<CustomListElement>::class.java)

            var str1 = ""

            for (i in 0..values.size-1){
                str1 += (i+1).toString() + ". " + values.get(i).duration.toString() + " sek \n"
            }



            AlertDialog.Builder(this).setTitle("Zapisane długości interwałów").
            setMessage(str1).show()
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

                val dataJson = ArrayListObjectParser.toJson(dataArray)

                val insert: Boolean = databaseHelper!!.insert(dataJson.toString())

                if(insert){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                else{
                    val toast = Toast.makeText(
                        applicationContext, "Nie udało się zapisać ustawień", Toast.LENGTH_LONG
                    )
                    toast.show()
                }
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