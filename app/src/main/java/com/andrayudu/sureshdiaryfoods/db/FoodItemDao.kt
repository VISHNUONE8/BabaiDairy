package com.andrayudu.sureshdiaryfoods.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andrayudu.sureshdiaryfoods.model.FoodItem

@Dao
interface FoodItemDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem):Long

    //used for inserting multiple food items at once
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodDetails: List<FoodItem?>?)

    @Update
    suspend fun updateFoodItem(foodItem: FoodItem):Int

    @Query("DELETE FROM customer_orders_table WHERE fooditem_name = :name")
    suspend fun deleteFoodItem(name:String):Int


//    @Delete
//    suspend fun deleteFoodItem(foodItem: FoodItem):Int

    @Query("DELETE FROM customer_orders_table")
    suspend fun deleteAll():Int

    @Query("SELECT * FROM customer_orders_table")
    fun getAllFoodItems(): LiveData<List<FoodItem>>
}