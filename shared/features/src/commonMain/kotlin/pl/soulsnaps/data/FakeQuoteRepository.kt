package pl.soulsnaps.data

import kotlinx.coroutines.delay
import pl.soulsnaps.domain.QuoteRepository

class FakeQuoteRepository : QuoteRepository {

    private val quotes = listOf(
        "Każdy dzień to nowa szansa, by zacząć od nowa.",
        "Nie musisz być idealny, wystarczy że będziesz sobą.",
        "Wdzięczność zmienia wszystko.",
        "Najlepszy moment na działanie to teraz.",
        "Twoje myśli kształtują Twoją rzeczywistość."
    )

    override suspend fun getQuoteOfTheDay(): String {
        delay(300)
        return quotes.random()
    }
}
