package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class ClearCardDetailsUseCaseTest : UnitTest() {

    @Mock
    private lateinit var repo: LocalCardDetailsRepository

    lateinit var sut: ClearCardDetailsUseCase

    @Before
    fun setUp() {
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
