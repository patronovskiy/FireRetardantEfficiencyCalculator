package domain;

//вспомогательный класс для подсчета строк в таблице температур
public class EntityCounter {
    //номер текущей строки (счетчик)
    private  int counter = 1;

    //геттер, возвращающий номер строки и увеличивающий счетчик при этом на 1
    public int getAndIncrementCounter() {
        return counter++;
    }

    //сеттер счтечика
    public void setCounter(int counter) {
        this.counter = counter;
    }

    //метод для "отчистки" - устанавливает значение счетчика 1
    public void clear() {
        this.counter = 1;
    }
}


