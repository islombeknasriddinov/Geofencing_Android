package com.example.geofencing.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Marker(
    var markerId: String,
    var enter: String? = null,
    var dwell: String? = null,
    var exit: String? = null,
    var latLng: LatLng? = null,
    var radius: Float? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(
            LatLng::
            class.java.classLoader
        ),
        parcel.readValue(
            Float::
            class.java.classLoader
        ) as? Float
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(markerId)
        parcel.writeString(enter)
        parcel.writeString(dwell)
        parcel.writeString(exit)
        parcel.writeParcelable(latLng, flags)
        parcel.writeValue(radius)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Marker> {
        override fun createFromParcel(parcel: Parcel): Marker {
            return Marker(parcel)
        }

        override fun newArray(size: Int): Array<Marker?> {
            return arrayOfNulls(size)
        }
    }
}