package com.andrayudu.babaidairy.model

import android.os.Parcel
import android.os.Parcelable

//this is the model for ItemsCatalogue and specialpricemodel for customers...
data class ItemsCatalogueModel (
    var itemsList:List<FoodItem>?=null,
    var updatedAt:String?=null,
    var customerName:String?=null

    ):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(FoodItem),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(itemsList)
        parcel.writeString(updatedAt)
        parcel.writeString(customerName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemsCatalogueModel> {
        override fun createFromParcel(parcel: Parcel): ItemsCatalogueModel {
            return ItemsCatalogueModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemsCatalogueModel?> {
            return arrayOfNulls(size)
        }
    }

}