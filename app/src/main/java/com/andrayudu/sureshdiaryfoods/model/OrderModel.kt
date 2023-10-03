package com.andrayudu.sureshdiaryfoods.model

import android.os.Parcel
import android.os.Parcelable


data class OrderModel(
    var userId:String?=null,
    var userName:String?=null,
    var orderId:String?=null,
    var quantity:String?=null,
    var date:String?=null,
    var orderValue:String?=null,
    var transportCharges:String?=null,
    var grandTotal:String?=null,
    var orderStatus:String?=null,
    var transportCompany:String?=null,
    var transportLrNo:String?=null,
    var cartItemList:List<CartItem>?=null
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
        parcel.readString(),
        parcel.createTypedArrayList(CartItem)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(orderId)
        parcel.writeString(quantity)
        parcel.writeString(date)
        parcel.writeString(orderValue)
        parcel.writeString(transportCharges)
        parcel.writeString(grandTotal)
        parcel.writeString(orderStatus)
        parcel.writeString(transportCompany)
        parcel.writeString(transportLrNo)
        parcel.writeTypedList(cartItemList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderModel> {
        override fun createFromParcel(parcel: Parcel): OrderModel {
            return OrderModel(parcel)
        }

        override fun newArray(size: Int): Array<OrderModel?> {
            return arrayOfNulls(size)
        }
    }
}