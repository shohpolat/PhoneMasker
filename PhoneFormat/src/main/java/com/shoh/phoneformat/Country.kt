package com.shoh.phoneformat

import android.os.Parcel
import android.os.Parcelable

data class Country(
    var countryName: String? = null,
    var phoneMask: String? = null,
    var alpha2code: String? = null,
    var flag: String? = null,
    var prefixNumber: String? = null,
    var alpha3code: String? = null
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(countryName)
        parcel.writeString(phoneMask)
        parcel.writeString(alpha2code)
        parcel.writeString(flag)
        parcel.writeString(prefixNumber)
        parcel.writeString(alpha3code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Country> {
        override fun createFromParcel(parcel: Parcel): Country {
            return Country(parcel)
        }

        override fun newArray(size: Int): Array<Country?> {
            return arrayOfNulls(size)
        }
    }
}