package com.andrayudu.sureshdiaryfoods.db

import com.andrayudu.sureshdiaryfoods.model.CartItem

class CartItemRepository(private val cartItemDao: CartItemDao) {



    val cartItems = cartItemDao.getAllCartItems()

    suspend fun insert(cartItem:CartItem):Long{
        return cartItemDao.insertCartItem(cartItem)
    }
    suspend fun getKovaCount(category:String):Int
    {
        return cartItemDao.getKovaCount(category)
    }

    suspend fun update(cartItem:CartItem):Int{
        return cartItemDao.updateCartItem(cartItem)
    }
    suspend fun delete(name:String):Int{
        return cartItemDao.deleteCartItem(name)
    }
    suspend fun deleteAll():Int{
        return   cartItemDao.deleteAll()
    }
}