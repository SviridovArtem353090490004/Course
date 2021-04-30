import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Random;

public class Program {
    public static void main(String[] args) throws InterruptedException {


        String json = new JsonGeneratorAndPrinter().generateSchedule(10, "2021-04-02");

        Schedule sc = new Gson().fromJson(json, Schedule.class);

        DockModel dm = new DockModel();
        ScheduleGenerator gnr = new ScheduleGenerator(
                Config.capacityContainerUnitPerHour,
                Config.capacityDryKgPerHour,
                Config.capacityLiquidKgPerHour,
                "2021-04-02");
        ShipInfo si = gnr.generateShipInfo();
        si.enterQueue = LocalDateTime.of(2021, Month.APRIL, 30, 1, 1,1 );
        si.cargoType = CargoType.LIQUID;
        si.name = "Hello";
        sc.ships.add(si);

        dm.startModel(sc, LocalDateTime.of(2021, Month.APRIL, 2, 1, 1,1 ));

    }
}
