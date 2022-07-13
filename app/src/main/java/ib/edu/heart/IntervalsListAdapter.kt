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
import com.polar.androidblesdk.R


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
                val durationGet = duration.text
                val data: MutableMap<String, Any> = HashMap()
                data[position.toString()] = CustomListElement(
                    customElements.get(position).countId,
                    customElements.get(position).duration
                )
                //println("POZYCJA PRZY ADAPTERZE: $position")
                //println("DURATION: $durationGet")

                //println("W adapterze przy kliknięciu: $data")

                if (counterChars == 0) {
                    counter += 1

                    //println("COUNTEREK: $counter")
                    //println("COUNTER CHARS: $counterChars")
                    if(counter == customElements.size) {
                        allowStart = 1
                    }
                }
                if(counter == customElements.size && !s.isNullOrEmpty()) {
                    allowStart = 1
                }
                counterChars += 1
                //println(s.toString())
                val intent = Intent("custom-message")
                //println("ALLOWSTART $allowStart")
                intent.putExtra("allow", allowStart.toString())

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

