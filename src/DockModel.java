import com.google.gson.Gson;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


class UnloadingReport {
    String name;
    Instant arrivingDateTime;
    long waitingTime; //format dd:hh:mm
    Instant unloadingStart;
    long unloadingDuration;
}

class Statistics {
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


class UploadThread implements Runnable {
    private LinkedBlockingQueue<ShipInfo> queue;
    private String name;
    private LocalDateTime startDate;
    private long speed;
    private Random rnd;
    private boolean running;

    public UploadThread(LinkedBlockingQueue<ShipInfo> queue, String name, LocalDateTime startDate, long speed) {
        this.queue = queue;
        this.name = name;
        this.startDate = startDate;
        this.speed = speed;
        this.running = true;
        this.rnd = new Random();
    }

    @Override
    public void run() {
        LocalDateTime tmp = startDate;
        while (running) {
            try {
                ShipInfo si = queue.take();

//                if(si != null) {
//                    System.out.println(this.name + " " + si.name + "Ship queue time " + si.enterQueue);
                    if (tmp.isBefore(si.enterQueue)) {
//                        System.out.println(this.name + " " + si.name + " tmp was " + tmp);
                        tmp = si.enterQueue;
                    }

                    si.startUnload = tmp;
//                    System.out.println(this.name + " " + si.name + " startunload " + tmp + " weight" + si.getWeight());
                    long hours = si.getWeight() / speed + si.getUploadLag();
//                    System.out.println(this.name + " " + si.name + "Unload hours " + hours + " lags " + si.getUploadLag());
                    tmp = tmp.plusHours(hours);
//                    System.out.println(this.name + " " + si.name + " endunload " + tmp);

                    si.endUnload = tmp;
                    Thread.sleep(hours);
//                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

//Service 3
public class DockModel {
    final int penaltyChargePerHour = 100;
    final int liftCost = 30000;
    final int modelingDays = 31;

    private int countLiquidCrane;
    private int countContainerCrane;
    private int countDryCrane;


    public DockModel() {
        countLiquidCrane = 1;
        countContainerCrane = 1;
        countDryCrane = 1;
    }

    private void shuffleArrivingDatesAndUpload(Schedule sc) {
        for (int i = 0; i < sc.ships.size(); i++) {
            sc.ships.get(i).addLag(7);
        }
    }

    private ArrayList<ShipInfo> getShipsByDate(ArrayList<ShipInfo> ships, LocalDateTime date) {
        ArrayList<ShipInfo> res = new ArrayList<>();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0"));

        for (ShipInfo si : ships) {
            LocalDateTime arrDate = LocalDateTime.parse(si.getArrivingDate(), formatter.ofPattern(si.dateFormat, Locale.UK));

            if (arrDate.toLocalDate().equals(date.toLocalDate())) {
                res.add(si);
            }

        }

        return res;
    }

    public Report startModel(Schedule sc, LocalDateTime startDate) throws InterruptedException {
        shuffleArrivingDatesAndUpload(sc);
        boolean isModelingContinue = true;
        int tryModeling = 1;

        while (isModelingContinue) {
            ArrayList<ShipInfo> copyShips = new ArrayList<>(sc.ships);

            ExecutorService liquidThreads = Executors.newFixedThreadPool(countLiquidCrane);
            ExecutorService containerThreads = Executors.newFixedThreadPool(countContainerCrane);
            ExecutorService dryThreads = Executors.newFixedThreadPool(countDryCrane);

            LinkedBlockingQueue<ShipInfo> liquidQueue = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<ShipInfo> dryQueue = new LinkedBlockingQueue<>();
            LinkedBlockingQueue<ShipInfo> containerQueue = new LinkedBlockingQueue<>();

            System.out.println("Start modeling. Try: " + tryModeling);
            System.out.println("Container crane count " + countContainerCrane);
            System.out.println("Liquid crane count " + countLiquidCrane);
            System.out.println("Dry crane count " + countDryCrane);

            LocalDateTime tmpStartDate = startDate;


            for (int i = 0; i < modelingDays; i++) {
                ArrayList<ShipInfo> arrivingShips = getShipsByDate(copyShips, tmpStartDate);
//                System.out.println("Ships arrived " + tmpStartDate);
                for (ShipInfo si : arrivingShips) {
                    switch (si.getCargoType()) {
                        case CONTAINER:
                            containerQueue.put(si);
//                            System.out.println(si.getName() + " " + si.getCargoType() + " " + si.getArrivingDate());
                            break;
                        case LIQUID:
                            liquidQueue.put(si);
//                            System.out.println(si.getName() + " " + si.getCargoType() + " " + si.getArrivingDate());
                            break;
                        case DRY:
                            dryQueue.put(si);
//                            System.out.println(si.getName() + " " + si.getCargoType() + " " + si.getArrivingDate());
                            break;
                    }
                }
//                System.out.println("------------------------");
                tmpStartDate = tmpStartDate.plusDays(1);

            }
            for (int i = 0; i < countLiquidCrane; i++) {
                UploadThread liquid = new UploadThread(liquidQueue, "liquid"+i, startDate, Config.capacityLiquidKgPerHour);
                liquidThreads.submit(liquid);
            }
            for (int i = 0; i < countContainerCrane; i++) {
                UploadThread container = new UploadThread(containerQueue, "container"+i, startDate, Config.capacityContainerUnitPerHour);
                containerThreads.submit(container);
            }
            for (int i = 0; i < countDryCrane; i++) {
                UploadThread dry = new UploadThread(dryQueue, "dry"+i, startDate, Config.capacityDryKgPerHour);
                dryThreads.submit(dry);
            }

            while (liquidQueue.size() > 0) {
                Thread.sleep(100);
            }
            while (dryQueue.size() > 0) {
                Thread.sleep(100);
            }
            while (containerQueue.size() > 0) {
                Thread.sleep(100);
            }

            long penaltyLiquid = 0;
            long penaltyDry = 0;
            long penaltyContainer = 0;


            for (ShipInfo si : copyShips) {
                if(si.startUnload != null) {
                    long unloadHours = si.startUnload.until(si.endUnload, ChronoUnit.HOURS) - si.getExpectedUnloadHours();
                    si.penalty = si.enterQueue.until(si.startUnload, ChronoUnit.HOURS) * 100;
                    if (unloadHours > 0) {
                        si.penalty += unloadHours * 100;
                    }

                    switch (si.getCargoType()) {
                        case LIQUID:
                            penaltyLiquid += si.penalty;
                            break;
                        case DRY:
                            penaltyDry += si.penalty;
                            break;
                        case CONTAINER:
                            penaltyContainer += si.penalty;
                            break;
                    }
                }
            }

            System.out.println("Penalty container " + penaltyContainer);
            System.out.println("Penalty dry " + penaltyDry);
            System.out.println("Penalty liquid " + penaltyLiquid);


            if ( (penaltyLiquid > liftCost) || (penaltyContainer > liftCost) || (penaltyDry > liftCost)) {
                if (penaltyLiquid > liftCost) {
                    countLiquidCrane += penaltyLiquid / liftCost;
                }
                if (penaltyContainer > liftCost) {
                    countContainerCrane += penaltyContainer / liftCost;
                }
                if (penaltyDry > liftCost) {
                    countDryCrane += penaltyDry / liftCost;
                }
                tryModeling++;
            } else {
                isModelingContinue = false;
            }
            System.out.println("=========================\n");


            liquidThreads.shutdown();
            containerThreads.shutdown();
            dryThreads.shutdown();
//            System.out.println(new Gson().toJson(sc.ships));
//            break;
        }

        System.out.println("END modeling. Try: " + tryModeling);
        System.out.println("Container crane count " + countContainerCrane);
        System.out.println("Liquid crane count " + countLiquidCrane);
        System.out.println("Dry crane count " + countDryCrane);
        ///generate report

        return null;
    }

}
