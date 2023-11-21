package com.wavesenterprise

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.impl.BlackListContractImpl
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BlackListContractImplTest {

    @Test
    fun `should add renter address in black list`() {
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val blackListContract = BlackListContractImpl(
            contractState = state,
        )
        val someAddress = "address"
        blackListContract.addRenter(someAddress)

        assertEquals(
            someAddress,
            state["BLACK_LIST_$someAddress", String::class.java]
        )
    }
}
