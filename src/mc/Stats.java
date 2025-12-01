/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mc;

/**
 *
 * @author tom Welford's method
 */
public class Stats {

    /* number of values */
    private long count = 0;
    /* last value */
    private double last = 0.0;
    /* sum of values */
    private double sum = 0.0;
    /* avg of values */
    private double avg = 0.0;

    public void update(double v) {
        double delta = v - avg;
        last = v;
        count++;
        avg += delta / count;
        sum += (double) (count - 1) / count * delta * delta;
    }

    public double getLast() {
        return last;
    }
    
    public long getCount() {
        return count;
    }
    
    public double getAvg() {
        return avg;
    }

    public double getVar() {
        return count < 2 ? Double.NaN : sum / (count - 1);
    }
    
    public double getStdev() {
        return Math.sqrt(getVar());
    }
    
    public double getCVar2() {
        return (1 + 0.25 / count) * getVar() / (getAvg() * getAvg());
    }
    
    @Override
    public String toString() {
        return "count = " + getCount() + ", avg = " + getAvg() + ", stdev = " + getStdev() + ", var = " + getVar() + ", cvar2 = " + getCVar2() + ", last = " + getLast();
    }
}
