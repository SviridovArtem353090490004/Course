

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum CargoType {
    LIQUID("Liquid"),
    CONTAINER("Container"),
    DRY("Dry");

    private static final List<CargoType> VALUES = Arrays.asList(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    String displayName;

    CargoType(String s) {
        this.displayName = s;
    }

    public static CargoType random()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

    public String getDisplayName() {
        return displayName;
    }
}


class Schedule {
    private Instant arrivingDate;
    private String name;
    private long weight;
    private CargoType cargoType;
    private long waitingDays;

    public Schedule(Instant arrivingDate, String name, long weight,
                    CargoType cargoType, long waitingDays) {
        this.arrivingDate = arrivingDate;
        this.name = name;
        this.weight = weight;
        this.cargoType = cargoType;
        this.waitingDays = waitingDays;
    }

    public Instant getArrivingDate() {
        return arrivingDate;
    }

    public String getName() {
        return name;
    }

    public long getWeight() {
        return weight;
    }

    public long getWaitingDays() {
        return waitingDays;
    }

    public void print(){
        System.out.println("Arriving date: " + arrivingDate);
        System.out.println("Name: " + name);
        if(cargoType != CargoType.CONTAINER) {
            System.out.println("Weight (kg): " + weight);
        }else{
            System.out.println("Units (pcs): " + weight);
        }
        System.out.println("Cargo type: " + cargoType.name());
        System.out.println("Waiting days: " + waitingDays);
    }
}



//Service 1
public class ScheduleGenerator {

    private Instant startInclusive;
    private Instant endExclusive;

    private long capacityLiquidKgPerHour;
    private long capacityDryKgPerHour;
    private long capacityContainerUnitPerHour;

    private Instant between(Instant start) {
        long startSeconds = start.getEpochSecond();
        long endSeconds = endExclusive.getEpochSecond();
        long random = ThreadLocalRandom
                .current()
                .nextLong(startSeconds, endSeconds);

        return Instant.ofEpochSecond(random);
    }

    private String getRandomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public ScheduleGenerator(long capacityContainerUnitPerHour, long capacityDryKgPerHour, long capacityLiquidKgPerHour){
        String startDateStr="2000-01-01T00:00:00.000Z";
        startInclusive = Instant.parse(startDateStr);
        endExclusive = Instant.now();

        this.capacityContainerUnitPerHour = capacityContainerUnitPerHour;
        this.capacityLiquidKgPerHour = capacityLiquidKgPerHour;
        this.capacityDryKgPerHour = capacityDryKgPerHour;

    }

    public Schedule generate(){
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Instant arriving_date = between(startInclusive);
        CargoType type = CargoType.random();
        long weightKg;
        if(type == CargoType.CONTAINER) {
            weightKg = rnd.nextLong(10, 1000);
        }else{
            weightKg = rnd.nextLong(100, 100000);
        }

        long days = getHours(type, weightKg);

        return new Schedule(
                arriving_date,
                getRandomString(),
                weightKg,
                type,
                days
        );
    }

    private long getHours(CargoType type, long weight) {
        long hours = -1;
        if(type == CargoType.DRY) {
            hours = weight / capacityDryKgPerHour;
            if(weight % capacityDryKgPerHour != 0){
                hours++;
            }
        }
        else {
            if (type == CargoType.LIQUID) {
                hours = weight / capacityLiquidKgPerHour;
                if (weight % capacityLiquidKgPerHour != 0) {
                    hours++;
                }
            }
            else {
                if (type == CargoType.CONTAINER) {
                    hours = weight / capacityContainerUnitPerHour;
                    if (weight % capacityContainerUnitPerHour != 0) {
                        hours++;
                    }
                }
            }
        }
        return hours;
    }
}
