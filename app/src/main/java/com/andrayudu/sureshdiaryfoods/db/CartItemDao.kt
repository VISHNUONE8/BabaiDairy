package com.andrayudu.sureshdiaryfoods.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andrayudu.sureshdiaryfoods.model.CartItem

@Dao
interface CartItemDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem):Long

    @Query("SELECT quantity FROM cart_item_table WHERE item_name = :name")
     suspend fun getCartCount(name:String):String

    @Update
    suspend fun updateCartItem(cartItem: CartItem):Int

    @Query("DELETE FROM cart_item_table WHERE item_name = :name")
    suspend fun deleteCartItem(name: String):Int

    @Query("SELECT IFNULL(SUM(quantity),0) FROM cart_item_table WHERE item_category=:category")
    suspend fun getKovaCount(category:String):Int


    @Query("DELETE FROM cart_item_table")
    suspend fun deleteAll():Int

    @Query("SELECT * FROM cart_item_table")
    fun getAllCartItems(): LiveData<List<CartItem>>




}