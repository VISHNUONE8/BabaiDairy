package com.andrayudu.sureshdiaryfoods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsViewModel

class HomeActivityViewModelFactory(private val repository: CartItemRepository) : ViewModelProvider.Factory {

    override fun<T: ViewModel> create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(HomeActivityViewModel::class.java)){
            return HomeActivityViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }

}
