package ib.edu.heart

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.ib.heart.MainActivity


class Data: AppCompatActivity() {


    private lateinit var dataBtn: Button
    private lateinit var resetBtn: Button
    private lateinit var user: TextView
    private lateinit var sensor: TextView


    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.polar.androidblesdk.R.layout.data)

        val tl = findViewById<View>(com.polar.androidblesdk.R.id.table) as TableLayout
        tl.visibility =  View.INVISIBLE
        resetBtn = findViewById(com.polar.androidblesdk.R.id.reset)

        val ok:String = intent.getStringExtra("user").toString()
        val data:String = intent.getStringExtra("sensor").toString()
        val gson = Gson()
        var sensorVal = gson.fromJson(data, Array<Int>::class.java)

        val gson1 = Gson()
        var userVal = gson1.fromJson(ok, Array<Int>::class.java)

        val dataArrayInt = arrayListOf<Int>()

        resetBtn.visibility = View.INVISIBLE




        dataBtn = findViewById(com.polar.androidblesdk.R.id.viewTable)
        user = findViewById(com.polar.androidblesdk.R.id.user)
        sensor = findViewById(com.polar.androidblesdk.R.id.sensor)

        dataBtn.setOnClickListener {

            tl.visibility =  View.VISIBLE


            dataBtn.visibility = View.INVISIBLE

            var str1 = ""
            var str2 = ""

            println(sensorVal.toString())

            for (i in 0..sensorVal.size-1){
                str1 += userVal.get(i).toString() + "\n"
                str2 += sensorVal.get(i).toString() + "\n"
            }

            user.text = str1
            sensor.text = str2

            resetBtn.visibility = View.VISIBLE



        }

        resetBtn.setOnClickListener {

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)


        }





    }

}