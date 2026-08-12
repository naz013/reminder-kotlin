package com.github.naz013.reviews.auth

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FirebaseAuthManager.
 *
 * Tests authentication operations including sign-in, sign-out, and user state checks.
 */
class FirebaseAuthManagerTest {

  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var authManager: FirebaseAuthManager

  /**
   * Sets up test fixtures before each test.
   * Initializes mocked FirebaseAuth and FirebaseAuthManager instances.
   */
  @Before
  fun setup() {
    firebaseAuth = mockk(relaxed = true)
    authManager = FirebaseAuthManager(firebaseAuth)
  }

  /**
   * Tests successful anonymous sign-in when user is not already signed in.
   * Validates that the sign-in process completes successfully and returns a user.
   */
  @Test
  fun `signInAnonymously signs in new user successfully`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser>(relaxed = true) {
      every { uid } returns "test-user-id"
    }
    val mockAuthResult = mockk<AuthResult>(relaxed = true) {
      every { user } returns mockUser
    }

    every { firebaseAuth.currentUser } returns null
    every { firebaseAuth.signInAnonymously() } returns Tasks.forResult(mockAuthResult)

    // When
    val result = authManager.signInAnonymously()

    // Then
    assertTrue("Expected success but got failure: ${result.exceptionOrNull()}", result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
    verify { firebaseAuth.signInAnonymously() }
  }

  /**
   * Tests sign-in when user is already authenticated.
   * Validates that existing authentication is recognized and no new sign-in occurs.
   */
  @Test
  fun `signInAnonymously returns existing user when already signed in`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser> {
      every { uid } returns "existing-user-id"
    }
    every { firebaseAuth.currentUser } returns mockUser

    // When
    val result = authManager.signInAnonymously()

