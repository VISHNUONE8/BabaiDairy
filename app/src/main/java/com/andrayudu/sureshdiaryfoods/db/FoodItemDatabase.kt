package com.andrayudu.sureshdiaryfoods.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.FoodItem

@Database(entities = [FoodItem::class,CartItem::class],version = 12 , exportSchema = false)
abstract class FoodItemDatabase :RoomDatabase(){

    abstract val foodItemDao:FoodItemDao
    abstract val cartItemDao: CartItemDao



    companion object{
        @Volatile
        private var INSTANCE:FoodItemDatabase? = null
        fun getInstance(context: Context):FoodItemDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FoodItemDatabase::class.java,
                        "fooditem_database"
                    ).fallbackToDestructiveMigration()
                            //the above code snippet migrates old db to new db
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}