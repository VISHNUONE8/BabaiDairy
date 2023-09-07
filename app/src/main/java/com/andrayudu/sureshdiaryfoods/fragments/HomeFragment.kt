package com.andrayudu.sureshdiaryfoods.fragments

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FragmentHomeBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemDao
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.ui.CartActivity
import com.andrayudu.sureshdiaryfoods.ui.FoodItemsActivity

class HomeFragment : Fragment() {


    lateinit var binding: FragmentHomeBinding
    lateinit var mContext: Context


    lateinit var FoodIntent: Intent

    lateinit var dao: CartItemDao





    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        dao = FoodItemDatabase.getInstance(mContext).cartItemDao
    }
    fun expand(){

        val v = if(binding.hiddenLayout.visibility == View.GONE)
            View.VISIBLE
        else
            View.GONE

        TransitionManager.beginDelayedTransition(binding.kovaLayout,AutoTransition())
        binding.hiddenLayout.visibility = v


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)



        binding.kovaLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        FoodIntent = Intent(mContext, FoodItemsActivity::class.java)




        binding.oilCardview.setOnClickListener {

            FoodIntent.putExtra("itemName", "Oil")
            startActivity(FoodIntent)

        }
        binding.gheeCardview.setOnClickListener {
            Log.i("TAG", "The  ghee cardbiew is clicked")

            FoodIntent.putExtra("itemName", "Ghee")
            startActivity(FoodIntent)


        }
        binding.milkCardview.setOnClickListener {
            Log.i("TAG", "The  Milk cardbiew is clicked")
            FoodIntent.putExtra("itemName", "Milk")
            startActivity(FoodIntent)


        }
        binding.otherSweetsCardview.setOnClickListener {
            Log.i("TAG", "The  Milk cardbiew is clicked")
            FoodIntent.putExtra("itemName", "OtherSweets")
            startActivity(FoodIntent)


        }
//        binding.kovaCardview.setOnClickListener {
//            Log.i("TAG", "The  kova cardbiew is clicked")
//
//
//            FoodIntent.putExtra("itemName", "Kova")
//            startActivity(FoodIntent)
//
//        }
        binding.normalKovaTV.setOnClickListener {

            FoodIntent.putExtra("itemName", "Kova")
            startActivity(FoodIntent)
        }
        binding.speciallKovaTV.setOnClickListener {

            FoodIntent.putExtra("itemName", "SpecialKova")
            startActivity(FoodIntent)
        }
        binding.kovaLayout.setOnClickListener {
            expand()
        }
        binding.mixtureCardview.setOnClickListener {
            FoodIntent.putExtra("itemName", "Hot")
            startActivity(FoodIntent)

        }

        binding.cartBtn.setOnClickListener {

            startActivity(Intent(mContext, CartActivity::class.java))
        }

        return binding.root
    }

}