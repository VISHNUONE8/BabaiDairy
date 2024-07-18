package com.andrayudu.babaidairy.ui

import org.junit.After
import org.junit.Before

class HomeActivityTest{

    private lateinit var homeActivity:HomeActivity

    @Before
    fun setUp() {
        homeActivity = HomeActivity()
        println("Ready for testing!");
    }

    @After
    fun tearDown() {
        println("Done with unit test!");
    }
}