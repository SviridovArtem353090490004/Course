


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


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

    public static CargoType random() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

    public String getDisplayName() {
        return displayName;
    }
}


class ShipInfo {
    private String arrivingDate;
    private String name;
    private long weight;
    private CargoType cargoType;
    private long uploadHours;
    private long uploadLag;
    public final String dateFormat = "d MMM yyyy, HH:mm:ss";


    public ShipInfo(Instant arrivingDate, String name, long weight,
                    CargoType cargoType, long uploadHours) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0"));
        this.arrivingDate = formatter.format(arrivingDate);
        this.name = name;
        this.weight = weight;
        this.cargoType = cargoType;
        this.uploadHours = uploadHours;
        this.uploadLag = 0;
    }

    public String getArrivingDate() {
        return arrivingDate;
    }

    public void addLag(int rangeDays){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0"));
        LocalDateTime date = LocalDateTime.parse(arrivingDate, formatter.ofPattern(this.dateFormat, Locale.UK));
        Random rnd = new Random();
        if(rnd.nextInt(100) < Config.possibilityOfShipLags ){
            //Ship will lag
            if(rnd.nextInt(2) == 0){
                date = date.minusDays(rnd.nextInt(rangeDays));
            }else{
                date = date.plusDays(rnd.nextInt(rangeDays));
            }
        }
        Instant in = date.toInstant(ZoneOffset.UTC);

        arrivingDate = formatter.format(in);

        if(rnd.nextInt(100) < Config.possibilityOfUploadLags){
            //Ship will lag on unload
            this.uploadLag = rnd.nextInt(1440);
        }


    }

    public String getName() {
        return name;
    }

    public long getWeight() {
        return weight;
    }

    public long getUploadHours() {
        return uploadHours;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    public long getUploadLag() {
        return uploadLag;
    }

    public void print() {
        System.out.println("Arriving date: " + arrivingDate);
        System.out.println("Name: " + name);
        if (cargoType != CargoType.CONTAINER) {
            System.out.println("Weight (kg): " + weight);
        } else {
            System.out.println("Units (pcs): " + weight);
        }
        System.out.println("Cargo type: " + cargoType.name());
        System.out.println("Waiting days: " + uploadHours);
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

    public ScheduleGenerator(long capacityContainerUnitPerHour, long capacityDryKgPerHour, long capacityLiquidKgPerHour, String startDate) {

        startInclusive = LocalDate.parse(startDate).atStartOfDay(ZoneId.of("UTC+0")).toInstant();
        endExclusive = Instant.now();

        this.capacityContainerUnitPerHour = capacityContainerUnitPerHour;
        this.capacityLiquidKgPerHour = capacityLiquidKgPerHour;
        this.capacityDryKgPerHour = capacityDryKgPerHour;

    }

    public ShipInfo generateShipInfo() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Instant arriving_date = between(startInclusive);
        CargoType type = CargoType.random();
        long weightKg;
        if (type == CargoType.CONTAINER) {
            weightKg = rnd.nextLong(10, 1000);
        } else {
            weightKg = rnd.nextLong(100, 100000);
        }

        long days = getHours(type, weightKg);

        return new ShipInfo(
                arriving_date,
                getRandomString(),
                weightKg,
                type,
                days
        );
    }

    private long getHours(CargoType type, long weight) {
        long hours = -1;
        if (type == CargoType.DRY) {
            hours = weight / capacityDryKgPerHour;
            if (weight % capacityDryKgPerHour != 0) {
                hours++;
            }
        } else {
            if (type == CargoType.LIQUID) {
                hours = weight / capacityLiquidKgPerHour;
                if (weight % capacityLiquidKgPerHour != 0) {
                    hours++;
                }
            } else {
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




//    Arriving date: 3 Mar 2021, 17:47:52
//        Name: 7VuPcB4iFx
//        Weight (kg): 61173
//        Cargo type: DRY
//        Waiting days: 102
//
//        Arriving date: 5 Mar 2021, 00:00:00
//        Name: 7VuPcB4iFx
//        Weight (kg): 61173
//        Cargo type: DRY
//        Waiting days: 102