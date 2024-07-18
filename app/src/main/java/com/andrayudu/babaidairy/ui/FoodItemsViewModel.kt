package com.andrayudu.babaidairy.ui

import android.util.Log
import androidx.lifecycle.*
import com.andrayudu.babaidairy.model.CartItem
import com.andrayudu.babaidairy.db.CartItemRepository
import com.andrayudu.babaidairy.model.FoodItem
import com.andrayudu.babaidairy.model.ItemsCatalogueModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//this viewmodel is used by both HomeFragment and FoodItemsActivity but not in a shared way
//for sharedviewmodel instance see homeActivity viewmodel as it is shared by ordersFrag and HomeActivity
//note: for sharing we should by viewModels() and by activityviewModels()
class FoodItemsViewModel(private val repository: CartItemRepository): ViewModel() {


    private val TAG = "FoodItemsViewModel"

    private val mDb =  FirebaseDatabase.getInstance()
    var specialPriceCatalogue = ItemsCatalogueModel()
    //LiveData
    val cartItems = repository.cartItems
    private val _firebaseFoodItems =  MutableLiveData<List<FoodItem>>()
    val firebaseFoodItems: LiveData<List<FoodItem>>
      get() = _firebaseFoodItems


    private var foodCatalogueFromIntent:ItemsCatalogueModel? = null



    //to insert a value into the database
    fun insert(foodItem: FoodItem) {
        viewModelScope.launch(Dispatchers.IO)
        {
            //if the foodItem quantity is 0 then the item will be deleted from the cart...
            if(foodItem.Quantity == 0){
                repository.delete(foodItem.Name)
            }
            else{
                val cartItem = CartItem()
                cartItem.Name = foodItem.Name
                cartItem.Quantity = foodItem.Quantity
                cartItem.Category = foodItem.Category
                cartItem.Price = foodItem.Price
                //calculating cartItem Total Price i.e quantity * price
                if(cartItem.Category.equals("Kova") || cartItem.Category.equals("KovaSpl")){
                    cartItem.ItemTotalPrice = (cartItem.Quantity * cartItem.Price * 3)
                }
                else{
                    cartItem.ItemTotalPrice =(cartItem.Quantity * cartItem.Price)
                }
                val newRowId = repository.insert(cartItem)
                withContext(Dispatchers.Main) {
                    if (newRowId > -1) {
                        Log.i("insert is successful","bro")
                    } else {

                        Log.i("insert has failed","bro")

                    }
                }
            }

        }

    }

    fun getSpecialPricesList(foodCatalogueIntent: ItemsCatalogueModel?, itemCategoryFromIntent: String?) {

        foodCatalogueFromIntent = foodCatalogueIntent
        viewModelScope.launch (Dispatchers.IO){
            val mAuth = FirebaseAuth.getInstance()
            val userId = mAuth.currentUser?.uid

            try{
                 mDb.getReference("SpecialPricesList").child(userId!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            specialPriceCatalogue = ItemsCatalogueModel()
                            if (snapshot.exists()) {
                                val specialCatalogueFromDb = snapshot.getValue(ItemsCatalogueModel::class.java)
                                Log.i(TAG,"specialpriceCatalogue loaded")
                                if (specialCatalogueFromDb!=null){
                                    specialPriceCatalogue = specialCatalogueFromDb
                                }
                            }
                            Log.i(TAG,"the special catalogue is:$specialPriceCatalogue")
                            postSpecialCatalogue(itemCategoryFromIntent)
                        }
                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }catch (e:Exception){
                Log.e(TAG,"The error is:${e.message.toString()}")
            }
        }
    }

    //after the specialPrices list of user is loaded,we will use
    //spl list to change the catalogue list prices to spl pricess..
    private fun postSpecialCatalogue(itemCategoryFromIntent: String?) {
        val specialPriceList = specialPriceCatalogue.itemsList
        val foodCatalogueListIntent = foodCatalogueFromIntent?.itemsList
        val displayList = ArrayList<FoodItem>()
        try {
            for (item in foodCatalogueListIntent!! ){
                val itemName = item.Name
                //we will filter the selected category
                if (item.Category.equals(itemCategoryFromIntent)){
                    val splItem =  specialPriceList?.find { ( it.Name == itemName) }
                    if (splItem != null) {
                        item.Price = splItem.Price
                    }
                    displayList.add(item)
                }


            }
            //sorts the list according to the preference
            displayList.sort()
            _firebaseFoodItems.postValue(displayList)
        }
        catch (e:Exception){
              Log.e(TAG,"An exception occurred:${e.message.toString()}")
//            TODO(here implement livedata  so that it will be displayed as toast in the activity)
        }

    }

}

class FoodItemsViewModelFactory(private val repository: CartItemRepository) : ViewModelProvider.Factory {

    override fun<T: ViewModel> create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(FoodItemsViewModel::class.java)){
            return FoodItemsViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }

}