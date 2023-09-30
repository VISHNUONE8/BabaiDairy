package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.*
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.FoodItem
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.SpecialPricesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections

//this viewmodel is used by both HomeFragment and FoodItemsActivity but not in a shared way
//for sharedviewmodel instance see homeActivity viewmodel as it is shared by ordersFrag and HomeActivity
//note: for sharing we should by viewModels() and by activityviewModels()
class FoodItemsViewModel(private val repository: CartItemRepository): ViewModel() {


    private val tag = "FoodItemsViewModel"

    var specialPricesModel :SpecialPricesModel?  = null
    val foodItemsList: ArrayList<FoodItem> = ArrayList()

    //LiveData
    val cartItems = repository.cartItems
    private val status = MutableLiveData<String>()
    private val firebaseFoodItems =  MutableLiveData<List<FoodItem>>()


    fun getStatus():LiveData<String>{
        return status
    }
    fun getFirebaseFoodItems():LiveData<List<FoodItem>>{
        return firebaseFoodItems
    }

    //to insert a value into the database
    fun insert(foodItem: FoodItem) {
        viewModelScope.launch(Dispatchers.IO)
        {
            //by this time the quantity value is changed to 0 already
            if(foodItem.Quantity.equals("0")){
                Log.i("this condition is executed","now"+foodItem.Name)
                repository.delete(foodItem.Name)
            }
            else{
                val current = LocalDateTime.now()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formatted = current.format(formatter)
                val newRowId = repository.insert(CartItem(foodItem.Name,foodItem.Price,foodItem.Quantity.toString(),foodItem.Category))
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

    //loads splPriceSnap and updates the status LiveData
    fun getSpecialPricesSnapshot(){

        viewModelScope.launch (Dispatchers.IO){
            val mAuth = FirebaseAuth.getInstance()
            val userId = mAuth.currentUser?.uid

            FirebaseDatabase.getInstance().getReference("SpecialPrices").child(userId.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            specialPricesModel = snapshot.getValue(SpecialPricesModel::class.java)
                            Log.i(tag,"specialprice snap loaded")
                        }
                        status.postValue("loaded")
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


    }


    fun getSpecialPrice(foodItem: FoodItem?):FoodItem?{

        var specificPrice:String? = null

        when(foodItem?.Category){
            "Kova"-> {

                 specificPrice = specialPricesModel?.kovaPrice
                 Log.i(tag,"kova price for this customer is:"+specificPrice)


            }
//            "SpecialKova"->{
//
//                val specificPrice = specialPriceSnapshot.child("SpecialKova").getValue().toString()
//                item.Price = specificPrice
//
//
//            }
            "Ghee"->{

                 specificPrice = specialPricesModel?.gheePrice

            }
//
            "OtherSweets"->{
                //other sweets has many different items so based on the item we should get price
                 specificPrice = specialPricesModel?.otherSweetsPrice
            }

            else->{
                //if the item doesnt belong to any above categories its price will stay same...

            }

        }
        if (specificPrice!=null){
            foodItem?.Price = specificPrice!!
            Log.i("spl price is:",""+foodItem?.Price)

        }
        return foodItem
    }
    fun loadItems(itemName:String?) {
        viewModelScope.launch(Dispatchers.IO) {

            if (firebaseFoodItems.value == null) {
                FirebaseDatabase.getInstance().getReference("FoodItems").child(itemName.toString())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {

                                for (dataSnapshot in snapshot.children) {
                                    val foodItem: FoodItem? = dataSnapshot.getValue(FoodItem::class.java)

                                    Log.i(tag,"the item is "+foodItem!!.Name)
                                    //if the users special price snap is null then it will show normal prices
                                    val splPriceAddedItem = getSpecialPrice(foodItem)
                                    foodItemsList.add(splPriceAddedItem!!)
                                }
                                Collections.sort(foodItemsList)
                                firebaseFoodItems.postValue(foodItemsList)


                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
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