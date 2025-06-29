import com.rs.groww.model.Stock
import com.rs.groww.model.Watchlist

object DummyData {
    val watchlists = listOf<Watchlist>()
    fun Double.format(digits: Int) = "%.${digits}f".format(this)
}
