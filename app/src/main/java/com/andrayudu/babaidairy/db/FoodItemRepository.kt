package com.andrayudu.babaidairy.db

import com.andrayudu.babaidairy.model.FoodItem

class FoodItemRepository(private val dao:FoodItemDao) {


    val foodItems = dao.getAllFoodItems()



    suspend fun insert(foodItem: FoodItem):Long{
        return dao.insertFoodItem(foodItem)
    }
    suspend fun update(foodItem: FoodItem):Int{
        return dao.updateFoodItem(foodItem)
    }
    suspend fun delete(name: String):Int{
        return dao.deleteFoodItem(name)
    }
    suspend fun deleteAll():Int{
        return   dao.deleteAll()
    }


}