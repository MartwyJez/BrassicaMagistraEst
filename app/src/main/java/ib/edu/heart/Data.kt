package ib.edu.heart

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class Data: AppCompatActivity() {


    private lateinit var dataBtn: Button
    private lateinit var user: TextView
    private lateinit var sensor: TextView


    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.polar.androidblesdk.R.layout.data)

        val tl = findViewById<View>(com.polar.androidblesdk.R.id.table) as TableLayout
        tl.visibility =  View.INVISIBLE

        val bundle: Bundle? = intent.extras
        val userRec = bundle?.get("user")
        val sensorRec = bundle?.get("sensor")

        val uvals = ArrayList<Float>()
        var udata = userRec.toString().replace("[", "").replace("]","")

        var indexes = 0
        var indexes2 = 0

        while(indexes2 < udata.length){
            indexes2 = udata.indexOf(",")
            if(indexes2 >= 0){
                val wartosc : String = udata.subSequence(indexes, indexes2) as String
                uvals.add(wartosc.toFloat())
                udata = udata.substring(0, indexes2).plus("").plus(udata.substring(indexes2 + 1))
                indexes = indexes2
            }
            else{
                val wartosc : String = udata.subSequence(indexes, udata.length) as String
                uvals.add(wartosc.toFloat())
                break
            }
        }

        val svals = ArrayList<Float>()
        var sdata = userRec.toString().replace("[", "").replace("]","")

        var indexes3 = 0
        var indexes4 = 0

        while(indexes3 < sdata.length){
            indexes4 = sdata.indexOf(",")
            if(indexes4 >= 0){
                val wartosc : String = sdata.subSequence(indexes3, indexes4) as String
                svals.add(wartosc.toFloat())
                sdata = sdata.substring(0, indexes4).plus("").plus(sdata.substring(indexes4 + 1))
                indexes3 = indexes4
            }
            else{
                val wartosc : String = sdata.subSequence(indexes3, sdata.length) as String
                svals.add(wartosc.toFloat())
                break
            }
        }

        dataBtn = findViewById(com.polar.androidblesdk.R.id.viewTable)
        user = findViewById(com.polar.androidblesdk.R.id.user)
        sensor = findViewById(com.polar.androidblesdk.R.id.sensor)

        dataBtn.setOnClickListener {

            tl.visibility =  View.VISIBLE


            dataBtn.visibility = View.INVISIBLE

            var str1 = ""
            var str2 = ""



            for (i in 0..svals.size) {
                str1 += udata.get(i).toString() + "\n"
                str2 += sdata.get(i).toString() + "\n"
            }

            user.text = str1
            sensor.text = str2



        }



    }

}