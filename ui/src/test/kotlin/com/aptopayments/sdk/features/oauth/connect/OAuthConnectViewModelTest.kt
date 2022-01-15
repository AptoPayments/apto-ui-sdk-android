package com.aptopayments.sdk.features.oauth.connect

import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.oauth.OAuthAttemptStatus
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val URL_TEST = "http://www.test.com"

@ExtendWith(InstantExecutorExtension::class)
class OAuthConnectViewModelTest : UnitTest() {

    private lateinit var sut: OAuthConnectViewModel

    private val allowedBalanceType: AllowedBalanceType = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val oauthAttemptStart: OAuthAttempt = mock {
        on { url } doReturn URL(URL_TEST)
    }
    private val oauthAttemptVerify: OAuthAttempt = mock()

    private var analyticsManager: AnalyticsServiceContract = mock()

    @BeforeEach
    fun setUp() {
        sut = OAuthConnectViewModel(allowedBalanceType, aptoPlatform, analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.SelectBalanceStoreOauthLogin)
    }

    @Test
    internal fun `given an result with URL when api is called then OauthPassed action is returned`() {
        configureStartOauth(oauthAttemptStart.right())

        sut.startOAuthAuthentication()

        val action = sut.action.getOrAwaitValue()
        assertTrue(action is OAuthConnectViewModel.Action.StartOauth)
        assertEquals(oauthAttemptStart.url, action.url)
    }

    @Test
    internal fun `given an result without URL when api is called then failure is raised`() {
        whenever(oauthAttemptStart.url).thenReturn(null)
        configureStartOauth(oauthAttemptStart.right())

        sut.startOAuthAuthentication()

        assertNotNull(sut.failure.getOrAwaitValue())
    }

    @Test
    internal fun `klajsd klajsdlka jsdlkj asldk`() {
        configureStartOauth(oauthAttemptStart.right())
        whenever(oauthAttemptVerify.status).thenReturn(OAuthAttemptStatus.PASSED)
        configureReload(oauthAttemptVerify.right())
        sut.startOAuthAuthentication()

        sut.reloadStatus()

        verify(aptoPlatform).verifyOauthAttemptStatus(eq(oauthAttemptStart), any())
    }

    @Test
    internal fun `given an oauth started when verification passed then action is OauthPassed`() {
        configureStartOauth(oauthAttemptStart.right())
        whenever(oauthAttemptVerify.status).thenReturn(OAuthAttemptStatus.PASSED)
        configureReload(oauthAttemptVerify.right())
        sut.startOAuthAuthentication()

        sut.reloadStatus()

        val action = sut.action.getOrAwaitValue()
        assertTrue(action is OAuthConnectViewModel.Action.OauthPassed)
    }

    @Test
    internal fun `given an oauth started when verification failed then action is OauthFailed`() {
        configureStartOauth(oauthAttemptStart.right())
        whenever(oauthAttemptVerify.status).thenReturn(OAuthAttemptStatus.FAILED)
        configureReload(oauthAttemptVerify.right())
        sut.startOAuthAuthentication()

        sut.reloadStatus()

        val action = sut.action.getOrAwaitValue()
        assertTrue(action is OAuthConnectViewModel.Action.OauthFailure)
    }

    @Test
    internal fun `given an oauth started when verification pending then action is OauthPending`() {
        configureStartOauth(oauthAttemptStart.right())
        whenever(oauthAttemptVerify.status).thenReturn(OAuthAttemptStatus.PENDING)
        configureReload(oauthAttemptVerify.right())
        sut.startOAuthAuthentication()

        sut.reloadStatus()

        val action = sut.action.getOrAwaitValue()
        assertTrue(action is OAuthConnectViewModel.Action.OauthPending)
    }

    @Test
    internal fun `given an oauth not started when verification called then no interaction with the API`() {
        sut.reloadStatus()

        verifyNoInteractions(aptoPlatform)
    }

    @Suppress("UNCHECKED_CAST")
    private fun configureStartOauth(result: Either<Failure, OAuthAttempt>) {
        whenever(
            aptoPlatform.startOauthAuthentication(eq(allowedBalanceType), any())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, OAuthAttempt>) -> Unit).invoke(result)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun configureReload(result: Either<Failure, OAuthAttempt>) {
        whenever(
            aptoPlatform.verifyOauthAttemptStatus(any(), any())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, OAuthAttempt>) -> Unit).invoke(result)
        }
    }
}
