package com.example.roomdb

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.example.roomdb.ui.theme.ComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SwipeToDismissTest {
    @get:Rule
    val compose = createComposeRule()

    private val cards = mutableStateListOf(CardData(title = "Swipe me"))
    private var removalCount = 0

    private fun showCards() {
        compose.setContent {
            ComposeTheme {
                CardsContainer(
                    cardsList = cards,
                    onRemoveCard = {
                        removalCount++
                        cards.remove(it)
                    }
                )
            }
        }
        compose.waitForIdle()
    }

    private fun cardY() =
        compose.onNodeWithText("Swipe me").fetchSemanticsNode().boundsInRoot.center.y

    private fun checkDismissAfterRelease(toRight: Boolean) {
        showCards()
        val y = cardY()
        val start = if (toRight) 0.1f else 0.9f
        val end = if (toRight) 0.8f else 0.2f

        compose.onRoot().performTouchInput {
            down(Offset(width * start, y))
            moveTo(Offset(width * end, y), delayMillis = 600)
        }
        // Cross the threshold and keep the pointer down across recompositions.
        compose.runOnIdle {
            assertEquals(1, cards.size)
            assertEquals(0, removalCount)
        }
        compose.onRoot().performTouchInput {
            advanceEventTime(500)
            up()
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, cards.size)
            assertEquals(1, removalCount)
        }
    }

    @Test
    fun swipeRightDeletesOnlyAfterRelease() = checkDismissAfterRelease(toRight = true)

    @Test
    fun swipeLeftDeletesOnlyAfterRelease() = checkDismissAfterRelease(toRight = false)

    @Test
    fun reversingSwipeReturnsCardAndAllowsAnotherSwipe() {
        showCards()
        val y = cardY()
        compose.onRoot().performTouchInput {
            down(Offset(width * 0.1f, y))
            moveTo(Offset(width * 0.8f, y), delayMillis = 600)
        }
        compose.runOnIdle { assertEquals(0, removalCount) }
        compose.onRoot().performTouchInput {
            moveTo(Offset(width * 0.2f, y), delayMillis = 600)
            advanceEventTime(500)
            up()
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, cards.size)
            assertEquals(0, removalCount)
        }
        compose.onRoot().performTouchInput {
            swipe(Offset(width * 0.9f, y), Offset(width * 0.1f, y), durationMillis = 600)
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, cards.size)
            assertEquals(1, removalCount)
        }
    }
}
