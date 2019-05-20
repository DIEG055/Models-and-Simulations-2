package Ejercicio_1_2;

public class Ship {

    private final float arrivalTime;

    public Ship(float arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public float getArrival_time() {
        return arrivalTime;
    }

    public float timeInSystem(float currentTime) {
        return currentTime - arrivalTime;
    }

}
