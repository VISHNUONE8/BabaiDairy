package com.andrayudu.sureshdiaryfoods.ui

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith

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