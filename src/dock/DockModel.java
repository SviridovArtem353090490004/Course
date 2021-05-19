package dock;

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


//класс потоков разгрузки
class UploadThread implements Runnable {
    private LinkedBlockingQueue<ShipInfo> queue; //очередь разгрузки
    private String name; //имя корабля
    private LocalDateTime startDate; //стартовая дата
    private long speed; //скорость разгрузки
    private Random rnd; //случайная величина
    private boolean running; //??

    //конструктор
    public UploadThread(LinkedBlockingQueue<ShipInfo> queue, String name, LocalDateTime startDate, long speed) {
        this.queue = queue;
        this.name = name;
        this.startDate = startDate;
        this.speed = speed;
        this.running = true;
        this.rnd = new Random();
    }

    //переопределение run
    @Override
    public void run() {
        LocalDateTime tmp = startDate; //присвоение стартовой даты промежуточной переменной
        while (running) { //цикл while running
            try { //блок try
                ShipInfo si = queue.take(); //получаем элемент из очереди с его удалением

//                System.out.println(si.name + " " + queue.size() + " " + si.getCargoType());
//                if(si != null) {
//                    System.out.println(this.name + " " + si.name + "Ship queue time " + si.enterQueue);
                if (tmp.isBefore(si.enterQueue)) { //если время промежуточной переменной предшествует времени входа судна то
//                        System.out.println(this.name + " " + si.name + " tmp was " + tmp);
                    tmp = si.enterQueue; //промежуточная переменная становится равна времени входа судна
                }

                si.startUnload = tmp; // присваеваем переменной старта разгрузки значение промежуточной переменной
//                    System.out.println(this.name + " " + si.name + " startunload " + tmp + " weight" + si.getWeight());
                long hours = si.getWeight() / speed + si.getUnloadLag(); //высчитываем время как сумму частного веса груза на скорость и отставание
//                    System.out.println(this.name + " " + si.name + "Unload hours " + hours + " lags " + si.getUploadLag());
                tmp = tmp.plusHours(hours); // присваеваем промежуточной переменной сумму часов hours
//                    System.out.println(this.name + " " + si.name + " endunload " + tmp);

                si.endUnload = tmp; // присваеваем переменной конца разгрузки значение промежуточной переменной
                Thread.sleep(hours); // отпускаем поток в сон на время hours
//                }
            } catch (InterruptedException e) { //ловля исключений и вывод стека вызовов
                System.out.println("Thread error");
                e.printStackTrace();
            }
        }
    }
}

//класс модели порта
//Service 3
public class DockModel {
    private int penaltyChargePerHour; //значение штрафа по умолчанию
    private int liftCost; //стоимость крана по умолчанию
    private int modelingDays; //длительность моделирования по умолчанию

    private int countLiquidCrane; //кол-во кранов для жидких грузов
    private int countContainerCrane; //кол-во кранов для контейнеров
    private int countDryCrane; //кол-во кранов для сыпучих грузов

    //конструктор (изначально кранов по 1 еденице)
    public DockModel(int penaltyChargePerHour, int liftCost, int modelingDays) {
        countLiquidCrane = 1;
        countContainerCrane = 1;
        countDryCrane = 1;
        this.penaltyChargePerHour = penaltyChargePerHour;
        this.liftCost = liftCost;
        this.modelingDays = modelingDays;
    }

    //отставания/опережения дат прибытия и разгрузки
    private void shuffleArrivingDatesAndUpload(Schedule sc) {
        for (int i = 0; i < sc.ships.size(); i++) { //цикл по размеру расписания судов
            sc.ships.get(i).addLag(7); //добавление случайной величины отставания от -7 до 7 дней
        }
    }

