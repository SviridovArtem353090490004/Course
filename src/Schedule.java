import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

enum CargoType {
    LIQUID,
    CONTAINER,
    DRY;

    private static final List<CargoType> VALUES = Arrays.asList(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static CargoType random()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

};


public class Schedule {
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
