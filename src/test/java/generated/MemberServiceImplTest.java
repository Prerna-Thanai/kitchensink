import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculatorTest {

    @Mock
    private MathService mathService;

    @InjectMocks
    private Calculator calculator;

    @Test
    public void testAdd() {
        when(mathService.add(2, 3)).thenReturn(5);
        int result = calculator.add(2, 3);
        assertEquals(5, result);
    }

    @Test
    public void testSubtract() {
        when(mathService.subtract(5, 3)).thenReturn(2);
        int result = calculator.subtract(5, 3);
        assertEquals(2, result);
    }

    @Test
    public void testMultiply() {
        when(mathService.multiply(2, 3)).thenReturn(6);
        int result = calculator.multiply(2, 3);
        assertEquals(6, result);
    }

    @Test
    public void testDivide() {
        when(mathService.divide(6, 3)).thenReturn(2);
        int result = calculator.divide(6, 3);
        assertEquals(2, result);
    }
}
