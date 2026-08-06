#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "backend"
checks = []

def check(name, ok):
    checks.append((name, bool(ok)))
    print(("PASS" if ok else "FAIL") + ": " + name)

shipment_test = (BACKEND / "services/be-fulfillment-api/src/test/java/com/dnnthanh/marketplace/be/fulfillment/api/domain/model/ShipmentStatusTest.java").read_text()
shipment_enum = (BACKEND / "services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/domain/enumtype/ShipmentStatus.java").read_text()
check("shipment test imports ShipmentStatus", "import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;" in shipment_test)
check("shipment test uses real PICKING/PACKED lifecycle", "ShipmentStatus.PICKING" in shipment_test and "ShipmentStatus.PACKED" in shipment_test)
check("shipment test has no nonexistent PACKING/SHIPPED", "ShipmentStatus.PACKING" not in shipment_test and "ShipmentStatus.SHIPPED" not in shipment_test)
check("shipment lifecycle constants exist", all(name in shipment_enum for name in ["PICKING", "PACKED", "IN_TRANSIT", "DELIVERED"]))

business = (BACKEND / "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/exception/BusinessException.java").read_text()
check("business exception relies on Lombok getter", "public Object[] getMessageArguments()" not in business)
check("business exception serialization metadata remains transient", "transient Object[] messageArguments" in business)

inventory_api = (BACKEND / "services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/api/InventoryPrivateApi.java").read_text()
check("inventory private api imports reserve request", "import com.dnnthanh.marketplace.be.inventory.api.api.request.ReserveInventoryRequest;" in inventory_api)
check("inventory private api imports reservation response", "import com.dnnthanh.marketplace.be.inventory.api.api.response.ReservationResponse;" in inventory_api)

java_files = list(BACKEND.rglob("*.java"))
optional_row_hits = [str(p.relative_to(ROOT)) for p in java_files if ".optionalRow()" in p.read_text(errors="ignore")]
check("no unsupported JdbcClient optionalRow calls", not optional_row_hits)
map_optional = []
for p in java_files:
    text = p.read_text(errors="ignore")
    if "new ColumnMapRowMapper()" in text:
        map_optional.append(p)
check("map-row optional queries use ColumnMapRowMapper", len(map_optional) >= 5 and all(".optional()" in p.read_text(errors="ignore") for p in map_optional))

failed = [name for name, ok in checks if not ok]
print(f"COMPILER_FOLLOWUPS_V7: {len(checks)-len(failed)} passed, {len(failed)} failed")
if failed:
    for name in failed:
        print(" - " + name)
    raise SystemExit(1)
