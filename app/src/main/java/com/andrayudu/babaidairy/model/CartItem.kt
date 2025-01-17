package com.andrayudu.babaidairy.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item_table")
data class CartItem (

    @PrimaryKey
    @ColumnInfo(name = "item_name")
    var Name :String="food",

    @ColumnInfo(name = "item_price")
    var Price: Int=0,

    @ColumnInfo(name = "item_total_price")
    var ItemTotalPrice: Int=0,

    @ColumnInfo(name = "quantity")
    var Quantity:Int=0,

    @ColumnInfo(name = "item_category")
    var Category: String?=null,



):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Name)
        parcel.writeInt(Price)
        parcel.writeInt(ItemTotalPrice)
        parcel.writeInt(Quantity)
        parcel.writeString(Category)
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
