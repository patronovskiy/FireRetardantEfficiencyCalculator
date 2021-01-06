import domain.EntityCounter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

//класс для тестирования счетчика строк в таблице
public class EntityCounterTest {

    @Test
    public void testEntityCounter() {
        EntityCounter entityCounter = new EntityCounter();
        assertEquals(1, entityCounter.getAndIncrementCounter());
        entityCounter.clear();
        for (int i = 0; i < 10; i++) {
            entityCounter.getAndIncrementCounter();
        }
        assertEquals(11, entityCounter.getAndIncrementCounter());
        entityCounter.setCounter(0);
        assertEquals(0, entityCounter.getAndIncrementCounter());
    }
}
