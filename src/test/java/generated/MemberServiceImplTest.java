import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculatorTest {

    @Mock
    private CalculatorService calculatorService;

    @Test
    public void testAdd() {
        Calculator calculator = new Calculator(calculatorService);
        when(calculatorService.add(2, 3)).thenReturn(5);
        int result = calculator.add(2, 3);
        assertEquals(5, result);
    }

    @Test
    public void testSubtract() {
        Calculator calculator = new Calculator(calculatorService);
        when(calculatorService.subtract(5, 3)).thenReturn(2);
        int result = calculator.subtract(5, 3);
        assertEquals(2, result);
    }

    @Test
    public void testMultiply() {
        Calculator calculator = new Calculator(calculatorService);
        when(calculatorService.multiply(2, 3)).thenReturn(6);
        int result = calculator.multiply(2, 3);
        assertEquals(6, result);
    }

    @Test
    public void testDivide() {
        Calculator calculator = new Calculator(calculatorService);
        when(calculatorService.divide(6, 3)).thenReturn(2);
        int result = calculator.divide(6, 3);
        assertEquals(2, result);
    }
}
