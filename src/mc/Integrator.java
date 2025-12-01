/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mc;

import java.io.IOException;

/**
 *
 * @author tom
 */
    public class Integrator {
    Domain domain;
    Function function;
    Function pdf;
    Stats stats;
        
    public void update() throws IOException {
        Object sample = domain.getElement();
        stats.update(function.getValue(sample) / pdf.getValue(sample));
    }
    
    public Stats getStats() {
        return stats;
    }
    
    @Override
    public String toString() {
         return "Int: " + stats.toString() + "\n";
    }
    
    public Integrator(Domain domain, Function function, Function pdf) {
        this.domain = domain;
        this.function = function;
        this.pdf = pdf;
        this.stats = new Stats();
    }
}
