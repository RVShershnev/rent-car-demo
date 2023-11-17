package com.wavesenterprise.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.impl.RentCarContractImpl.Companion.CONTRACT_CREATOR_KEY
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RentCarContractImplTest {

    @Test
    fun `should set contract creator by CONTRACT_CREATOR_KEY`() {
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val contract = RentCarContractImpl(
            contractState = state,
        )
        val renterAddress = "address"
        contract.setRentContractCreator(renterAddress)

        val result = state[CONTRACT_CREATOR_KEY, String::class.java]

        assertEquals(renterAddress, result)
    }
}