    //расписание по дням
    private ArrayList<ShipInfo> getShipsByDate(ArrayList<ShipInfo> ships, LocalDateTime date) {
        ArrayList<ShipInfo> res = new ArrayList<>(); //создаем список с результатами

        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0")); //задаем форматы даты и времени

        for (ShipInfo si : ships) { //цикл по всем кораблям
            LocalDateTime arrDate = LocalDateTime.parse(si.getArrivingDate(), formatter.ofPattern(si.dateFormat, Locale.UK)); //парсим дату прибытия

            if (arrDate.toLocalDate().equals(date.toLocalDate())) { //если время прибытия корабля равно дате, то
                res.add(si); //добавляем корабль
            }

        }

        return res;
    }
//генератор отчета

    private Report generateReport(ArrayList<ShipInfo> ships, long countLiquid, long countContainer, long countDry) {


        ArrayList<UnloadingReport> unloadingList = new ArrayList<>(); //создаем список разгрузки
        long penalty = 0; //задаем штрафы равными нулю в начале моделирования
        long maxUnloadDelay = 0; //задаем максимальное отставание в разгрузке равным нулю в начале моделирования
        long sumUnloadDelay = 0; //задаем сумму отставаний равной нулю в начале моделирования
        long sumWaitingHours = 0; //задаем сумму ожидания разгрузки равной нулю в начале моделирования
        for (ShipInfo si : ships) { //цикл по всем кораблям
            penalty += si.penalty; //суммируем штрафы
            sumUnloadDelay += si.unloadLag; //суммируем отставание
            if (si.unloadLag > maxUnloadDelay) { //если отставание в разгрузке у конкретного корабля больше, чем максимальное отставание, то
                maxUnloadDelay = si.unloadLag; //максимальным отставанием становится отставание конкретного корабля
            }

            long wh = si.enterQueue.until(si.startUnload, ChronoUnit.HOURS); //ожидание считаем от начала разгрузки
            sumWaitingHours += wh; // суммируем ожидание разгрузки

            unloadingList.add(new UnloadingReport(si.name,
                    si.getArrivingDate(),
                    wh,
                    si.startUnload,
                    si.startUnload.until(si.endUnload, ChronoUnit.HOURS))
            ); //добовляем список разгрузки в отчет

        }

        long shipsSize = ships.size(); //переменная кол-ва кораблей
        Statistics stat = new Statistics(shipsSize, sumWaitingHours / shipsSize,
                maxUnloadDelay, sumUnloadDelay / shipsSize, penalty, countLiquid,
                countContainer, countDry); //создаем объект типа статистика и заполяем его данными


        return new Report(unloadingList, stat); //возвращаем отчет
    }

