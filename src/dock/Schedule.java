package dock;

import java.util.ArrayList;

//класс расписания
public class Schedule {
    public ArrayList<ShipInfo> ships; //список из кораблей

    //конструктор
    public Schedule() {
        this.ships = new ArrayList<>();
    }

    //добавить корабль
    public void addShip(ShipInfo shipInfo) {
        ships.add(shipInfo); //добать кораблю всю информацию
    }

    //вывести все корабли с их информацией
    public void print() {
        for (ShipInfo s : ships) { //цикл по всем кораблям
            s.print(); //вывод кораблей с информацией
        }
    }
}