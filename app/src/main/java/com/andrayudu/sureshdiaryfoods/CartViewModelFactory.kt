package com.andrayudu.sureshdiaryfoods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.ui.CartViewModel

class CartViewModelFactory(private val repository: CartItemRepository):ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(CartViewModel::class.java)){
            return CartViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }

}
