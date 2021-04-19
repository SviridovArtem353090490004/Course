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

//    private Schedule shuffleDates(Schedule sc){
//        for(int i = 0; i < sc.ships.size(); i++){
//            sc.ships.get(i).
//        }
//        return sc;
//    }


    public Report startModel(Schedule sc, String startDate){




        return null;
    }

}
