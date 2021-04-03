import java.time.Instant;
import java.util.ArrayList;

class UnloadingReport{
    String name;
    Instant arrivingDateTime;
    long waitingTime; //format dd:hh:mm
    Instant unloadingStart;
    long unloadingDuration;
}

class Statistics{
    long countUnloadedShips;
    long averageQueueLength;
    long averageWaitingTime;
    long maxUnloadingDelay;
    long averageUnloadingDelay;
    long penaltyChargeSum;
    long countLiquidLift;
    long countContainerLift;
    long countDryLift;
}

class Report {
    ArrayList<UnloadingReport> unloadingList;
    Statistics statistics;
}

//Service 3
public class DockModel {
    final int penaltyChargePerHour = 100;
    final int liftCost = 30000;


    public String startModel(Schedule sc, Instant startDate){



        return "";
    }

}
