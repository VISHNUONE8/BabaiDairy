package com.andrayudu.sureshdiaryfoods.model

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


):Comparable<FoodItem> {
    //the below method sorts the foodItems based on their preference
    override fun compareTo(other: FoodItem): Int {
        val comparePreference = other.Preference
        return (Preference - comparePreference)
    }

}
