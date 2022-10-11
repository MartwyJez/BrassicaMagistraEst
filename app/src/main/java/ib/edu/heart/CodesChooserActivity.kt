package ib.edu.heart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class CodesChooserActivity : AppCompatActivity() {

    private lateinit var submitCodes: Button
    private lateinit var etCodePatient: EditText
    private lateinit var etCodeSession: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codes_chooser)

        submitCodes = findViewById(R.id.btnSubmitCodes)
        etCodePatient = findViewById(R.id.etCodePatient)
        etCodeSession = findViewById(R.id.etCodeSession)

        val illegalChars = charArrayOf('/',':', '*', '?', '\"','<','>','|')

        submitCodes.setOnClickListener {
            if(etCodePatient.text.isEmpty() || etCodeSession.text.isEmpty()) {
                val toast = Toast.makeText(
                    applicationContext, "Należy wypełnić wszystkie pola", Toast.LENGTH_LONG
                )
                toast.show()
            }else if(etCodePatient.text.toString().any(illegalChars::contains) ||
                etCodeSession.text.toString().any(illegalChars::contains)){
                val toast = Toast.makeText(
                    applicationContext, "Kod sesji lub pacjenta nie powinien zawierać znaków:" +
                            " /:*?\"<>|", Toast.LENGTH_LONG
                )
                toast.show()
            }else{
                val intent = Intent(this, IntervalCountChooserActivity::class.java)
                intent.putExtra("codeSession", etCodePatient.text.toString())
                intent.putExtra("codePatient", etCodeSession.text.toString())
                startActivity(intent)
            }
        }


    }
}