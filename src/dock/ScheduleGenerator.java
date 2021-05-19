package dock;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

//перечисление типов грузов
enum CargoType {
    LIQUID("Liquid"), //жидкий груз
    CONTAINER("Container"), //контейнеры
    DRY("Dry"); //сухой груз

    private static final List<CargoType> VALUES = Arrays.asList(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random(); //переменная случайной величины

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

//класс информации о корабле
class ShipInfo {
    public final String dateFormat = "d MMM yyyy, HH:mm:ss"; //формат времени
    public transient final Semaphore uploadCount = new Semaphore(2); //кол-во кранов, которые одновременно могут разгружать одно судно по умолчанию
    public String name; //имя корабля
    public CargoType cargoType; //тип груза
    public long unloadLag; //задержка разгрузки
    public LocalDateTime startUnload; //время старта разгрузки
    public LocalDateTime endUnload; //время конца разгрузки
    public LocalDateTime enterQueue; //время вставания в очечедь
    public long penalty; //штраф за простой в очереди
    private String arrivingDate; //дата прибытия
    private long weight; //вес груза
    private long expectedUnloadHours; //ожидаемое время разгрузки в часах

    //конструктор
    public ShipInfo(Instant arrivingDate, String name, long weight,
                    CargoType cargoType, long expectedUnloadHours) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0"));
        this.arrivingDate = formatter.format(arrivingDate);
        this.name = name;
        this.weight = weight;
        this.cargoType = cargoType;
        this.expectedUnloadHours = expectedUnloadHours;
        this.unloadLag = 0;

        this.penalty = 0;
        startUnload = endUnload = null;

    }

    //геттер для времени прибытия
    public String getArrivingDate() {
        return arrivingDate;
    }

    //добавление отставания
    public void addLag(int rangeDays) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.of("UTC+0")); //формат даты
        LocalDateTime date = LocalDateTime.parse(arrivingDate, formatter.ofPattern(this.dateFormat, Locale.UK)); //парсинг даты
        Random rnd = new Random(); //создание случайной величины
        if (rnd.nextInt(100) < Config.possibilityOfShipLags) { //если случайная величина до 100 меньше возможности отставания судна
            //судно получит отставание
            if (rnd.nextInt(2) == 0) { //если случайная величина равна нулю
                date = date.minusDays(rnd.nextInt(rangeDays)); //делаем отрицательное отставание
            } else { //иначе
                date = date.plusDays(rnd.nextInt(rangeDays)); //делаем положительное отставание
            }
        }
        Instant in = date.toInstant(ZoneOffset.UTC); //приводим дату к Instant

        arrivingDate = formatter.format(in); //форматируем дату прибытия
        this.enterQueue = LocalDateTime.parse(this.arrivingDate, formatter.ofPattern(this.dateFormat, Locale.UK)); //парсим дату входа в очередь
        if (rnd.nextInt(100) < Config.possibilityOfUploadLags) { //если случайная величина до 100 меньше возможности отставания судна
            //судно получит отставание на разгрузке
            this.unloadLag = rnd.nextInt(1440 / 60); //в диапазоне от 0 до 1440 деленноеи на 60, ибо используем часы
        }


    }

    //геттрер имени корабля
    public String getName() {
        return name;
    }

    //геттер веса груза
    public long getWeight() {
        return weight;
    }

    //геттер ожидаемого времени разгрузки
    public long getExpectedUnloadHours() {
        return expectedUnloadHours;
    }

    //геттер типа груза
    public CargoType getCargoType() {
        return cargoType;
    }

    //геттер отставания
    public long getUnloadLag() {
        return unloadLag;
    }

    //вывод информации о корабле
    public void print() {
        System.out.println("Arriving date: " + arrivingDate); //вывод времени прибытия
        System.out.println("Name: " + name);  //вывод имени судна
        if (cargoType != CargoType.CONTAINER) { // цикл по типу груза
            System.out.println("Weight (kg): " + weight); //вывод веса в килограммах для контейнера
        } else {
            System.out.println("Units (pcs): " + weight); //вывод кол-ва груза в штуках для остальных типов
        }
        System.out.println("Cargo type: " + cargoType.name()); //вывод типа груза
        System.out.println("Waiting days: " + expectedUnloadHours); //вывод ожидаемого времени разгрузки в часах
    }
}


