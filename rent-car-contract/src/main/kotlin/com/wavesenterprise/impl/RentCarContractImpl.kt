package com.wavesenterprise.impl

import com.wavesenterprise.api.RentCarContract
import com.wavesenterprise.domain.Car
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.core.state.getValue
import com.wavesenterprise.sdk.node.domain.contract.ContractId.Companion.base58ContractId

@ContractHandler
class RentCarContractImpl(
    val contractState: ContractState,
    val call: ContractCall,
) : RentCarContract {

    val cars: Mapping<Car> by contractState

    override fun initRent() {
        val creatorAddress: String = call.sender.asBase58String()
        contractState.put(CONTRACT_CREATOR_KEY, creatorAddress)
        cars.put(
            key = "1",
            value = Car(
                name = "bmw",
                renter = null,
                number = "1"
            )
        )
        cars.put(
            key = "2",
            value = Car(
                name = "mersedes",
                renter = null,
                number = "2"
            )
        )
        cars.put(
            key = "3",
            value = Car(
                name = "audi",
                renter = null,
                number = "3"
            )
        )
    }

    override fun rentCar(carNumber: String) {
        val sender = call.sender.asBase58String()
        checkInBlackList(sender)
        val car = getCarIfExist(carNumber)
        checkRenterExist(car)
        car.renter = sender
        cars.put(carNumber, car)
    }

    override fun createCar(car: Car) {
        checkContractCreator()
        cars.put(car.number, car)
    }

    override fun changeCarRenter(carNumber: String, carRenter: String) {
        checkContractCreator()
        val car = getCarIfExist(carNumber)
        car.renter = carRenter
        cars.put(carNumber, car)
    }

    override fun setBlackListContract(contractId: String) {
        checkContractCreator()
        contractState.put(BLACK_LIST_CONTRACT_ID, contractId)
    }

    private fun checkInBlackList(address: String) {
        val blackListContractIdOptional = contractState.tryGet(BLACK_LIST_CONTRACT_ID, String::class.java)
        if (blackListContractIdOptional.isPresent) {
            val blackListItemOptional = contractState.external(blackListContractIdOptional.get().base58ContractId).tryGet(
                "BLACK_LIST_$address", String::class.java
            )
            if (blackListItemOptional.isPresent) {
                throw IllegalStateException("Sender with $address exist in black list.")
            }
        } else {
            return
        }
    }

    private fun checkRenterExist(car: Car) {
        if (car.renter == null) {
            return
        } else {
            throw IllegalStateException("Car with id ${car.number} has renter.")
        }
    }

    private fun checkContractCreator() {
        val senderAddress = call.sender.asBase58String()
        val contractCreator = contractState[CONTRACT_CREATOR_KEY, String::class.java]
        if (senderAddress != contractCreator) {
            throw IllegalStateException("Only contract creator can create ot change cars.")
        }
    }

    private fun getCarIfExist(carNumber: String): Car {
        val carOptional = cars.tryGet(carNumber)
        if (carOptional.isEmpty) {
            throw IllegalStateException("Car with number $carNumber is not exist.")
        } else {
            return carOptional.get()
        }
    }

    companion object {
        const val CONTRACT_CREATOR_KEY = "CONTRACT_CREATOR"
        const val BLACK_LIST_CONTRACT_ID = "BLACK_LIST_CONTRACT_ID"
    }
}
