import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CalculatorTest {

    @Mock
    private CalculatorService calculatorService;

    @InjectMocks
    private Calculator calculator;

    @Test
    public void testAdd() {
        when(calculatorService.add(5, 3)).thenReturn(8);
        int result = calculator.add(5, 3);
        assertEquals(8, result);
    }

    @Test
    public void testSubtract() {
        when(calculatorService.subtract(8, 3)).thenReturn(5);
        int result = calculator.subtract(8, 3);
        assertEquals(5, result);
    }

    @Test
    public void testMultiply() {
        when(calculatorService.multiply(4, 2)).thenReturn(8);
        int result = calculator.multiply(4, 2);
        assertEquals(8, result);
    }

    @Test
    public void testDivide() {
        when(calculatorService.divide(10, 2)).thenReturn(5);
        int result = calculator.divide(10, 2);
        assertEquals(5, result);
    }
}
