package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.inject
import org.koin.test.mock.declareMock

internal class ClearCardDetailsUseCaseTest : UnitTest() {
    lateinit var sut: ClearCardDetailsUseCase
    val repo: LocalCardDetailsRepository by inject()

    @Before
    fun configureKoin() {
        startKoin {
            modules(listOf(applicationModule))
        }

        declareMock<LocalCardDetailsRepository>()
        sut = ClearCardDetailsUseCase(repo)
    }

    @Test
    fun `when pin and biometrics authentication needed`() {
        val result = sut()

        verify(repo).clear()
        result.isRight shouldEqual true
        result.either({}, { it shouldBe Unit })
    }

}
