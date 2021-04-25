

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

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
    final int modelingDays = 31;

    private final LinkedBlockingQueue<ShipInfo> liquidQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ShipInfo> dryQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ShipInfo> containerQueue = new LinkedBlockingQueue<>();

    private final int time = 500;

    Thread liquid = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                try{

                System.out.println("liquid size " + liquidQueue.size());
                ShipInfo si = liquidQueue.take();
                System.out.println("take liquid");


                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    Thread container = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                System.out.println("container size " + containerQueue.size());
                ShipInfo si = containerQueue.take();
                System.out.println("take container");

                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    Thread dry = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                System.out.println("dry size " + dryQueue.size());
                ShipInfo si = dryQueue.take();
                System.out.println("take dry");

                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private void shuffleArrivingDatesAndUpload(Schedule sc){
        for(int i = 0; i < sc.ships.size(); i++){
            sc.ships.get(i).addLag(7);
        }
    }

    private ArrayList<ShipInfo> getShipsByDate(ArrayList<ShipInfo> ships, LocalDateTime date){
        ArrayList<ShipInfo> res = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0"));

        for(ShipInfo si : ships){
            LocalDateTime arrDate = LocalDateTime.parse(si.getArrivingDate(), formatter.ofPattern(si.dateFormat, Locale.UK));

            if(arrDate.toLocalDate().equals(date.toLocalDate())){
                res.add(si);
            }

        }

        return res;
    }

    public Report startModel(Schedule sc, LocalDateTime startDate) throws InterruptedException {
        shuffleArrivingDatesAndUpload(sc);

        container.start();
        liquid.start();
        dry.start();


        for(int i = 0; i < modelingDays; i++){
            ArrayList<ShipInfo> arrivingShips = getShipsByDate(sc.ships, startDate);
            System.out.println(startDate);
            System.out.println("==========================");
            for(ShipInfo si : arrivingShips){
                switch (si.getCargoType()) {
                    case CONTAINER:
                        System.out.println("put container");
                        containerQueue.put(si);
                        break;
                    case LIQUID:
                        System.out.println("put liquid");
                        liquidQueue.put(si);
                        break;
                    case DRY:
                        System.out.println("put dry");
                        dryQueue.put(si);
                        break;
                }
            }
            startDate = startDate.plusDays(1);

        }

        return null;
    }

}
