package dock;

public class Config {
    public static long capacityLiquidKgPerHour = 300; //скорость разгрузки крана для жидких грузов в час
    public static long capacityDryKgPerHour = 600; //скорость разгрузки крана для сыпучих грузов в час
    public static long capacityContainerUnitPerHour = 8; //скорость разгрузки крана для контейнеров в час


    public static int possibilityOfShipLags = 5; //возможность отклонения в расписании прибытия кораблей
    public static int possibilityOfUploadLags = 10; //возможность отклонения в разгрузке кораблей

}