package domain;

/**
 * @author patronovskiy
 * @link https://github.com/patronovskiy
 */

//класс, описывающий сущность "строка таблицы"
public class TableEntity {
    //ПОЛЯ
    private int number;         //номер строки
    private String channel;     //название канала
    private double minutes;     //время с начала испытания, мин
    private double temperature; //температура на канале, °С

    //КОНСТРУКТОРЫ
    public TableEntity(int number, String channel, double minutes, double temperature) {
        this.number = number;
        this.channel = channel;
        this.minutes = minutes;
        this.temperature = temperature;
    }

    //ГЕТТЕРЫ И СЕТТЕРЫ
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public double getMinutes() {
        return minutes;
    }

    public void setMinutes(double minutes) {
        this.minutes = minutes;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
