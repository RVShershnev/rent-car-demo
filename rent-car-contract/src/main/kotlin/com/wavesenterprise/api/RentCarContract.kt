package com.wavesenterprise.api

import com.wavesenterprise.domain.Car
import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam

interface RentCarContract {

    @ContractInit
    fun initRent()

    @ContractAction
    fun rentCar(@InvokeParam("carNumber") carNumber: String)

    @ContractAction
    fun createCar(@InvokeParam("car") car: Car)

    @ContractAction
    fun changeCarRenter(
        @InvokeParam("carNumber") carNumber: String,
        @InvokeParam("carRenter") carRenter: String,
    )

    @ContractAction
    fun setBlackListContract(
        @InvokeParam("contractId") contractId: String,
    )
}
