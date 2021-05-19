package dock;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.Month;

public class Program {
    public static void main(String[] args) throws InterruptedException {


        String json = new JsonGeneratorAndPrinter().generateSchedule(150, "2021-04-02"); //генерируем json расписание на 150 кораблей начиная с даты 2021-04-02

        Schedule sc = new Gson().fromJson(json, Schedule.class); //создаем объект класса расписание и даем ему json файл

        DockModel dm = new DockModel(100, 30000, 31); //создаем модель работы порта

        Report r = dm.startModel(sc, LocalDateTime.of(2021, Month.APRIL, 2, 1, 1, 1)); //стартуем модель работы порта

        System.out.println(new Gson().toJson(r)); //выводим json файл с расписанием

        System.exit(0); //заканчиваем программу
    }
}
