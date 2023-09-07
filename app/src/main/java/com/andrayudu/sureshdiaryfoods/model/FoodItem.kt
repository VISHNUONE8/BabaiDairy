package com.andrayudu.sureshdiaryfoods.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_orders_table")
data class FoodItem (

    @PrimaryKey
    @ColumnInfo(name = "fooditem_name")
    var Name: String="",


    @ColumnInfo(name = "item_price")
    var Price: String="",

    @ColumnInfo(name = "item_quantity")
    var Quantity: String="0",

    @ColumnInfo(name = "item_category")
    var Category: String="",
    @ColumnInfo(name = "item_preference")
    var Preference: String="0",


):Comparable<FoodItem> {
    override fun compareTo(compareFoodItem: FoodItem): Int {
        val comparePreference = compareFoodItem.Preference.toInt()
        return (Preference.toInt() - comparePreference)
    }
}