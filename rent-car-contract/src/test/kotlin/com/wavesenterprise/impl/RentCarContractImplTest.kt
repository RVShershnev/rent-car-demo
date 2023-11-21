package com.wavesenterprise.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.domain.Car
import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import com.wavesenterprise.sdk.node.domain.Address
import org.junit.jupiter.api.Assertions.assertEquals
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import com.wavesenterprise.impl.RentCarContractImpl.Companion.CONTRACT_CREATOR_KEY
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.ContractStateReader
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.util.*


class RentCarContractImplTest {


    @Test
    fun `should init with contract creator and default state`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )

        contract.initRent()

        val carsMapping = state.getMapping(Car::class.java, "CARS")

        assertEquals(
            senderAddress,
            state[CONTRACT_CREATOR_KEY, String::class.java]
        )

        val expectedCarsNumbers = setOf("1", "2", "3")
        assertTrue(carsMapping.hasAll(expectedCarsNumbers))
    }

    @Test
    fun `should rent existing car`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val carsMapping = state.getMapping(Car::class.java, "CARS")
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )

        carsMapping.put(
            key = "1",
            value = Car(
                name = "bmw",
                renter = null,
                number = "1"
            )
        )
        contract.rentCar("1")

        assertEquals(senderAddress, carsMapping["1"].renter)
    }

    @Test
    fun `shouldn't rent not existing car`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )

        assertThrows<IllegalStateException> {
            contract.rentCar("1")
        }.also {
            assertEquals(
                "Car with number 1 is not exist.",
                it.message
            )
        }
    }

    @Test
    fun `shouldn't rent car if sender exist in black list contract`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val blackListContractId = "9bEGVyg9HowqQ4ThD75zayYKoq64ikzXvgqxY1UR2rRL"
        val externalState: ContractState = mockk<ContractState>().also {
            every { it.tryGet(any(), String::class.java) } returns Optional.of(senderAddress)
        }
        val state: ContractState = mockk<ContractState>().also {
            every { it.external(any()) } returns externalState
            every { it.tryGet(any(), String::class.java) } returns Optional.of(blackListContractId)
        }
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }

        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )

        assertThrows<IllegalStateException> {
            contract.rentCar("1")
        }
    }

    @Test
    fun `should create car by contract creator`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )
        state.put(CONTRACT_CREATOR_KEY, senderAddress)
        val car = Car(
            name = "bmw",
            renter = null,
            number = "1"
        )

        contract.createCar(car)
        val carMapping = state.getMapping(Car::class.java, "CARS")

        assertEquals(car, carMapping["1"])
    }

    @Test
    fun `shouldn't create car by not contract creator`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val contractCreatorAddress = "H43VeKaxTaAxVqCdee4zP9Jy2kvmTTjAEvPDFit7t5Pb"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(contractCreatorAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )
        state.put(CONTRACT_CREATOR_KEY, senderAddress)
        val car = Car(
            name = "bmw",
            renter = null,
            number = "1"
        )

        assertThrows<IllegalStateException> {
            contract.createCar(car)
        }.also {
            assertEquals(
                "Only contract creator can create ot change cars.",
                it.message
            )
        }
    }

    @Test
    fun `should change car renter if car exist`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val newCarRenter = "H43VeKaxTaAxVqCdee4zP9Jy2kvmTTjAEvPDFit7t5Pb"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )
        state.put(CONTRACT_CREATOR_KEY, senderAddress)
        val carsMapping = state.getMapping(Car::class.java, "CARS")
        val car = Car(
            name = "bmw",
            renter = null,
            number = "1"
        )
        carsMapping.put(car.number, car)
        contract.changeCarRenter(car.number, newCarRenter)

        assertEquals(newCarRenter, carsMapping[car.number].renter)
    }

    @Test
    fun `shouldn't change car renter if car not exist`() {
        val senderAddress = "3NqmRauaV87hhJPz1wzS6wx8kqWD5i7coCM"
        val newCarRenter = "H43VeKaxTaAxVqCdee4zP9Jy2kvmTTjAEvPDFit7t5Pb"
        val state = ContractTestStateFactory.state(jacksonObjectMapper())
        val call: ContractCall = mockk<ContractCall>().also {
            every { it.sender } returns Address.fromBase58(senderAddress)
        }
        val contract = RentCarContractImpl(
            contractState = state,
            call = call,
        )
        state.put(CONTRACT_CREATOR_KEY, senderAddress)

        assertThrows<IllegalStateException> {
            contract.changeCarRenter("1", newCarRenter)
        }.also {
            assertEquals(
                "Car with number 1 is not exist.",
                it.message
            )
        }
    }
}
