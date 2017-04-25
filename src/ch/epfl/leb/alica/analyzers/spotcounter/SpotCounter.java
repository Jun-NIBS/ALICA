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
package ch.epfl.leb.alica.analyzers.spotcounter;

import ch.epfl.leb.alica.Analyzer;
import ij.measure.ResultsTable;
import ij.process.ShortProcessor;
import java.awt.image.ColorModel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author stefko
 */
public class SpotCounter implements Analyzer {
    private final int noise_tolerance;
    private final int box_size;
    private final SpotCounterCore core;
    
    private double current_output = 0.0;
    
    public SpotCounter(int noise_tolerance, int box_size) {
        this.noise_tolerance = noise_tolerance;
        this.box_size = box_size;
        this.core = new SpotCounterCore(noise_tolerance, box_size);
    }
    

    @Override
    public double getCurrentOutput() {
        return current_output;
    }

    @Override
    public String getName() {
        return "SpotCounter";
    }

    @Override
    public void processImage(Object image, int image_width, int image_height, double pixel_size_um, long time_ms) {
        
        double fov_area = pixel_size_um*pixel_size_um*image_width*image_height;
        ShortProcessor sp = new ShortProcessor(image_width, image_height);
        sp.setPixels(image);
        ResultsTable results = core.analyze(sp.duplicate());
        current_output = results.getValue("n", results.getCounter()-1) ;// fov_area;
        Logger.getLogger(this.getName()).log(Level.SEVERE, String.format("Image: %d, Density: %10.5f\n", results.getCounter(), current_output));
    }
    
}