//Service 1
//генератор расписания
public class ScheduleGenerator {

    private Instant startInclusive;
    private Instant endExclusive;

    private long capacityLiquidKgPerHour; //скорость разгрузки крана для жидких грузов в час
    private long capacityDryKgPerHour; //скорость разгрузки крана для сыпучих грузов в час
    private long capacityContainerUnitPerHour; //скорость разгрузки крана для контейнеров в час

    //генератор расписания
    public ScheduleGenerator(long capacityContainerUnitPerHour, long capacityDryKgPerHour, long capacityLiquidKgPerHour, String startDate) {

        startInclusive = LocalDate.parse(startDate).atStartOfDay(ZoneId.of("UTC+0")).toInstant(); //парсим и приводим к Instant начало стоянки
        endExclusive = Instant.now(); //конец стоянки

        this.capacityContainerUnitPerHour = capacityContainerUnitPerHour;
        this.capacityLiquidKgPerHour = capacityLiquidKgPerHour;
        this.capacityDryKgPerHour = capacityDryKgPerHour;

    }

    // вычисление временной разницы
    private Instant between(Instant start) {
        long startSeconds = start.getEpochSecond(); //берем время начала
        long endSeconds = endExclusive.getEpochSecond(); //берем время конца
        long random = ThreadLocalRandom
                .current()
                .nextLong(startSeconds, endSeconds); //берем случайную величину

        return Instant.ofEpochSecond(random); //возвращаем случайную величину
    }

    //получение имени корабля путем создания случайной строки символов
    private String getRandomString() {
        int leftLimit = 48; // цифра '0'
        int rightLimit = 122; // буква 'z'
        int targetStringLength = 10; //длина имени судна
        Random random = new Random(); //случайная величина

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString(); //возвращаем случайно сгенерированное в диапазоне символов имя корабля
    }

    //генератор информации о корабле
    public ShipInfo generateShipInfo() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Instant arriving_date = between(startInclusive); //высчитываем дату прибытия как
        CargoType type = CargoType.random(); //делаем тип груза случайно сгенерированным
        long weightKg; //переменная веса груза
        if (type == CargoType.CONTAINER) { //если тип груза - контейнер, то
            weightKg = rnd.nextLong(20, 100); //вес в пределах от 20 до 100
        } else { //иначе
            weightKg = rnd.nextLong(10, 3000);//вес в пределах от 10 до 3000
        }

        long days = getHours(type, weightKg); //высчитываем кол-во дней в разгрузке в зависсимости от типа груза и его веса

        return new ShipInfo(
                arriving_date,
                getRandomString(),
                weightKg,
                type,
                days
        ); //возвращаем информацию о судне
    }

    //генератор времени
    private long getHours(CargoType type, long weight) {
        long hours = -1;
        if (type == CargoType.DRY) { //если тип груза - сухой, то
            hours = weight / capacityDryKgPerHour; //время разгрузки определяется отношением веса груза к скорости крана данного типа
            if (weight % capacityDryKgPerHour != 0) { //если остаток от деления веса на скорость крана не равно нулю, то
                hours++; //время увеличивается
            }
        } else {
            if (type == CargoType.LIQUID) { //если тип груза - жидкость, то
                hours = weight / capacityLiquidKgPerHour; //время разгрузки определяется отношением веса груза к скорости крана данного типа
                if (weight % capacityLiquidKgPerHour != 0) { //если остаток от деления веса на скорость крана не равно нулю, то
                    hours++; //время увеличивается
                }
            } else {
                if (type == CargoType.CONTAINER) { //если тип груза - контейнер, то
                    hours = weight / capacityContainerUnitPerHour; //время разгрузки определяется отношением веса груза к скорости крана данного типа
                    if (weight % capacityContainerUnitPerHour != 0) { //если остаток от деления веса на скорость крана не равно нулю, то
                        hours++; //время увеличивается
                    }
                }
            }
        }
        return hours; //возвращаем время
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