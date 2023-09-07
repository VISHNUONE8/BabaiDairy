package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.Event
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.FoodItem
import com.andrayudu.sureshdiaryfoods.model.CartItem
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

class FoodItemsViewModel(private val repository: CartItemRepository): ViewModel() {

    val cartItems = repository.cartItems


     var specialPriceSnapshot: DataSnapshot? = null

    val foodItemsList: ArrayList<FoodItem> = ArrayList()

    var firebaseFoodItems: MutableLiveData<List<FoodItem>> = MutableLiveData()

    //    val firebaseList:List<FoodItem> = List()
    private val foodItemList: ArrayList<FoodItem?> = ArrayList()


    private var isUpdateOrDelete = false
    private lateinit var foodItem: FoodItem

    val foodName = MutableLiveData<String>()
    val foodQuantity = MutableLiveData<String>()
    val foodPrice = MutableLiveData<String>()

//    val saveorUpdateButtonText = MutableLiveData<String>()
//    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()

    val message: LiveData<Event<String>>
        get() = statusMessage


    init {
//        saveorUpdateButtonText.value = "Save"
//        clearAllOrDeleteButtonText.value = "Clear All"
    }


    fun saveOrUpdate() {


        if (foodName.value == null) {
            statusMessage.value = Event("Please Enter Food Name")
        } else if (foodQuantity.value == null) {
            statusMessage.value = Event("Please Enter Food Quantity")
        }
//        else if(foodPrice.value==null){
//            statusMessage.value = Event("Please Enter Food Price")
//        }

        else {

//            if(isUpdateOrDelete){
//                subscriberToUpdateOrDelete.name = inputName.value!!
//                subscriberToUpdateOrDelete.email  =inputEmail.value!!
//                update(subscriberToUpdateOrDelete)
//            }
            val foodItemName = foodName.value!!
            val quantity = foodQuantity.value!!
            val price = foodPrice.value!!

            insert(FoodItem(foodItemName,price,quantity))
            //After the insertion is completed we are clearing the input email and input name attributes
            foodName.value = ""
            foodQuantity.value = "0"
            foodPrice.value = "0"

        }
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
                val newRowId = repository.insert(CartItem(foodItem.Name,foodItem.Price,foodItem.Quantity.toString(),foodItem.Category,formatted.toString()))
                withContext(Dispatchers.Main) {
                    if (newRowId > -1) {
                        Log.i("insert is successful","bro")
                        statusMessage.value = Event("FoodItem Inserted Successfully! $newRowId")
                    } else {
                        statusMessage.value = Event("Error Occured")

                    }
                }
            }

        }

    }
    fun getSpecialPricesSnapshot(){
        val mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid


        FirebaseDatabase.getInstance().getReference("SpecialPrices").child(userId.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        specialPriceSnapshot = snapshot
//                        snapshot.key returns the snapshots name
                        Log.i("TAG","the snapshot key is:"+ snapshot.key)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }

    fun getSpecialPrice(foodItem: FoodItem?):FoodItem?{

        var specificPrice:String? = "null"

        when(foodItem?.Category){
            "Kova"-> {

                 specificPrice = specialPriceSnapshot?.child("Kova")?.getValue().toString()


            }
//            "SpecialKova"->{
//
//                val specificPrice = specialPriceSnapshot.child("SpecialKova").getValue().toString()
//                item.Price = specificPrice
//
//
//            }
            "Ghee"->{

                 specificPrice = specialPriceSnapshot?.child("Ghee")?.getValue().toString()


            }
//
            "OtherSweets"->{
                //other sweets has many different items so based on the item we should get price
                 specificPrice = specialPriceSnapshot?.child(foodItem.Name)?.getValue().toString()

            }

            else->{
                //if the item doesnt belong to categoories kova and special kova






            }

        }
        if (specificPrice!="null"){
            foodItem?.Price = specificPrice!!
            Log.i("spl price is:",""+foodItem?.Price)

        }
        return foodItem
    }


    fun getFirebaseData(itemName:String?) {

        if (firebaseFoodItems.value == null) {
            FirebaseDatabase.getInstance().getReference("FoodItems").child(itemName.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            println("The value of the required is:"+snapshot.child("PiccaKova").child("Name").value)

                            for (dataSnapshot in snapshot.children) {
                                val FoodItemsModel: FoodItem? =
                                    dataSnapshot.getValue(FoodItem::class.java)


                                    //if the users special price snap is null then it will show normal prices
                                    getSpecialPrice(FoodItemsModel)


                                foodItemsList.add(FoodItemsModel!!)
                            }

                            Collections.sort(foodItemsList)
                            firebaseFoodItems.postValue(foodItemsList)


                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    fun clearAllOrDelete() {


        clearAll()


    }




    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO)

        {

            val numberOfRowsDeleted = repository.deleteAll()
            withContext(Dispatchers.Main) {
                if (numberOfRowsDeleted > 0) {


                    statusMessage.value =
                        Event("$numberOfRowsDeleted Food Items Deleted Successfully")
                } else {
                    statusMessage.value = Event("An Error Occured")

                }

            }

        }
    }
}