    //старт модели работы порта
    public Report startModel(Schedule sc, LocalDateTime startDate) throws InterruptedException {
        shuffleArrivingDatesAndUpload(sc); //перемешиваем даты прибытия и разгрузки
        boolean isModelingContinue = true; //переменная для проверки работы порта
        int tryModeling = 1; //счетчик попыток моделирования
        ArrayList<ShipInfo> processedShips = new ArrayList<>(); //создание списка кораблей на разгрузку
        while (isModelingContinue) { //если модель порта работает, то
            ArrayList<ShipInfo> copyShips = new ArrayList<>(sc.ships); //копируем список кораблей в новый список

            ExecutorService liquidThreads = Executors.newFixedThreadPool(countLiquidCrane); //
            ExecutorService containerThreads = Executors.newFixedThreadPool(countContainerCrane); //
            ExecutorService dryThreads = Executors.newFixedThreadPool(countDryCrane); //

            LinkedBlockingQueue<ShipInfo> liquidQueue = new LinkedBlockingQueue<>(); //создаем блокирующую очередь для кранов для жидких грузов
            LinkedBlockingQueue<ShipInfo> dryQueue = new LinkedBlockingQueue<>(); //создаем блокирующую очередь для кранов для сухих грузов
            LinkedBlockingQueue<ShipInfo> containerQueue = new LinkedBlockingQueue<>(); //создаем блокирующую очередь для кранов для контейнеров

//            System.out.println("Start modeling. Try: " + tryModeling); //вывод начала моделирования
//            System.out.println("Container crane count " + countContainerCrane); // вывод стартового кол-ва кранов для контейнеров
//            System.out.println("Liquid crane count " + countLiquidCrane); // вывод стартового кол-ва кранов для жидких грузов
//            System.out.println("Dry crane count " + countDryCrane); // вывод стартового кол-ва кранов для сыпучих грузов

            LocalDateTime tmpStartDate = startDate; //присваеваем промежуточной переменной дату старта моделирования

            for (int i = 0; i < modelingDays; i++) { //цикл по дням моделирования
                ArrayList<ShipInfo> arrivingShips = getShipsByDate(copyShips, tmpStartDate); //создаем список прибытия кораблей по дням
//                System.out.println("Ships arrived " + tmpStartDate);
                for (ShipInfo si : arrivingShips) { //цикл по всем кораблям
                    switch (si.getCargoType()) { //свитч по типу груза
                        case CONTAINER: // если контейнер
                            containerQueue.put(si); //помещаем корабль в очередь на разгрузку к контейнерным кранам
                            processedShips.add(si); //помещаем корабль в список кораблей в разгрузке
//                            System.out.println(si.getName() + " " + si.getCargoType() + " " + si.getArrivingDate());
                            break;
                        case LIQUID: // если груз жидкий
                            liquidQueue.put(si); //помещаем корабль в очередь на разгрузку к кранам для жидких грузов
                            processedShips.add(si); //помещаем корабль в список кораблей в разгрузке
//                            System.out.println(si.getName() + " " + si.getCargoType() + " " + si.getArrivingDate());
                            break;
                        case DRY: // если сухой груз
                            dryQueue.put(si); //помещаем корабль в очередь на разгрузку к кранам для сухих грузов
                            processedShips.add(si); //помещаем корабль в список кораблей в разгрузке
//                            System.out.println(si.getName() + " " + si.getCargoType() + " " + si.getArrivingDate());
                            break;
                    }
                }
//                System.out.println("------------------------");
                tmpStartDate = tmpStartDate.plusDays(1);

            }

            for (int i = 0; i < countLiquidCrane; i++) { //цикл по кол-ву кранов для жидких грузов
                UploadThread liquid = new UploadThread(liquidQueue, "liquid" + i, startDate, Config.capacityLiquidKgPerHour); //создание нового крана для жидких грузов
                liquidThreads.submit(liquid);
            }
            for (int i = 0; i < countContainerCrane; i++) { //цикл по кол-ву кранов для контейнеров
                UploadThread container = new UploadThread(containerQueue, "container" + i, startDate, Config.capacityContainerUnitPerHour); //создание нового крана для контейнеров
                containerThreads.submit(container);
            }
            for (int i = 0; i < countDryCrane; i++) { //цикл по кол-ву кранов для сухих грузов
                UploadThread dry = new UploadThread(dryQueue, "dry" + i, startDate, Config.capacityDryKgPerHour); //создание нового крана для сухих грузов
                dryThreads.submit(dry);
            }

            while (liquidQueue.size() > 0) { //пока размер очереди для кранов для жидких грузов больше ноля
                Thread.sleep(100); //слип на 100 милисекунд
            }
            while (dryQueue.size() > 0) { //пока размер очереди для кранов для сухих грузов больше ноля
                Thread.sleep(100); //слип на 100 милисекунд
            }
            while (containerQueue.size() > 0) { //пока размер очереди для кранов для контейнеров больше ноля
                Thread.sleep(100); //слип на 100 милисекунд
            }

// сначала штраф равен нулю для каждого типа кранов
            long penaltyLiquid = 0;
            long penaltyDry = 0;
            long penaltyContainer = 0;


            for (ShipInfo si : copyShips) { //цикл по кораблям
                if (si.startUnload != null) { //если время старта разгрузки не null, то
                    long unloadHours = si.startUnload.until(si.endUnload, ChronoUnit.HOURS) - si.getExpectedUnloadHours(); //время разгрузки высчитывается как разница между стартом и финишем разгрузки
                    si.penalty = si.enterQueue.until(si.startUnload, ChronoUnit.HOURS) * penaltyChargePerHour; //
                    if (unloadHours > 0) { //если время разгрузки больше ноля, то
                        si.penalty += unloadHours * penaltyChargePerHour; //штраф увеличивается на произведение времени разгрузки на размер штрафа за час простоя
                    }

                    switch (si.getCargoType()) { //свитч по типу груза
                        case LIQUID: // если жидкий груз
                            penaltyLiquid += si.penalty; //увеличивается штраф для кранов для жидких грузов
                            break;
                        case DRY: // если сухой груз
                            penaltyDry += si.penalty; //увеличивается штраф для кранов для сухих грузов
                            break;
                        case CONTAINER: // если контейнер
                            penaltyContainer += si.penalty; //увеличивается штраф для кранов для контейнеров
                            break;
                    }
                }
            }

//            System.out.println("Penalty container " + penaltyContainer); //вывод суммы штрафов для кранов для контейнеров
//            System.out.println("Penalty dry " + penaltyDry); //вывод суммы штрафов для кранов для сыпучих грузов
//            System.out.println("Penalty liquid " + penaltyLiquid); //вывод суммы штрафов для кранов для жидких грузов


            if ((penaltyLiquid > liftCost) || (penaltyContainer > liftCost) || (penaltyDry > liftCost)) { // если штраф кранов для жидких грузов больше стоимости крана
                // или штраф кранов для сухих грузов больше стоимости крана
                // или штраф кранов для контейнеров больше стоимости крана, то
                if (penaltyLiquid > liftCost) { // если штраф кранов для жидких грузов больше стоимости крана
                    countLiquidCrane += penaltyLiquid / liftCost; //кол-во кранов для жидких грузов увеличиваем на частное штрафа кранов для жидких грузов и стоимости крана
                }
                if (penaltyContainer > liftCost) { //если штраф кранов для контейнеров больше стоимости крана
                    countContainerCrane += penaltyContainer / liftCost; //кол-во кранов для контейнеров увеличиваем на частное штрафа кранов для контейнеров и стоимости крана
                }
                if (penaltyDry > liftCost) { //если штраф кранов для сухих грузов больше стоимости крана
                    countDryCrane += penaltyDry / liftCost; //кол-во кранов для сухих грузов увеличиваем на частное штрафа кранов для сухих грузов и стоимости крана
                }
                tryModeling++; //увеличиваем счетчик попыток
            } else { //иначе
                isModelingContinue = false;  //не продолжаем моделирование
            }
//            System.out.println("=========================\n");


            liquidThreads.shutdown(); //выключаем потоки для кранов для жидких грузов
            containerThreads.shutdown(); //выключаем потоки для кранов для контейнеров
            dryThreads.shutdown(); //выключаем потоки для кранов для сухих грузов
//            System.out.println(new Gson().toJson(sc.ships));
//            break;

            if (isModelingContinue) { //если моделирование работает, то
                processedShips.clear(); //очищаем список кораблей в процессе разгрузки
            }
        }

//        System.out.println("END modeling. Try: " + tryModeling); // вывод конца моделирования
//        System.out.println("Container crane count " + countContainerCrane); // вывод потребовавшегося кол-ва кранов для контейнеров
//        System.out.println("Liquid crane count " + countLiquidCrane); // вывод потребовавшегося кол-ва кранов для жидких грузов
//        System.out.println("Dry crane count " + countDryCrane); // вывод потребовавшегося кол-ва кранов для сыпучих грузов

//        System.out.println(processedShips.size());
        return generateReport(processedShips, countLiquidCrane, countContainerCrane, countDryCrane); //возвращаем отчет

    }

}
