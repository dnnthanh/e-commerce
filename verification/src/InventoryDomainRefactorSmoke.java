import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.InventoryBalance;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.Reservation;
import java.time.LocalDateTime;

/** Framework-free regression smoke for the typed inventory domain contract. */
public final class InventoryDomainRefactorSmoke {
  public static void main(String[] args) {
    Reservation reservation = new Reservation(
        "R-1", 10L, 20L, 2, ReservationStatus.RESERVED, LocalDateTime.now().plusMinutes(5));
    if (reservation.status() != ReservationStatus.RESERVED) {
      throw new AssertionError("reservation status must be typed");
    }

    InventoryBalance balance = new InventoryBalance(10L, 20L, 5, 0, 0);
    assertInvalid(() -> balance.reserve(0));
    assertInvalid(() -> balance.release(1));
    System.out.println("INVENTORY_DOMAIN_REFACTOR_SMOKE=PASS");
  }

  private static void assertInvalid(Runnable action) {
    try {
      action.run();
    } catch (InvalidInventoryMutationException expected) {
      return;
    }
    throw new AssertionError("typed InvalidInventoryMutationException expected");
  }
}
