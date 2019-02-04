package com.elementary.tasks.core.views

import android.content.Context
import android.location.Address
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.elementary.tasks.core.utils.GeocoderTask
import timber.log.Timber

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class AddressAutoCompleteView : AppCompatAutoCompleteTextView {

    private var mImm: InputMethodManager? = null
    private var foundPlaces: MutableList<Address> = mutableListOf()
    private var mAdapter: AddressAdapter? = null
    private var isEnabledInner = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mImm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isEnabledInner) performTypeValue(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        setSingleLine(true)
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_SEARCH) {
                performTypeValue(text.toString().trim())
                hideKb()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun hideKb() {
        if (mImm?.isActive(this) == true) {
            mImm?.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }

    fun getAddress(position: Int): Address? {
        return if (position < foundPlaces.size) {
            foundPlaces[position]
        } else null
    }

    private fun performTypeValue(s: String) {
        GeocoderTask.findAddresses(context, s) {
            Timber.d("onAddressReceived: $it")
            foundPlaces.clear()
            foundPlaces.addAll(it)
            mAdapter = AddressAdapter(context, android.R.layout.simple_list_item_2, it)
            setAdapter<AddressAdapter>(mAdapter)
            mAdapter?.notifyDataSetChanged()
        }
    }

    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener?) {
        super.setOnItemClickListener { adapterView, view, i, l1 ->
            if (mAdapter != null) {
                isEnabledInner = false
                setText(mAdapter?.getName(i) ?: "")
                isEnabledInner = true
            }
            l?.onItemClick(adapterView, view, i, l1)
            hideKb()
        }
    }

    private inner class AddressAdapter internal constructor(context: Context, resource: Int, objects: List<Address>) : ArrayAdapter<Address>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v: View = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null, false)
            val tv1 = v.findViewById<TextView>(android.R.id.text1)
            val tv2 = v.findViewById<TextView>(android.R.id.text2)
            val address = getItem(position) ?: return v
            if (address.getAddressLine(0) != null) {
                tv1.text = address.getAddressLine(0)
                tv2.text = formName(address)
            } else {
                tv1.text = formName(address)
                tv2.text = ""
            }
            return v
        }

        fun getName(position: Int): String {
            val name = getItem(position)?.let {
                formName(it)
            }
            return name ?: ""
        }
    }

    companion object {
        fun formName(address: Address): String {
            val sb = StringBuilder()
            sb.append(address.featureName)
            if (address.adminArea != null) {
                sb.append(", ").append(address.adminArea)
            }
            if (address.countryName != null) {
                sb.append(", ").append(address.countryName)
            }
            return sb.toString()
        }
    }
}
