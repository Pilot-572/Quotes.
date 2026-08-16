package xyz.crt572.quotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import xyz.crt572.quotes.data.QuoteDatabase
import xyz.crt572.quotes.ui.QuotesApp
import xyz.crt572.quotes.ui.theme.QuotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuotesTheme {
                val vm: QuotesViewModel = viewModel(factory = viewModelFactory {
                    initializer { QuotesViewModel(QuoteDatabase.get(applicationContext)) }
                })
                QuotesApp(vm)
            }
        }
    }
}