    // Then
    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
    verify(exactly = 0) { firebaseAuth.signInAnonymously() }
  }

  /**
   * Tests sign-in failure when Firebase returns null user.
   * Validates that proper error handling occurs when authentication succeeds but user is null.
   */
  @Test
  fun `signInAnonymously returns failure when user is null`() = runTest {
    // Given
    val mockAuthResult = mockk<AuthResult>(relaxed = true) {
      every { user } returns null
    }

    every { firebaseAuth.currentUser } returns null
    every { firebaseAuth.signInAnonymously() } returns Tasks.forResult(mockAuthResult)

    // When
    val result = authManager.signInAnonymously()

    // Then
    assertTrue("Expected failure but got success", result.isFailure)
    assertNotNull(result.exceptionOrNull())
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  /**
   * Tests sign-in failure due to network or Firebase error.
   * Validates that exceptions are properly caught and returned as failures.
   */
  @Test
  fun `signInAnonymously returns failure when exception occurs`() = runTest {
    // Given
    val exception = RuntimeException("Network error")

    every { firebaseAuth.currentUser } returns null
    every { firebaseAuth.signInAnonymously() } returns Tasks.forException(exception)

    // When
    val result = authManager.signInAnonymously()

    // Then
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }

  /**
   * Tests getting current user when user is signed in.
   * Validates that the current user is correctly retrieved.
   */
  @Test
  fun `getCurrentUser returns user when signed in`() {
    // Given
    val mockUser = mockk<FirebaseUser> {
      every { uid } returns "current-user-id"
    }
    every { firebaseAuth.currentUser } returns mockUser

    // When
    val user = authManager.getCurrentUser()

    // Then
    assertNotNull(user)
    assertEquals(mockUser, user)
  }

  /**
   * Tests getting current user when no user is signed in.
   * Validates that null is returned when no authentication exists.
   */
  @Test
  fun `getCurrentUser returns null when not signed in`() {
    // Given
    every { firebaseAuth.currentUser } returns null

    // When
    val user = authManager.getCurrentUser()

    // Then
    assertNull(user)
  }

  /**
   * Tests getting current user ID when user is signed in.
   * Validates that the user ID is correctly retrieved.
   */
  @Test
  fun `getCurrentUserId returns uid when signed in`() {
    // Given
    val mockUser = mockk<FirebaseUser> {
      every { uid } returns "user-id-123"
    }
    every { firebaseAuth.currentUser } returns mockUser

    // When
    val userId = authManager.getCurrentUserId()

    // Then
    assertNotNull(userId)
    assertEquals("user-id-123", userId)
  }

  /**
   * Tests getting current user ID when no user is signed in.
   * Validates that null is returned when no authentication exists.
   */
  @Test
  fun `getCurrentUserId returns null when not signed in`() {
    // Given
    every { firebaseAuth.currentUser } returns null

    // When
    val userId = authManager.getCurrentUserId()

    // Then
    assertNull(userId)
  }

  /**
   * Tests signed-in status check when user is authenticated.
   * Validates that isSignedIn returns true when user exists.
   */
  @Test
  fun `isSignedIn returns true when user is authenticated`() {
    // Given
    val mockUser = mockk<FirebaseUser>()
    every { firebaseAuth.currentUser } returns mockUser

    // When
    val signedIn = authManager.isSignedIn()

    // Then
    assertTrue(signedIn)
  }

  /**
   * Tests signed-in status check when no user is authenticated.
   * Validates that isSignedIn returns false when no user exists.
   */
  @Test
  fun `isSignedIn returns false when not authenticated`() {
    // Given
    every { firebaseAuth.currentUser } returns null

    // When
    val signedIn = authManager.isSignedIn()

    // Then
    assertFalse(signedIn)
  }

  /**
   * Tests sign-out functionality.
   * Validates that signOut calls FirebaseAuth.signOut().
   */
  @Test
  fun `signOut calls Firebase sign out`() {
    // Given
    val mockUser = mockk<FirebaseUser> {
      every { uid } returns "user-to-sign-out"
    }
    every { firebaseAuth.currentUser } returns mockUser
    every { firebaseAuth.signOut() } returns Unit

    // When
    authManager.signOut()

    // Then
    verify { firebaseAuth.signOut() }
  }

  /**
   * Tests sign-out with no user signed in.
   * Validates that signOut handles null user gracefully.
   */
  @Test
  fun `signOut handles null user gracefully`() {
    // Given
    every { firebaseAuth.currentUser } returns null
    every { firebaseAuth.signOut() } returns Unit

    // When
    authManager.signOut()

    // Then
    verify { firebaseAuth.signOut() }
  }

  /**
   * Tests ensureAuthenticated when user is already signed in.
   * Validates that authentication check succeeds for existing users.
   */
  @Test
  fun `ensureAuthenticated returns success when user is signed in`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser>()
    every { firebaseAuth.currentUser } returns mockUser

    // When
    val result = authManager.ensureAuthenticated()

    // Then
    assertTrue(result.isSuccess)
  }

  /**
   * Tests ensureAuthenticated when user is not signed in.
   * Validates that automatic sign-in is triggered and succeeds.
   */
  @Test
  fun `ensureAuthenticated signs in user when not authenticated`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser>(relaxed = true) {
      every { uid } returns "auto-signed-in-user"
    }
    val mockAuthResult = mockk<AuthResult>(relaxed = true) {
      every { user } returns mockUser
    }

    every { firebaseAuth.currentUser } returns null
    every { firebaseAuth.signInAnonymously() } returns Tasks.forResult(mockAuthResult)

    // When
    val result = authManager.ensureAuthenticated()

    // Then
    assertTrue("Expected success but got failure: ${result.exceptionOrNull()}", result.isSuccess)
    verify { firebaseAuth.signInAnonymously() }
  }

  /**
   * Tests ensureAuthenticated when sign-in fails.
   * Validates that authentication failure is properly handled.
   */
  @Test
  fun `ensureAuthenticated returns failure when sign-in fails`() = runTest {
    // Given
    val exception = RuntimeException("Authentication failed")

    every { firebaseAuth.currentUser } returns null
    every { firebaseAuth.signInAnonymously() } returns Tasks.forException(exception)

    // When
    val result = authManager.ensureAuthenticated()

    // Then
    assertTrue(result.isFailure)
    assertNotNull(result.exceptionOrNull())
  }
}

