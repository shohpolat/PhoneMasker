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
import com.shoh.myfirstlibrary.phoneFormatter.Country
import com.shoh.phoneformat.databinding.PhoneEditTextBinding

class PhoneFormatter(context: Context, attr: AttributeSet) : ConstraintLayout(context, attr) {

    companion object {
        const val DEFAULT_MASK = "+##############"
        const val MIN_NUMBERS = 11
        const val MAX_NUMBERS = 15
    }

    private val KAZ = Country(
        "Kazakhstan",
        "# ### ### ## ##",
        "KZ",
        "https://restcountries.eu/data/kaz.svg",
        "7",
        "KAZ"
    )

    private val RUS = Country(
        "Russian Federation",
        "# ### ### ## ##",
        "RU",
        "https://restcountries.eu/data/rus.svg",
        "7",
        "RUS"
    )
    private var currentCountry: String? = null

    private var listener: IsMaskFilledListener? = null
    private var mMask: String? = null
    private var hasFlag = false
    private var textColor: Int? = null
    private var textSize: Float? = null
    private var background: Int? = null
    private var currentMask = ""
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

            requestBuilder.load(url).placeholder(R.drawable.ic_globe).centerCrop()
                .into(binding.flag)
        }
    }

    fun setMask(mask: String?, prefix: String? = null) {
        if (mask != null && mask.isNotEmpty()) {
            mMask = mask
            addListener(prefix)
        } else {
            cleanUpMask()
        }
    }

    fun getRawText(): String {
        val editable = binding.editText.text
        return editable!!.replace(Regex("[-\\s+]"), "")
    }

    private fun addListener(prefix: String?) {
        mMask?.let {
            if (mMask != currentMask) {
                currentMask = mMask!!
                val listener = MaskChangedListener(Mask(mMask!!))
                binding.editText.addTextChangedListener(listener)
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
                println("applying mask = ${mMask}, country = ${currentCountry}")
            }
        }
    }

    fun setFlag(flag: Int) {
        binding.flag.setImageResource(flag)
    }

    fun cleanUpMask() {
        println("cleaning mask")
        mMask = DEFAULT_MASK
        addListener(null)
    }

    fun setList(list: List<Country>?) {
        list?.let {
            this.list = list
        }
    }

    fun setPhoneWithCode(alpha3code: String, number: String? = null) {

        val country = findCountryByAlphaCode(alpha3code)
        if (country != null) {
            currentCountry = country.alpha3code
            setFlag(country.flag)
            setMask(country.phoneMask, number)
        }

    }

    private fun findCountryByAlphaCode(code: String): Country? {
        list?.forEach {
            if (code == it.alpha3code) {
                return it
            }
        }
        return null
    }

    fun isNumberFilled() = isFilled

    fun setOnFlagClickListener(listener: OnClickListener) {

        binding.flagContainer.setOnClickListener(listener)

    }

    fun setOnMaskFilledListener(listener: IsMaskFilledListener) {
        this.listener = listener
    }

    fun initializeTextWatcher() {
        binding.editText.doOnTextChanged { text, start, before, count ->

            if (text!!.isEmpty()) {
                cleanUpMask()
                setFlag(R.drawable.ic_globe)
            } else if (text.length <= 6) {
                val t = checkCode("$text")
                if (t != null) {
                    setFlag(t.flag)
                    setMask("+${t.phoneMask!!}")
                }
            }

            if (mMask == DEFAULT_MASK) {

                if (text.length in MIN_NUMBERS..MAX_NUMBERS) {
                    listener?.onFilled(true)
                    isFilled = true
                } else {
                    listener?.onFilled(false)
                    isFilled = false
                }

            } else {
                val maskRawLength = mMask?.replace(Regex("[+\\s]"), "")
                val textRawLength = text!!.replace(Regex("[+\\s]"), "")
                if (maskRawLength?.length == textRawLength.length) {
                    listener?.onFilled(true)
                    isFilled = true
                } else {
                    listener?.onFilled(false)
                    isFilled = false
                }
            }
        }
    }

    private fun checkCode(code: String): Country? {
        val t = code.replace(Regex("[+\\s]"), "")
        println(t)
        if (t == "7") {
            currentCountry = RUS.alpha3code
            return RUS
        } else if (t == "76" || t == "77") {
            currentCountry = KAZ.alpha3code
            return KAZ
        } else if (t.length >= 2 && t[0].toString() == "7" && (t[1].toString() != "6" && t[1].toString() != "7")) {
            currentCountry = RUS.alpha3code
            return RUS
        } else {
            list?.forEach {
                if (it.prefixNumber == t) {
                    currentCountry = it.alpha3code
                    return it
                }
            }
        }

        return null
    }

    interface IsMaskFilledListener {
        fun onFilled(isFilled: Boolean)
    }

}