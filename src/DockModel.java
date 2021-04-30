import com.google.gson.Gson;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;


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


class UploadThread extends Thread {
    private LinkedBlockingQueue<ShipInfo> queue;
    private String name;
    private LocalDateTime startDate;
    private long speed;

    public UploadThread(LinkedBlockingQueue<ShipInfo> queue, String name, LocalDateTime startDate, long speed) {
        this.queue = queue;
        this.name = name;
        this.startDate = startDate;
        this.speed = speed;
    }

    @Override
    public void run() {
        LocalDateTime tmp = startDate;
        while (true) {
            try {
                ShipInfo si = queue.take();
//                System.out.println(si.getName() + "thread Was tmp " + tmp );
                System.out.println(tmp + "thread Was tmp " );
                if (tmp.isBefore(si.enterQueue)) {
                    tmp = si.enterQueue;
                    System.out.println(si.getName() + " Was tmp *********** " + tmp + " become enterQueue" + si.enterQueue);
                }

                si.startUnload = tmp;
                long hours = si.getWeight() / speed + si.getUploadLag();
                System.out.println(tmp + " Unload hours " + hours);
                tmp = tmp.plusHours(hours);
                System.out.println(tmp + " Tmp after unload "  );


                si.endUnload = tmp;

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

    private final LinkedBlockingQueue<ShipInfo> liquidQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ShipInfo> dryQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ShipInfo> containerQueue = new LinkedBlockingQueue<>();

    private final int time = 500;

    private final ArrayList<UploadThread> liquidThreads;
    private final ArrayList<UploadThread> containerThreads;
    private final ArrayList<UploadThread> dryThreads;

    public DockModel() {
        liquidThreads = new ArrayList<>();
        containerThreads = new ArrayList<>();
        dryThreads = new ArrayList<>();

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

        while (isModelingContinue) {
            LocalDateTime tmpStartDate = startDate;

            for (int i = 0; i < modelingDays; i++) {
                ArrayList<ShipInfo> arrivingShips = getShipsByDate(sc.ships, tmpStartDate);
//                System.out.println(startDate);
//                System.out.println("==========================");
                for (ShipInfo si : arrivingShips) {
                    switch (si.getCargoType()) {
                        case CONTAINER:
//                            System.out.println("put container");
                            containerQueue.put(si);
                            break;
                        case LIQUID:
//                            System.out.println("put liquid");
                            liquidQueue.put(si);
                            break;
                        case DRY:
//                            System.out.println("put dry");
                            dryQueue.put(si);
                            break;
                    }
                }

                tmpStartDate = tmpStartDate.plusDays(1);

            }
            for (int i = 0; i < countLiquidCrane; i++) {
                UploadThread liquid = new UploadThread(liquidQueue, "liquid", startDate, Config.capacityLiquidKgPerHour);
                liquidThreads.add(liquid);
            }
            for (int i = 0; i < countContainerCrane; i++) {
                UploadThread container = new UploadThread(containerQueue, "container", startDate, Config.capacityContainerUnitPerHour);
                containerThreads.add(container);
            }
            for (int i = 0; i < countDryCrane; i++) {
                UploadThread dry = new UploadThread(dryQueue, "dry", startDate, Config.capacityDryKgPerHour);
                dryThreads.add(dry);
            }

            for (UploadThread th : liquidThreads) {
                th.start();
            }

//            for (UploadThread th : dryThreads) {
//                th.start();
//            }
//
//            for (UploadThread th : containerThreads) {
//                th.start();
//            }

            while (liquidQueue.size() > 0) {
                Thread.sleep(100);
            }
//            while (dryQueue.size() > 0) {
//                Thread.sleep(100);
//            }
//            while (containerQueue.size() > 0) {
//                Thread.sleep(100);
//            }

            if (/*есть ли еще штрафы > 30000*/ false) {
                if (/*если много штрафов за жидкую разгрузку*/ false) {
                    countLiquidCrane++;
                }
                if (/*если много штрафов за контейнерную разгрузку*/ false) {
                    countContainerCrane++;
                }
                if (/*если много штрафов за сухую разгрузку*/ false) {
                    countDryCrane++;
                }
            } else {
                isModelingContinue = false;
            }

            System.out.println(new Gson().toJson(sc.ships));
        }


        ///generate report

        return null;
    }

}
