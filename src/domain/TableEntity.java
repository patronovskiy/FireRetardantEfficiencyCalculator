package domain;

import java.util.Comparator;

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

    //компаратор для сравнения и сортировки строк таблицы
    public static Comparator<TableEntity> sortByMinutes = new Comparator<TableEntity>() {
        @Override
        public int compare(TableEntity o1, TableEntity o2) {
            if(o1.getMinutes() < o2.getMinutes()) {
                return -1;
            } else if (o1.getMinutes() > o2.getMinutes()) {
                return 1;
            }
            return 0;
        }
    };
}
