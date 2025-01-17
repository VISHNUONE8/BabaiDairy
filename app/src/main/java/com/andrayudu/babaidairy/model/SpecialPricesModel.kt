package com.andrayudu.babaidairy.model

data class SpecialPricesModel(
    var customerName:String?=null,
    var updatedAt:Long = 0,
    var normalKovaPrice: Int = 0,
    var splKovaPrice: Int = 0,
    var sugarKovaPrice: Int = 0,
    var sugarLessKovaPrice: Int = 0,
    var buffaloMilkPrice: Int = 0,
    var cowMilkPrice: Int = 0,
    var skimmedMilkPrice: Int = 0,
    var hundredBoiledPrice: Int = 0,
    var seventyBoiledPrice: Int = 0,
    var fiftyBoiledPrice: Int = 0,
    var agraPanPrice: Int = 0,
    var kajuBytesPrice: Int = 0,
    var killiPrice: Int = 0,
    var soanPapdiPrice: Int = 0,
    var splSoanPapdiPrice: Int = 0,
    var chakodiPrice: Int = 0,
    var dhalMixturePrice: Int = 0,
    var marwadiMixture: Int = 0,
    var moongDalPrice: Int = 0,
    var splMixturePrice: Int = 0,
    var halfKgPaneerPrice: Int = 0,
    var oneKgPaneerPrice: Int = 0,
    var fiveKgPaneerPrice: Int = 0)
