package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.elementary.tasks.R
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.os.data.ContactData
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ViewContactPickerBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ContactPickerView : LinearLayout, TextWatcher, KoinComponent {

  private val contactsReader by inject<ContactsReader>()

  private var mImm: InputMethodManager? = null
  private lateinit var binding: ViewContactPickerBinding
  private var contactInfo: ContactInfo? = null

  var contactPicker: ContactPicker? = null
  var listener: OnNumberChangeListener? = null
  var number: String
    get() = binding.numberView.trimmedText()
    set(number) {
      binding.numberView.setText(number)
      prepareContactInfo(number)
      updateContactView()
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_contact_picker, this)
    orientation = VERTICAL
    binding = ViewContactPickerBinding.bind(this)

    binding.numberView.isFocusableInTouchMode = true
    binding.numberView.setOnFocusChangeListener { _, hasFocus ->
      mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      if (!hasFocus) {
        mImm?.hideSoftInputFromWindow(binding.numberView.windowToken, 0)
      } else {
        mImm?.showSoftInput(binding.numberView, 0)
      }
    }
    binding.numberView.setOnClickListener {
      mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      if (mImm?.isActive(binding.numberView) == false) {
        mImm?.showSoftInput(binding.numberView, 0)
      }
    }
    binding.numberView.addTextChangedListener(this)

    binding.selectNumber.setOnClickListener {
      contactPicker?.pickContact {
        number = it.phone
        contactInfo = createContactInfo(it)
        listener?.onChanged(it.phone, contactInfo)
        updateContactView()
      }
    }
  }

  private fun updateContactView() {
    val contactInfo = contactInfo
    if (contactInfo == null) {
      binding.contactInfo.gone()
    } else {
      contactInfo.photo?.run {
        binding.contactPhotoView.setImageBitmap(this)
        binding.contactPhotoView.visible()
        binding.contactIconView.gone()
      } ?: run {
        binding.contactPhotoView.gone()
        binding.contactIconView.visible()
      }
      binding.contactNameView.text = contactInfo.name
      binding.contactInfo.visible()
    }
  }

  private fun createContactInfo(contactData: ContactData): ContactInfo {
    val id = contactsReader.getIdFromNumber(contactData.phone)
    val photo = contactsReader.getPhotoBitmap(id)
    return ContactInfo(contactData.phone, contactData.name, photo)
  }

  private fun prepareContactInfo(number: String) {
    val id = contactsReader.getIdFromNumber(number)
    if (id == 0L) return
    val photo = contactsReader.getPhotoBitmap(id)
    val name = contactsReader.getNameFromNumber(number)
    contactInfo = ContactInfo(number, name, photo)
  }

  fun showError(@StringRes messageRes: Int) {
    binding.numberLayout.error = context.getString(messageRes)
    binding.numberLayout.isErrorEnabled = true
  }

  override fun afterTextChanged(s: Editable?) {
    val number = s?.toString()
    if (number.isNullOrEmpty()) {
      contactInfo = null
      updateContactView()
      return
    }
    if (number != contactInfo?.number) {
      contactInfo = null
    }
    prepareContactInfo(number)
    listener?.onChanged(number, contactInfo)
    updateContactView()
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    binding.numberLayout.isErrorEnabled = false
  }

  interface OnNumberChangeListener {
    fun onChanged(phoneNumber: String, contactInfo: ContactInfo?)
  }

  data class ContactInfo(
    val number: String,
    val name: String?,
    val photo: Bitmap?
  )
}
