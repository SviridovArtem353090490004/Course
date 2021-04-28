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


class UploadThread extends Thread {
    private LinkedBlockingQueue<ShipInfo> queue;
    private String name;

    public UploadThread( LinkedBlockingQueue<ShipInfo> queue, String name){
        this.queue = queue;
        this.name = name;
    }

    @Override
    public void run(){
        while(true) {
            try{

                System.out.println("liquid size " + queue.size());
                ShipInfo si = queue.take();
                System.out.println("take liquid");
                Thread.sleep(500);
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

    public DockModel(){
        liquidThreads = new ArrayList<>();
        containerThreads = new ArrayList<>();
        dryThreads = new ArrayList<>();

        countLiquidCrane = 1;
        countContainerCrane = 1;
        countDryCrane = 1;
    }

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
        boolean isModelingContinue = true;

        while(isModelingContinue) {
            for (int i = 0; i < countLiquidCrane; i++) {
                UploadThread liquid = new UploadThread(liquidQueue, "liquid");
                liquidThreads.add(liquid);
            }
            for (int i = 0; i < countContainerCrane; i++) {
                UploadThread container = new UploadThread(containerQueue, "container");
                containerThreads.add(container);
            }
            for (int i = 0; i < countDryCrane; i++) {
                UploadThread dry = new UploadThread(dryQueue, "dry");
                dryThreads.add(dry);
            }

            for (UploadThread th : liquidThreads) {
                th.start();
            }

            for (UploadThread th : dryThreads) {
                th.start();
            }

            for (UploadThread th : containerThreads) {
                th.start();
            }

            for (int i = 0; i < modelingDays; i++) {
                ArrayList<ShipInfo> arrivingShips = getShipsByDate(sc.ships, startDate);
                System.out.println(startDate);
                System.out.println("==========================");
                for (ShipInfo si : arrivingShips) {
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

            while(liquidQueue.size() > 0){
                Thread.sleep(100);
            }
            while(dryQueue.size() > 0){
                Thread.sleep(100);
            }
            while(containerQueue.size() > 0){
                Thread.sleep(100);
            }

            if(/*есть ли еще штрафы > 30000*/ false){
                if(/*если много штрафов за жидкую разгрузку*/ false) {
                    countLiquidCrane++;
                }
                if(/*если много штрафов за контейнерную разгрузку*/ false) {
                    countContainerCrane++;
                }
                if(/*если много штрафов за сухую разгрузку*/ false) {
                    countDryCrane++;
                }
            }else{
                isModelingContinue = false;
            }
        }


        ///generate report

        return null;
    }

}
