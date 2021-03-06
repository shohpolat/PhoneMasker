package com.shoh.phoneformat

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.PictureDrawable
import android.text.Selection
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.RequestBuilder
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.santalu.maskara.Mask
import com.santalu.maskara.MaskChangedListener
import com.shoh.phoneformat.databinding.PhoneEditTextBinding

class PhoneFormatter(context: Context, attr: AttributeSet) : ConstraintLayout(context, attr) {

    companion object {
        const val DEFAULT_MASK = "+##############"
        const val DEFAULT_COUNTRY = "default_country"
        const val MIN_NUMBERS = 11
        const val MAX_NUMBERS = 15
    }

    private val US = Country(
        "United States of America",
        "# ### ### ####",
        "US",
        "https://restcountries.eu/data/usa.svg",
        arrayListOf("1"),
        "USA"
    )

    private val CAN = Country(
        "Canada",
        "# ### ### ####",
        "CA",
        "https://restcountries.eu/data/can.svg",
        arrayListOf("1"),
        "CAN"
    )

    private var currentCountry: String? = null
    private var mCountry: String? = null

    private var maskFilledListener: IsMaskFilledListener? = null
    private var countryChangedListener: OnCountryChangedListener? = null
    private var mMask: String? = null
    private var hasFlag = false
    private var textColor: Int? = null
    private var textSize: Float? = null
    private var background: Int? = null
    private var list: List<Country>? = null
    private var isFilled: Boolean = false

    private var placeholderImage: Int? = null
    private var hintText: String? = null
    private var hintColor: Int? = null

    var binding: PhoneEditTextBinding =
        PhoneEditTextBinding.bind(View.inflate(context, R.layout.phone_edit_text, this))

    init {

        val attribute = context.obtainStyledAttributes(attr, R.styleable.PhoneFormatter)

        if (attribute.hasValue(R.styleable.PhoneFormatter_hasFlag)) {
            hasFlag = attribute.getBoolean(R.styleable.PhoneFormatter_hasFlag, false)
        }
        if (attribute.hasValue(R.styleable.PhoneFormatter_text_color)) {
            textColor = attribute.getColor(R.styleable.PhoneFormatter_text_color, Color.BLACK)
        }
        if (attribute.hasValue(R.styleable.PhoneFormatter_text_size)) {
            textSize = attribute.getFloat(R.styleable.PhoneFormatter_text_size, 14f)
        }
        if (attribute.hasValue(R.styleable.PhoneFormatter_background_resource)) {
            background = attribute.getResourceId(R.styleable.PhoneFormatter_background_resource, 0)
        }
        if (attribute.hasValue(R.styleable.PhoneFormatter_placeholder_image)) {
            placeholderImage =
                attribute.getResourceId(R.styleable.PhoneFormatter_placeholder_image, 0)
        }
        if (attribute.hasValue(R.styleable.PhoneFormatter_hint_text)) {
            hintText = attribute.getString(R.styleable.PhoneFormatter_hint_text)
        }
        if (attribute.hasValue(R.styleable.PhoneFormatter_hint_color)) {
            hintColor = attribute.getColor(R.styleable.PhoneFormatter_hint_color, Color.GRAY)
        }
        attribute.recycle()

        if (hasFlag) {
            binding.flagContainer.visibility = View.VISIBLE
        } else {
            binding.flagContainer.visibility = View.GONE
        }

        if (textColor != null) {
            binding.editText.setTextColor(textColor!!)
        }
        if (textSize != null) {
            binding.editText.textSize = textSize as Float
        }

        if (background != null && background != 0) {
            binding.inputBackground.setBackgroundResource(background!!)
        }

        if (placeholderImage != null) {
            binding.flag.setImageResource(placeholderImage!!)
        }

        if (hintText != null) {
            binding.editText.hint = hintText
        }

        if (hintColor != null) {
            binding.editText.setHintTextColor(hintColor!!)
        }

        initializeTextWatcher()

    }

    fun setFlag(url: String?) {
        if (url != null) {
            val requestBuilder: RequestBuilder<PictureDrawable> =
                GlideToVectorYou.init().with(context).requestBuilder

            requestBuilder.load(url).placeholder(R.drawable.ic_globe)
                .into(binding.flag)

        }
    }

    private fun setMask(country: Country, prefix: String? = null) {
        mCountry = country.alpha3code
        setFlag(country.flag)
        mMask = "+${country.phoneMask}"
        addListener(prefix)
    }

    fun getRawText(text: String? = null): String {
        return if (text == null) {
            val editable = binding.editText.text
            editable!!.replace(Regex("[-\\s+]"), "")
        } else {
            text.replace(Regex("[-\\s+]"), "")
        }

    }

    private fun addListener(prefix: String?) {
        mMask?.let {
            when (mCountry) {
                DEFAULT_COUNTRY -> {
                    currentCountry = null
                    applyMask(prefix)
                }
                currentCountry -> {
                }
                else -> {
                    currentCountry = mCountry
                    applyMask(prefix)
                }
            }
        }
    }

