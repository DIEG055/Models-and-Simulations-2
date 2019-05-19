/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ejercicio_1_2;

import org.omg.CORBA.portable.IDLEntity;

/**
 *
 * @author Juan Diego Medina
 */
public class Berth {
    int BUSY = 1;
    int IDLE = 0;
    Ship ship;
    float usetime;
    float shipArrivalTime;
    float shipDepartureTime;
    int state;

    public Berth() {
        this.usetime = 0;
        this.ship = null;
    }

    public void addShip(Ship s, float simtime, float shipDepartureTime) {
        this.ship = s;
        this.shipArrivalTime = simtime;
        this.shipDepartureTime = shipDepartureTime;
    }

    public Ship unloadedShip(float simtime) {
        Ship s = this.ship;
        this.ship = null;  
        usetime += (simtime - shipArrivalTime);
        this.shipArrivalTime = 0;
        this.shipDepartureTime = 0;
        return s;
    }

    public float getUsetime(float simTime) {
        return this.usetime / simTime;
    }
    
    public boolean busy(){
        if (this.ship != null){
            return true;
        }else{
            return false;
        }            
    }
    

}
