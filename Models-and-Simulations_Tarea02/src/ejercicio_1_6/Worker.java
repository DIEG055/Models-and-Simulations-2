/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejercicio_1_6;

import org.omg.CORBA.portable.IDLEntity;

/**
 *
 * @author Juan Diego Medina
 */
public class Worker {
    int BUSY = 1;
    int IDLE = 0;
    float usetime;
    int state;
    boolean waiting;

    public Worker() {
        this.usetime = 0;
        this.state = IDLE;
        this.waiting = false;
    }

    public float getUsetime(float simTime) {
        return this.usetime / simTime;
    }
    
    public boolean busy(){
        return (this.state == BUSY);
    }
    
    public void setState(int state) {
    	this.state = state;
    }
    
    public void setWaitingForBox( boolean wait) {
    	this.waiting = wait;
    }
    
    public boolean  isWaiting() {
    	return this.waiting;
    
    }
}
