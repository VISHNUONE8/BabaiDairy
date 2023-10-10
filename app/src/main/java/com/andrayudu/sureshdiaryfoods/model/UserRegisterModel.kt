package com.andrayudu.sureshdiaryfoods.model

import android.os.Parcel
import android.os.Parcelable

data class UserRegisterModel (var Name:String?=null,
                              var Email:String?=null,
                              var Limit:String?=null,
                              var Outstanding:String?=null,
                              var Mobile:String?=null,
                              var TransportCharges:String?=null,
                              var userId:String?=null,
                              var deviceToken:String?=null,
                              var role:String?=null,
                              var updatedAt:String?=null,
                              var isExpanded:Boolean = false


):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readBoolean()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Name)
        parcel.writeString(Email)
        parcel.writeString(Limit)
        parcel.writeString(Outstanding)
        parcel.writeString(Mobile)
        parcel.writeString(TransportCharges)
        parcel.writeString(userId)
        parcel.writeString(deviceToken)
        parcel.writeString(role)
        parcel.writeString(updatedAt)
        parcel.writeBoolean(isExpanded)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserRegisterModel> {
        override fun createFromParcel(parcel: Parcel): UserRegisterModel {
            return UserRegisterModel(parcel)
        }

        override fun newArray(size: Int): Array<UserRegisterModel?> {
            return arrayOfNulls(size)
        }
    }
}
