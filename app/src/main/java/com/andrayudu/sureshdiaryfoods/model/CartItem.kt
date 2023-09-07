package com.andrayudu.sureshdiaryfoods.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item_table")
data class CartItem (

    @PrimaryKey
    @ColumnInfo(name = "item_name")
    var Name :String="",

    @ColumnInfo(name = "item_price")
    var Price: String="0",

    @ColumnInfo(name = "quantity")
    var Quantity:String="0",

    @ColumnInfo(name = "item_category")
    var Category: String="",

    @ColumnInfo(name = "order_date")
    var Date: String="",

):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Name)
        parcel.writeString(Price)
        parcel.writeString(Quantity)
        parcel.writeString(Category)
        parcel.writeString(Date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartItem> {
        override fun createFromParcel(parcel: Parcel): CartItem {
            return CartItem(parcel)
        }

        override fun newArray(size: Int): Array<CartItem?> {
            return arrayOfNulls(size)
        }
    }
}
