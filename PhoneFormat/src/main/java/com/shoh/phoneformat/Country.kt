package com.shoh.phoneformat

import android.os.Parcel
import android.os.Parcelable
import androidx.versionedparcelable.ParcelField
import com.google.android.material.internal.ParcelableSparseArray
import java.io.Serializable

data class Country(
    var countryName: String? = null,
    var phoneMask: String? = null,
    var alpha2code: String? = null,
    var flag: String? = null,
    var prefixNumber: ArrayList<String>? = null,
    var alpha3code: String? = null
):Serializable