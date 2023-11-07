package com.andrayudu.sureshdiaryfoods.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.FoodItem

@Database(entities = [FoodItem::class,CartItem::class],version = 1 , exportSchema = false)
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
                        //the above code snippet -since we are not exporting the database schema,if we change the version number,it will try to migrate the old data to new data scheme
                        // when trying if there are no migration codes/methods writtened it will throw illegalState Exception
                        // so,fallback will create a new database everytime the version number is changed and there will be no error...
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}