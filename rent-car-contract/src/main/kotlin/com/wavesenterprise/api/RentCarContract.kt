package com.wavesenterprise.api

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam

interface RentCarContract {

    @ContractInit
    fun initRent() // Метод вызова 103 транзакции (CreateContractTx)

    @ContractAction
    fun setRentContractCreator(
        @InvokeParam("rentContractCreator") rentContractCreator: String
    ) // Метод вызова 104 транзакции (CallContractTx)
}
