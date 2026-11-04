package com.example.private24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.private24.ui.theme.ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CurrencyRatesScreen(
                        rates = "",
                        onLoadRates = {},
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyRatesScreen(
    rates: String,
    onLoadRates: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = rates,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.currency_rates)) },
            placeholder = { Text(stringResource(R.string.currency_rates_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Button(
            onClick = onLoadRates,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.load_currency_rates))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyRatesPreview() {
    ComposeTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CurrencyRatesScreen(
                rates = "",
                onLoadRates = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
