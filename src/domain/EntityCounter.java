package domain;

public class EntityCounter {
    private  int counter = 1;

    public int getAndIncrementCounter() {
        return counter++;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void clear() {
        this.counter = 1;
    }
}