    private fun applyMask(prefix: String?) {
        val listener = MaskChangedListener(Mask(mMask!!))
        binding.editText.addTextChangedListener(listener)
        println("applying mask = ${mMask}, country = ${mCountry}")
        if (prefix != null) {
            binding.editText.setText(prefix)
            Selection.setSelection(
                binding.editText.text,
                binding.editText.text!!.length
            )
        } else {
            if (binding.editText.text!!.isNotEmpty()) {
                binding.editText.setText(binding.editText.text!!.replace(Regex(" "), ""))
                Selection.setSelection(
                    binding.editText.text,
                    binding.editText.text!!.length
                )
            }
        }
    }

    fun setFlag(flag: Int? = null) {
        flag?.let {
            binding.flag.setImageResource(flag)
        }
    }

    fun cleanUpMask(number: String? = null) {
        println("cleaning mask")
        setFlag(R.drawable.ic_globe)
        mMask = DEFAULT_MASK
        mCountry = DEFAULT_COUNTRY
        countryChangedListener?.onChanged(null)
        addListener(number)
    }

    fun setList(list: List<Country>?) {
        list?.let {
            this.list = list
        }
    }

    fun setPhoneWithCode(alpha3code: String? = null, number: String? = null) {

        if (alpha3code != null) {
            val country = findCountryByAlphaCode(alpha3code)
            if (country != null && number != null) {
                if (country.prefixNumber?.firstOrNull { prefix ->
                        number.replace(
                            Regex("[+\\s]"),
                            ""
                        ).startsWith(prefix)
                    } != null) {
                    setMask(country, number)
                } else {
                    val countryByNumber = findCountryByNumber(number)
                    if (countryByNumber != null) {
                        setMask(countryByNumber, number)
                    }
                }
            } else if (country != null && number == null) {
                setMask(country, country.prefixNumber?.get(0))
            } else {
                cleanUpMask(number)
            }
        } else if (number != null) {
            val countryByNumber = findCountryByNumber(number)
            if (countryByNumber != null) {
                setMask(countryByNumber, number)
            }
        } else {
            cleanUpMask(number)
        }
    }

    private fun findCountryByNumber(number: String?): Country? {
        val first_4_digits = number?.subSequence(0, 4)
        var lastFound: Country? = null
        var prefix = ""
        println(first_4_digits)
        first_4_digits?.forEach { digit ->
            prefix += digit
            val temp = searchForPrefix(prefix)
            if (temp != null) {
                lastFound = temp
            }
        }
        return lastFound
    }

    private fun searchForPrefix(t: String): Country? {
        if (t == "1") {
            return US
        }
        return list?.firstOrNull { country ->
            country.prefixNumber?.firstOrNull { prefix ->
                prefix == t
            } != null
        }
    }

    private fun findCountryByAlphaCode(code: String): Country? {
        return list?.firstOrNull { code == it.alpha3code }
    }

    fun isNumberFilled() = isFilled

    fun setOnFlagClickListener(listener: OnClickListener) {
        binding.flagContainer.setOnClickListener(listener)
    }

    fun setOnMaskFilledListener(listener: IsMaskFilledListener) {
        this.maskFilledListener = listener
    }

    fun initializeTextWatcher() {
        binding.editText.doOnTextChanged { text, start, before, count ->

            maskFilledListener?.onTextChanged(getRawText(text.toString()))

            if (text!!.isEmpty()) {
                cleanUpMask()
            } else if (text.length <= 6) {
                val t = checkCode("$text")
                if (t != null && mCountry != t.alpha3code) {
                    setFlag(t.flag)
                    mCountry = t.alpha3code
                    setMask(t)
                    countryChangedListener?.onChanged(t)
                }

            }

            if (mMask == DEFAULT_MASK) {

                if (text.length in MIN_NUMBERS..MAX_NUMBERS) {
                    maskFilledListener?.onFilled(true)
                    isFilled = true
                } else {
                    maskFilledListener?.onFilled(false)
                    isFilled = false
                }

            } else {
                val maskRawLength = mMask?.replace(Regex("[+\\s]"), "")
                val textRawLength = text!!.replace(Regex("[+\\s]"), "")
                if (maskRawLength?.length == textRawLength.length) {
                    maskFilledListener?.onFilled(true)
                    isFilled = true
                } else {
                    maskFilledListener?.onFilled(false)
                    isFilled = false
                }
            }
        }
    }

    private fun checkCode(code: String): Country? {
        val t = code.replace(Regex("[+\\s]"), "")
        println(t)
        return if (t == "1") {
            if (mCountry != CAN.alpha3code) {
                US
            } else {
                CAN
            }
        } else {
            list?.firstOrNull { country -> country.prefixNumber?.firstOrNull { prefix -> prefix == t } != null }
        }
    }

    interface OnCountryChangedListener {
        fun onChanged(country: Country?)
    }

    fun setOnCountryChangedListener(onCountryChangedListener: OnCountryChangedListener) {
        this.countryChangedListener = onCountryChangedListener
    }

    interface IsMaskFilledListener {
        fun onFilled(isFilled: Boolean)
        fun onTextChanged(text: String)
    }


}