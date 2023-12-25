package com.andrayudu.sureshdiaryfoods.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_orders_table")
data class FoodItem (

    @PrimaryKey
    @ColumnInfo(name = "fooditem_name")
    var Name: String="food",

    @ColumnInfo(name = "item_price")
    var Price: Int=0,

    @ColumnInfo(name = "item_quantity")
    var Quantity: Int=0,

    @ColumnInfo(name = "item_category")
    var Category: String?=null,
    @ColumnInfo(name = "item_preference")
    var Preference: Int=0,
    @ColumnInfo(name = "item_outofstock")
    var isOutOfStock: Boolean=false,

    @ColumnInfo(name = "item_imagelink")
    var imageLink: String?=null,




):Comparable<FoodItem>,Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }

    //the below method sorts the foodItems based on their preference
    override fun compareTo(other: FoodItem): Int {
        val comparePreference = other.Preference
        return (Preference - comparePreference)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Name)
        parcel.writeInt(Price)
        parcel.writeInt(Quantity)
        parcel.writeString(Category)
        parcel.writeInt(Preference)
        parcel.writeByte(if (isOutOfStock) 1 else 0)
        parcel.writeString(imageLink)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FoodItem> {
        override fun createFromParcel(parcel: Parcel): FoodItem {
            return FoodItem(parcel)
        }

        override fun newArray(size: Int): Array<FoodItem?> {
            return arrayOfNulls(size)
        }
    }

}
