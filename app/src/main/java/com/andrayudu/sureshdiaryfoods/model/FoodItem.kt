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
    var Price: String?=null,

    @ColumnInfo(name = "item_quantity")
    var Quantity: String?=null,

    @ColumnInfo(name = "item_category")
    var Category: String?=null,
    @ColumnInfo(name = "item_preference")
    var Preference: String?="0",


):Comparable<FoodItem> {
    override fun compareTo(other: FoodItem): Int {
        val comparePreference = other.Preference!!.toInt()
        return (Preference!!.toInt() - comparePreference)
    }
}