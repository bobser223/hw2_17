package com.example.private24

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

sealed interface ExchangeArchiveRequestState {
    object Idle : ExchangeArchiveRequestState
    object Loading : ExchangeArchiveRequestState
    data class Success(
        val date: String,
        val baseCurrency: String,
        val rates: List<ExchangeRate>
    ) : ExchangeArchiveRequestState
    data class Error(val message: String) : ExchangeArchiveRequestState
}

data class ExchangeArchiveUiState(
    val dateInput: String = "",
    val dateError: String? = null,
    val searchQuery: String = "",
    val request: ExchangeArchiveRequestState = ExchangeArchiveRequestState.Idle
) {
    val isLoading: Boolean
        get() = request is ExchangeArchiveRequestState.Loading

    val visibleRates: List<ExchangeRate>
        get() = (request as? ExchangeArchiveRequestState.Success)?.rates.orEmpty()
            .filter { it.currency.orEmpty().contains(searchQuery.trim(), ignoreCase = true) }
            .sortedBy { it.currency }
}

class ExchangeArchiveViewModel : ViewModel() {

    private val networkModule = PrivatBankNetworkModule()

    private val _uiState = MutableStateFlow(ExchangeArchiveUiState())
    val uiState: StateFlow<ExchangeArchiveUiState> = _uiState.asStateFlow()

    private var currentCall: Call<ExchangeArchiveResponse>? = null

    fun onDateChange(input: String) {
        if (_uiState.value.isLoading) return

        val filtered = input.filter { it.isDigit() || it == '.' }.take(DATE_LENGTH)
        _uiState.update { it.copy(dateInput = filtered, dateError = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadRates() {
        val state = _uiState.value
        if (state.isLoading) return

        val date = parseDate(state.dateInput)
        if (date == null) {
            _uiState.update {
                it.copy(dateError = "Введіть існуючу дату у форматі ДД.ММ.РРРР.")
            }
            return
        }
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(dateError = "Дата не може бути в майбутньому.") }
            return
        }
        if (date.isBefore(LocalDate.now().minusYears(ARCHIVE_YEARS))) {
            _uiState.update {
                it.copy(dateError = "Архів доступний лише за останні $ARCHIVE_YEARS роки.")
            }
            return
        }

        _uiState.update {
            it.copy(dateError = null, request = ExchangeArchiveRequestState.Loading)
        }

        Log.d(TAG, "Запит курсів на ${date.format(DATE_FORMAT)}")
        val call = networkModule.service.getCurrencyExchange(date.format(DATE_FORMAT))
        currentCall = call

        call.enqueue(object : Callback<ExchangeArchiveResponse> {
            override fun onResponse(
                call: Call<ExchangeArchiveResponse>,
                response: Response<ExchangeArchiveResponse>
            ) {
                if (call !== currentCall || call.isCanceled) return

                val body = response.body()
                val request = when {
                    !response.isSuccessful -> ExchangeArchiveRequestState.Error(
                        "Сервер повернув HTTP ${response.code()}. Спробуйте пізніше."
                    )
                    body == null -> ExchangeArchiveRequestState.Error(
                        "Банк повернув відповідь без даних."
                    )
                    else -> ExchangeArchiveRequestState.Success(
                        date = body.date ?: date.format(DATE_FORMAT),
                        baseCurrency = body.baseCurrencyLit.orEmpty(),
                        rates = body.exchangeRate.orEmpty()
                            .filter { !it.currency.isNullOrBlank() }
                    )
                }
                _uiState.update { it.copy(request = request) }
            }

            override fun onFailure(call: Call<ExchangeArchiveResponse>, throwable: Throwable) {
                if (call !== currentCall || call.isCanceled) return

                _uiState.update {
                    it.copy(
                        request = ExchangeArchiveRequestState.Error(
                            "Не вдалося завантажити курси. Перевірте з'єднання та спробуйте знову."
                        )
                    )
                }
            }
        })
    }

    override fun onCleared() {
        currentCall?.cancel()
        super.onCleared()
    }

    private fun parseDate(input: String): LocalDate? =
        try {
            LocalDate.parse(input, DATE_FORMAT)
        } catch (e: DateTimeParseException) {
            null
        }

    private companion object {
        const val TAG = "ExchangeArchiveViewModel"
        const val DATE_LENGTH = 10
        const val ARCHIVE_YEARS = 4L

        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
            .withResolverStyle(ResolverStyle.STRICT)
    }
}
