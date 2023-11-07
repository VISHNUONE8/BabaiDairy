package com.andrayudu.sureshdiaryfoods.model

import android.os.Parcel
import android.os.Parcelable


data class OrderModel(
    var userId:String?=null,
    var userName:String?=null,
    var orderId:String?=null,
    //orderValue means just the price of order without Transport
    var orderValue:Int=0,
    var quantity:Int=0,
    var date:String?=null,
    //transportCost means the order transport price...
    var orderTransportCost:Int=0,
    //transportCharges means the users transport charge
    var transportCharges:Int=0,
    var grandTotal:Int=0,
    var dispatchedGrandTotal:Int=0,
    var orderStatus:Int = 0,
    var transportCompany:String?=null,
    var transportLrNo:String?=null,
    var cartItemList:List<CartItem>?=null
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(CartItem)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(orderId)
        parcel.writeInt(orderValue)
        parcel.writeInt(quantity)
        parcel.writeString(date)
        parcel.writeInt(orderTransportCost)
        parcel.writeInt(transportCharges)
        parcel.writeInt(grandTotal)
        parcel.writeInt(dispatchedGrandTotal)
        parcel.writeInt(orderStatus)
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