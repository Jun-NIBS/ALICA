/* 
 * Copyright (C) 2017 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne
 * 
 * Author: Marcel Stefko
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.alica.controllers.pid;

import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.controllers.AbstractController;

/**
 *
 * @author stefko
 */
public class PID_controller extends AbstractController {
    private final MiniPID core;
    private final double P,I,D,F;
    
    private double current_output = 0.0;
    
    
    public PID_controller(double P, double I, double D, double F, double output_max) {
        super(output_max);
        this.P = P; this.I = I; this.D = D; this.F = F; 
        this.core = new MiniPID(P,I,D,F);
        this.core.setOutputLimits(0.0, maximum);
    }
    
    @Override
    public void setTarget(double target) {
        core.setSetpoint(target);
    }

    @Override
    public void nextValue(double value, long time_ms) {
        current_output = core.getOutput(value);
    }

    @Override
    public double getCurrentOutput() {
        return current_output;
    }
    
    @Override
    public String getName() {
        return "PID";
    }
    
}
