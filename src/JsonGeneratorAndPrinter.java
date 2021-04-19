
import com.google.gson.Gson;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//Service 2
public class JsonGeneratorAndPrinter {

    private String generateJson(Schedule sc) {
        return new Gson().toJson(sc);
    }

    public String generateSchedule(int count) {
        Schedule sc = new Schedule();

        ScheduleGenerator gnr = new ScheduleGenerator(
                Config.capacityContainerUnitPerHour,
                Config.capacityDryKgPerHour,
                Config.capacityLiquidKgPerHour,
                "2021-03-01");

        for (int i = 0; i < count; i++) {
            sc.addShip(gnr.generateShipInfo());
        }

        return generateJson(sc);
    }

    public String generateScheduleFromConsole(int count) {
        Schedule schedule = new Schedule();
        for (int j = 0; j < count; j++) {

            Instant arriving_date;
            String name;
            long weightKg;
            CargoType type;
            long days;

            Scanner sc = new Scanner(System.in);
            arriving_date = null;
            while (true) {
                try {
                    System.out.println("Input arriving datetime (ex. 2010-04-15T03:57:03Z): ");
                    arriving_date = Instant.parse(sc.next());
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Wrong date time format. Try Again!");
                }
            }

            System.out.println("Input name: ");
            name = sc.next();

            System.out.println("Input weight (kg): ");
            weightKg = sc.nextLong();

            while (true) {
                System.out.println("Choose cargo type: ");
                System.out.println("1. Liquid");
                System.out.println("2. Container");
                System.out.println("3. Dry");
                int i = sc.nextInt() - 1;
                if (i < 0 || i >= CargoType.values().length) {
                    System.out.println("Wrong number! Try Again!");
                    continue;
                }

                type = CargoType.values()[i];
                break;
            }

            System.out.println("Input Days");
            days = sc.nextLong();

            schedule.addShip(new ShipInfo(arriving_date, name, weightKg, type, days));
        }
        return generateJson(schedule);

    }

}
