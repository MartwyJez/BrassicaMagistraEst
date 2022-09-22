package ib.edu.heart

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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

class ChangeIDActivity: AppCompatActivity() {

    private lateinit var submit: Button
    private lateinit var etID: EditText
    private lateinit var databaseHelper: DataBaseSensor

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_sensor)

        databaseHelper = DataBaseSensor(this)

        submit = findViewById(R.id.btnSubmit)

        etID = findViewById(R.id.etSensorID)



        submit.setOnClickListener {
            if (etID.text.isEmpty()) {
                val toast = Toast.makeText(
                    applicationContext, "Najpierw wpisać ID czujnika", Toast.LENGTH_LONG
                )
                toast.show()
            }

            else{
                val insert: Boolean = databaseHelper!!.insert(etID.text.toString())

                if(insert){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    val toast = Toast.makeText(
                        applicationContext, "ID czujnika zapisano pomyślnie", Toast.LENGTH_LONG
                    )
                    toast.show()
                }
                else{
                    val toast = Toast.makeText(
                        applicationContext, "Nie udało się zapisać ID czujnika", Toast.LENGTH_LONG
                    )
                    toast.show()
                }


            }

        }


    }

}