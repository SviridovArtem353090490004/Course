import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ScheduleGenerator {

    private Instant startInclusive;
    private Instant endExclusive;

    private long capacityLiquidKgPerDay;
    private long capacityDryKgPerDay;
    private long capacityContainerUnitPerDay;

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

    public ScheduleGenerator(long capacityContainerUnitPerDay, long capacityDryKgPerDay, long capacityLiquidKgPerDay){
        String startDateStr="2000-01-01T00:00:00.000Z";
        startInclusive = Instant.parse(startDateStr);
        endExclusive = Instant.now();

        this.capacityContainerUnitPerDay = capacityContainerUnitPerDay;
        this.capacityLiquidKgPerDay = capacityLiquidKgPerDay;
        this.capacityDryKgPerDay = capacityDryKgPerDay;

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

        long days = getDays(type, weightKg);

        return new Schedule(
                arriving_date,
                getRandomString(),
                weightKg,
                type,
                days
        );
    }

    private long getDays(CargoType type, long weight) {
        long days = -1;
        if(type == CargoType.DRY) {
            days = weight / capacityDryKgPerDay;
            if(weight % capacityDryKgPerDay != 0){
                days++;
            }
        }
        else {
            if (type == CargoType.LIQUID) {
                days = weight / capacityLiquidKgPerDay;
                if (weight % capacityLiquidKgPerDay != 0) {
                    days++;
                }
            }
            else {
                if (type == CargoType.CONTAINER) {
                    days = weight / capacityContainerUnitPerDay;
                    if (weight % capacityContainerUnitPerDay != 0) {
                        days++;
                    }
                }
            }
        }
        return days;
    }
}
