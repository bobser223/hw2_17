package com.example.private24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.private24.ui.theme.ComposeTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExchangeArchiveRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ExchangeArchiveRoute(
    modifier: Modifier = Modifier,
    viewModel: ExchangeArchiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExchangeArchiveScreen(
        uiState = uiState,
        onDateChange = viewModel::onDateChange,
        onLoadRates = viewModel::loadRates,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        modifier = modifier
    )
}

@Composable
fun ExchangeArchiveScreen(
    uiState: ExchangeArchiveUiState,
    onDateChange: (String) -> Unit,
    onLoadRates: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.dateInput,
            onValueChange = onDateChange,
            enabled = !uiState.isLoading,
            singleLine = true,
            isError = uiState.dateError != null,
            label = { Text(stringResource(R.string.exchange_date_label)) },
            placeholder = { Text(stringResource(R.string.exchange_date_placeholder)) },
            supportingText = uiState.dateError?.let { error -> { Text(error) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onLoadRates() }),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onLoadRates,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.exchange_show_rates))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val request = uiState.request) {
                ExchangeArchiveRequestState.Idle ->
                    StatusMessage(stringResource(R.string.exchange_idle_hint))

                ExchangeArchiveRequestState.Loading -> CircularProgressIndicator()

                is ExchangeArchiveRequestState.Success ->
                    if (request.rates.isEmpty()) {
                        StatusMessage(stringResource(R.string.exchange_empty, request.date))
                    } else {
                        ExchangeRatesList(
                            success = request,
                            rates = uiState.visibleRates,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = onSearchQueryChange
                        )
                    }

                is ExchangeArchiveRequestState.Error ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = request.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onLoadRates) {
                            Text(stringResource(R.string.exchange_retry))
                        }
                    }
            }
        }
    }
}

@Composable
private fun StatusMessage(text: String) {
    Text(text = text, textAlign = TextAlign.Center)
}

@Composable
private fun ExchangeRatesList(
    success: ExchangeArchiveRequestState.Success,
    rates: List<ExchangeRate>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                R.string.exchange_rates_header,
                success.date,
                success.baseCurrency
            ),
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            label = { Text(stringResource(R.string.exchange_search_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        if (rates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                StatusMessage(stringResource(R.string.exchange_search_empty, searchQuery.trim()))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rates, key = { it.currency.orEmpty() }) { rate ->
                    ExchangeRateItem(rate = rate, defaultBaseCurrency = success.baseCurrency)
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateItem(rate: ExchangeRate, defaultBaseCurrency: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = rate.currency.orEmpty(),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(
                    R.string.exchange_base_currency,
                    rate.baseCurrency ?: defaultBaseCurrency
                ),
                style = MaterialTheme.typography.bodySmall
            )
            RateRow(
                label = stringResource(R.string.exchange_bank_buys),
                value = rate.purchaseRate
            )
            RateRow(
                label = stringResource(R.string.exchange_bank_sells),
                value = rate.saleRate
            )
        }
    }
}

@Composable
private fun RateRow(label: String, value: Double?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            value?.let { String.format(Locale.getDefault(), "%.4f", it) }
                ?: stringResource(R.string.exchange_no_rate)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExchangeArchivePreview() {
    ComposeTheme {
        ExchangeArchiveScreen(
            uiState = ExchangeArchiveUiState(
                dateInput = "01.11.2026",
                request = ExchangeArchiveRequestState.Success(
                    date = "01.11.2026",
                    baseCurrency = "UAH",
                    rates = listOf(
                        ExchangeRate(
                            baseCurrency = "UAH",
                            currency = "USD",
                            saleRateNB = 41.5,
                            purchaseRateNB = 41.5,
                            saleRate = 41.9,
                            purchaseRate = 41.3
                        ),
                        ExchangeRate(
                            baseCurrency = "UAH",
                            currency = "EUR",
                            saleRateNB = 45.1,
                            purchaseRateNB = 45.1
                        )
                    )
                )
            ),
            onDateChange = {},
            onLoadRates = {},
            onSearchQueryChange = {}
        )
    }
}
