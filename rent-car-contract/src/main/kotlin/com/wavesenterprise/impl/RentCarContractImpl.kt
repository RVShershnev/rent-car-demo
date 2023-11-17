package com.wavesenterprise.impl

import com.wavesenterprise.api.RentCarContract
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.state.ContractState

@ContractHandler
class RentCarContractImpl(
    val contractState: ContractState,
): RentCarContract {

    override fun initRent() { }

    override fun setRentContractCreator(rentContractCreator: String) {
        contractState.put(CONTRACT_CREATOR_KEY, rentContractCreator) // Добавление в стейт по ключу CONTRACT_CREATOR значения переданного адреса
    }

    companion object {
        const val CONTRACT_CREATOR_KEY = "CONTRACT_CREATOR"
    }
}
