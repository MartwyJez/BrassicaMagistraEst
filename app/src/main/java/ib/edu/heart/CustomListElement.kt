package ib.edu.heart

import android.text.Editable

class CustomListElement(var countId: Int?, var duration: Editable?) {
    override fun toString(): String {
        return "CustomListElement(countId=$countId, duration=$duration)"
    }
}
