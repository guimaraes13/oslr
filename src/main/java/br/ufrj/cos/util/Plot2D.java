/*
 * Probabilist Logic Learner is a system to learn probabilistic logic
 * programs from data and use its learned programs to make inference
 * and answer queries.
 *
 * Copyright (C) 2017 Victor Guimarães
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

package br.ufrj.cos.util;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.List;

/**
 * Created on 21/05/17.
 *
 * @author Victor Guimarães
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Plot2D extends ApplicationFrame {

    /**
     * Creates a 2D plot
     *
     * @param title       the title
     * @param series      the series of points
     * @param xAxisLabel  the x axis label
     * @param yAxisLabel  the y axis label
     * @param orientation the y axis label orientation
     * @param legend      if is to legend
     * @param tooltips    if is to show tool tips
     * @param urls        if is to show urls
     * @param width       the width
     * @param height      the height
     */
    protected Plot2D(String title, XYSeries series, String xAxisLabel, String yAxisLabel, PlotOrientation orientation,
                     boolean legend, boolean tooltips, boolean urls, int width, int height) {
        super(title);
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(title,
                                                                xAxisLabel,
                                                                yAxisLabel,
                                                                data,
                                                                orientation,
                                                                legend,
                                                                tooltips,
                                                                urls
                                                               );
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(width, height));
        setContentPane(chartPanel);

    }

    /**
     * Creates a plot of a ROC curve.
     *
     * @param points the points of the curve
     * @return the plot
     */
    @SuppressWarnings({"ConstantConditions", "MagicNumber"})
    public static Plot2D createRocPlot(List<Pair<Double, Double>> points) {
        String title = "ROC Curve";
        XYSeries series = new XYSeries("ROC Curve");
        for (Pair<Double, Double> point : points) {
            series.add(point.getLeft(), point.getRight());
        }
        String xAxisLabel = "False Positive Rate";
        String yAxisLabel = "True Positive Rate";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean legend = true;
        boolean tooltips = true;
        boolean urls = false;
        int width = 500;
        int height = 270;

        return new Plot2D(title, series, xAxisLabel, yAxisLabel, orientation, legend, tooltips, urls, width, height);
    }

    /**
     * Shows the plot.
     */
    public void plot() {
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

}
