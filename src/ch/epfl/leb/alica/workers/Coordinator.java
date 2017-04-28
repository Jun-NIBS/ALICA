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
package ch.epfl.leb.alica.workers;

import ch.epfl.leb.alica.Analyzer;
import ch.epfl.leb.alica.Controller;
import ch.epfl.leb.alica.Laser;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import org.micromanager.Studio;
import org.micromanager.data.Coords;
import org.micromanager.data.Image;
import org.micromanager.data.NewImageEvent;
import org.micromanager.events.LiveModeEvent;
import org.micromanager.internal.graph.GraphData;

/**
 * The workhorse of the analysis. Grabs images from the MM core, analyzes them,
 * and controls the laser power while running.
 * @author Marcel Stefko
 */
public class Coordinator {
    private boolean stop_flag = false;
    private final long thread_start_time_ms;
    
    private final Controller controller;
    
    private final AnalysisWorker analysis_worker;
    private final ControlWorker control_worker;
    private final MonitorWorker monitor_worker;
    
    private final MonitorGUI gui;
    
    /**
     * Initialize the coordinator
     * @param studio MM studio
     * @param analyzer
     * @param controller
     * @param laser
     * @param draw_from_core whether the images should be drawn from the MMCore
     *  (true), or from the end of the processing pipeline (false)
     */
    public Coordinator(Studio studio, Analyzer analyzer, Controller controller, Laser laser, boolean draw_from_core) {
        // log the start time
        this.thread_start_time_ms = System.currentTimeMillis();
        // sanitize input
        if (studio == null)
            throw new NullPointerException("You need to set a studio!");
        if (analyzer == null)
            throw new NullPointerException("You need to set an analyzer!");
        if (controller == null)
            throw new NullPointerException("You need to set a controller!");
        if (laser == null)
            throw new NullPointerException("You need to set a laser!");
        this.controller = controller;
        
        // analysis worker is a thread which runs continuously
        this.analysis_worker = new AnalysisWorker(this, studio, analyzer, draw_from_core);
        studio.events().registerForEvents(this.analysis_worker);
        
        // this is a Timer which executes its internal task periodically
        this.control_worker = new ControlWorker(analysis_worker, controller, laser);
        this.control_worker.scheduleExecution(5000, 500);
        
        // initialize the GUI
        gui = new MonitorGUI(this, 
                analyzer.getName(), 
                controller.getName(), 
                laser.getDeviceName()+"-"+laser.getPropertyName(),
                controller.getSetpoint());
        gui.setLaserPowerDisplayMax(laser.getMaxPower());
        
        // this updates the GUI with info from the workers
        this.monitor_worker = new MonitorWorker(gui, analysis_worker, control_worker);
        this.monitor_worker.scheduleExecution(2000, 100);
        this.analysis_worker.start();
        
        
        
        
        
        // display the GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.setVisible(true);
            }
        });
        

    }
    
    
    /**
     * Request the threads to stop.
     */
    public void requestStop() {
        stop_flag = true;
        analysis_worker.requestStop();
        monitor_worker.cancel();
        control_worker.cancel();
       
        try {
            analysis_worker.join();
        } catch (InterruptedException ex) {
            // exit ungracefully
            Logger.getLogger(Coordinator.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Analysis worker shutdown was interrupted.");
        }
        
    }
    
    /**
     * Set the controller setpoint to value
     * @param value new value of controller setpoint
     */
    public void setSetpoint(double value) {
        controller.setSetpoint(value);
    }
    
    /**
     * Returns time in milliseconds since the worker was initialized
     * @return elapsed time in milliseconds
     */
    public final long getTimeMillis() {
        return System.currentTimeMillis() - thread_start_time_ms;
    }
}


