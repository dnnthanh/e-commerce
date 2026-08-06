import com.dnnthanh.marketplace.be.search.api.application.query.SearchCursor;
import java.util.List;

/** Framework-free RED/GREEN smoke for stable search-after cursor encoding. */
public final class SearchCursorSmoke {
  private SearchCursorSmoke() {}

  public static void main(String[] args) {
    String cursor = SearchCursor.encode(List.of("12.50", "product-42"));
    require(!cursor.contains("12.50"), "cursor should be transport-safe encoded data");
    require(SearchCursor.decode(cursor).equals(List.of("12.50", "product-42")), "cursor must round-trip");
    try {
      SearchCursor.decode("not-base64!");
      throw new AssertionError("invalid cursor must be rejected");
    } catch (RuntimeException expected) {
      // expected
    }
    System.out.println("SEARCH_CURSOR_SMOKE=PASS");
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
