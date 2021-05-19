package dock;

import java.time.LocalDateTime;

public class UnloadingReport {
    String name; //имя корабля
    String arrivingDateTime; //дата прибытия в порт
    long waitingHours; //format dd:hh:mm - формат даты
    LocalDateTime unloadingStart; //дата и время старта разгрузки
    long unloadingHours; //длительность разгрузки

    public UnloadingReport(String name, String arrivingDateTime, long waitingHours, LocalDateTime unloadingStart, long unloadingHours) {
        this.name = name;
        this.arrivingDateTime = arrivingDateTime;
        this.waitingHours = waitingHours;
        this.unloadingStart = unloadingStart;
        this.unloadingHours = unloadingHours;
    }
}