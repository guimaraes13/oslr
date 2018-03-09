/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
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
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
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

    private static final int LEFT_PANE_BORDER = 55;
    private static final int RIGHT_PANE_BORDER = 10;
    private static final int TOP_PANE_BORDER = 34;
    private static final int BOTTOM_PANE_BORDER = 78;
    private static final double TICK_UNIT = 0.1;

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
        XYSeriesCollection data = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, data,
                                                          orientation, legend, tooltips, urls);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(0.0, 1.0);
        domainAxis.setTickUnit(new NumberTickUnit(TICK_UNIT));
        domainAxis.setVerticalTickLabels(true);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 1.0);
        rangeAxis.setTickUnit(new NumberTickUnit(TICK_UNIT));
        rangeAxis.setVerticalTickLabels(true);

        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new Dimension(LEFT_PANE_BORDER + width + RIGHT_PANE_BORDER,
                                                  TOP_PANE_BORDER + height + BOTTOM_PANE_BORDER));
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
        int height = 500;

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
