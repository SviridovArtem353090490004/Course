package dock;

import com.google.gson.Gson;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

//Service 2
//генератор json и принтер
public class JsonGeneratorAndPrinter {
    //генератор json
    private String generateJson(Schedule sc) {
        return new Gson().toJson(sc);
    }

    //генератор расписания
    public String generateSchedule(int count, String startDate) {
        Schedule sc = new Schedule(); //переменная расписания
//создаем генератор
        ScheduleGenerator gnr = new ScheduleGenerator(
                Config.capacityContainerUnitPerHour,
                Config.capacityDryKgPerHour,
                Config.capacityLiquidKgPerHour,
                startDate);

        for (int i = 0; i < count; i++) { //цикл по кол-ву кораблей
            sc.addShip(gnr.generateShipInfo()); //генерация информации о корабле
        }

        return generateJson(sc);
    }

    //генератор расписания из консоли
    public String generateScheduleFromConsole(int count) {
        Schedule schedule = new Schedule(); //переменная для расписания
        for (int j = 0; j < count; j++) { //цикл по кол-ву кораблей

            Instant arriving_date; //переменная даты прибытия корабля
            String name; //имя корабля
            long weightKg; //вес груза
            CargoType type; //тип груза
            long days; //длительность стоянки

            Scanner sc = new Scanner(System.in); //переменная потока ввода из консоли
            arriving_date = null;
            while (true) { //бесконечный цикл
                try { //блок try
                    System.out.println("Input arriving datetime (ex. 2010-04-15T03:57:03Z): "); //вывод примера формата даты
                    arriving_date = Instant.parse(sc.next()); //считывание даты
                    break;
                } catch (DateTimeParseException e) { //ловля исключения
                    System.out.println("Wrong date time format. Try Again!"); // вывод информации о вводе неправильного формата даты
                }
            }

            System.out.println("Input name: "); // запрос имени корбаля
            name = sc.next(); //чтение имени из консоли

            System.out.println("Input weight (kg): "); // запрос веса груза
            weightKg = sc.nextLong(); //чтение веса из консоли

            while (true) { //бесконечный цикл
                System.out.println("Choose cargo type: "); //выбор типа груза
                System.out.println("1. Liquid"); //жидкий груз
                System.out.println("2. Container"); //контейнеры
                System.out.println("3. Dry"); //сухой груз
                int i = sc.nextInt() - 1;
                if (i < 0 || i >= CargoType.values().length) { //если выбор не уданый
                    System.out.println("Wrong number! Try Again!"); //вывод уведомления о неправильном выборе типа груза
                    continue;
                }

                type = CargoType.values()[i]; //чтение типа груза из консоли
                break;
            }

            System.out.println("Input Days"); //запрос длительности стоянки
            days = sc.nextLong(); //чтение длительности стоянки из консоли

            schedule.addShip(new ShipInfo(arriving_date, name, weightKg, type, days)); //добавить корабли в расписание
        }
        return generateJson(schedule);

    }

}
