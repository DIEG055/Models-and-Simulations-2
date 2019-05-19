/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ejercicio_1_2;

/**
 *
 * @author Juan Diego Medina
 */
public class Ship {

    private float arrivalTime;

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
