package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Typeface
import android.location.Address
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.elementary.tasks.core.async.GeocoderTask
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

    private var mTypeface: Typeface? = null
    private var foundPlaces: List<Address>? = null

    private var task: GeocoderTask? = null
    private var mAdapter: AddressAdapter? = null
    private var isEnabledInner = true

    private val mExecutionCallback = object : GeocoderTask.GeocoderListener {
        override fun onAddressReceived(addresses: List<Address>) {
            Timber.d("onAddressReceived: $addresses")
            foundPlaces = addresses
            mAdapter = AddressAdapter(context, android.R.layout.simple_list_item_2, addresses)
            setAdapter<AddressAdapter>(mAdapter)
            mAdapter?.notifyDataSetChanged()
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
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
                performTypeValue(text.toString().trim { it <= ' ' })
                return@setOnEditorActionListener true
            }
            false
        }
    }

    fun getAddress(position: Int): Address {
        return foundPlaces!![position]
    }

    private fun performTypeValue(s: String) {
        if (task != null && task?.isCancelled == false) {
            task?.cancel(true)
        }
        task = GeocoderTask(context, mExecutionCallback)
        task?.execute(s)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTypeface != null) {
            typeface = mTypeface
        }
    }

    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener?) {
        super.setOnItemClickListener { adapterView, view, i, l1 ->
            if (mAdapter != null) {
                isEnabledInner = false
                setText(mAdapter!!.getName(i))
                isEnabledInner = true
            }
            l?.onItemClick(adapterView, view, i, l1)
        }
    }

    private inner class AddressAdapter internal constructor(context: Context, resource: Int, objects: List<Address>) : ArrayAdapter<Address>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = convertView
            if (v == null) {
                v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null, false)
            }
            val tv1 = v!!.findViewById<TextView>(android.R.id.text1)
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
            return formName(getItem(position)!!)
        }

        private fun formName(address: Address): String {
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
