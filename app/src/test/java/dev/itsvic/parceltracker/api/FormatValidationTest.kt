import dev.itsvic.parceltracker.api.PolishPostDeliveryService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatValidationTest {
    @Test fun polishPost_PocztexFormatReturnsTrue() {
        assertTrue(PolishPostDeliveryService.acceptsFormat("PX1234567890"))
    }
    @Test fun polishPost_CommonFormatReturnsTrue() {
        assertTrue(PolishPostDeliveryService.acceptsFormat("NL123456789PL"))
    }
    @Test fun polishPost_NonsenseReturnsFalse() {
        assertFalse(PolishPostDeliveryService.acceptsFormat("fsjkdkjjksd"))
    }
}
