package ib.edu.heart

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.polar.androidblesdk.R
import java.lang.reflect.Modifier


class IntervalsListAdapter(
    customElements: ArrayList<CustomListElement>,
    context: Context,

    ) : BaseAdapter(), ListAdapter {
    private val context: Context
    private val customElements: ArrayList<CustomListElement>
    private var counter = 0
    private var allowStart = 0


    override fun getCount(): Int {
        return customElements.size
    }

    override fun getItem(pos: Int): Any {
        return customElements[pos]
    }

    override fun getItemId(pos: Int): Long {
        return 0 //list.get(pos).getId();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        var view = convertView
        if (view == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.custom_listview, null)

        }
        val lp = view?.findViewById<TextView>(R.id.count)
        val duration = view?.findViewById<EditText>(R.id.etDuration)
        lp?.text = customElements[position].countId.toString()
        duration?.inputType = InputType.TYPE_CLASS_NUMBER
        var counterChars = 0
        duration?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                customElements.set(position, CustomListElement(
                    customElements.get(position).countId,
                    Integer.parseInt(duration.text.toString())
                ))


                println("W adapterze przy kliknięciu: $customElements")

                if (counterChars == 0) {
                    counter += 1

                    if(counter == customElements.size) {
                        allowStart = 1
                    }
                }
                if(counter == customElements.size && !s.isNullOrEmpty()) {
                    allowStart = 1
                }
                counterChars += 1

                val intent = Intent("custom-message")

                intent.putExtra("allow", allowStart.toString())

                val gson = GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                    .serializeNulls()
                    .create()
                val json = gson.toJson(customElements.get(position))
                intent.putExtra("elementsData", json)

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //println(s.toString())
                if(s.isNullOrEmpty()) {
                    allowStart = 0
                }
            }

        })
        return view
    }

    init {
        this.customElements = customElements
        this.context = context
    }
}

