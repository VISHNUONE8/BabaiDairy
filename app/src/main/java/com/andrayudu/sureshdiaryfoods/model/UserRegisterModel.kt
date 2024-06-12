package com.andrayudu.sureshdiaryfoods.model

import android.os.Parcel
import android.os.Parcelable

data class UserRegisterModel (var Name:String?=null,
                              var Email:String?=null,
                              var Limit:Int=0,
                              var Outstanding:Int=0,
                              var Mobile:String?=null,
                              var TransportCharges:Int=0,
                              var userId:String?=null,
                              var deviceToken:String?=null,
                              var role:String?=null,
                              //whenever the outstanding is updated,this val will also be updated...
                              var updatedAt:String?=null,
                              var isExpanded:Boolean = false,
                              var onHold:Boolean = false,
                              var address:String?=null


):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readBoolean(),
        parcel.readBoolean()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Name)
        parcel.writeString(Email)
        parcel.writeInt(Limit)
        parcel.writeInt(Outstanding)
        parcel.writeString(Mobile)
        parcel.writeInt(TransportCharges)
        parcel.writeString(userId)
        parcel.writeString(deviceToken)
        parcel.writeString(role)
        parcel.writeString(updatedAt)
        parcel.writeBoolean(isExpanded)
        parcel.writeBoolean(onHold)
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
