package com.example.roomdb

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.roomdb.ui.theme.ComposeTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CardData(
    val title: String = "Заголовок картки",
    val description: String = "Це дуже детальний опис для цієї картки, який з'являється після того, як ви натиснете кнопку розгортання. Тут може бути багато тексту та іншої корисної інформації.",
    val creationTime: String = "2023-10-27 15:30"
)

class MainActivity : ComponentActivity() {

    private val cards = mutableStateListOf<CardData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CardsContainer(
                        modifier = Modifier.padding(innerPadding),
                        cardsList = cards,
                        onAddCard = {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                            val currentTime = LocalDateTime.now().format(formatter)
                            cards.add(
                                CardData(
                                    title = "Title ${cards.size + 1}",
                                    creationTime = currentTime
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardsContainer(
    modifier: Modifier = Modifier,
    cardsList: List<CardData>,
    onAddCard: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(cardsList.size) {
        if (cardsList.isNotEmpty()) {
            listState.animateScrollToItem(cardsList.size - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(cardsList) { card ->
                ExpandableCard(cardData = card)
            }
        }
        Button(
            onClick = onAddCard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add card")
        }
    }
}

@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    cardData: CardData
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = cardData.title,
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Згорнути" else "Розгорнути")
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = cardData.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Створено: ${cardData.creationTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun ExpandableCardPreview() {
    ComposeTheme {
        CardsContainer(
            modifier = Modifier,
            cardsList = listOf(
                CardData(title = "Title 1"),
                CardData(title = "Title 2"),
                CardData(title = "Title 3"),
                CardData(title = "Title 4"),
            )
        )
    }
}
