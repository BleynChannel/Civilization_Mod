package com.civilization.civil_utils_lib.discord.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.awt.*;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class PlayersChart {
    private static XYDataset buildDataset(ArrayList<Integer> players, Date startupTime, int spacingTime) {
        // Создаем массив из временых меток
        Minute[] times = new Minute[players.size()];

        Calendar startupTimeCalendar = Calendar.getInstance();
        startupTimeCalendar.setTime(startupTime);

        for (int index = 0; index < times.length; index++) {
            times[index] = new Minute(startupTimeCalendar.getTime());
            startupTimeCalendar.set(Calendar.MINUTE,
                    startupTimeCalendar.get(Calendar.MINUTE) + spacingTime);
        }

        // Создаем датасет
        TimeSeries series = new TimeSeries("Игроки");
        for (int index = 0; index < times.length; index++) {
            series.add(times[index], players.get(index));
        }

        TimeSeriesCollection dateset = new TimeSeriesCollection();
        dateset.addSeries(series);

        return dateset;
    }

    private static byte[] convertToPNG(JFreeChart chart) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(stream, chart, 480, 360);
        return stream.toByteArray();
    }

    public static byte[] build(@NotNull ArrayList<Integer> players, @NotNull Date startupTime, int spacingTime)
            throws IOException {
        XYDataset dataset = buildDataset(players, startupTime, spacingTime);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null,
                "Время (МСК)",
                "Кол-во игроков",
                dataset);

        Color backgroundColor = new Color(0x212121);
        Color outlineColor = new Color(0x616161);
        Color labelColor = new Color(0xfafafa);
        Color axisColor = new Color(0xeeeeee);

        chart.setBackgroundPaint(backgroundColor);

        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(backgroundColor);
        legend.setItemPaint(labelColor);
        legend.setItemFont(new Font("Lato", Font.PLAIN, 12));

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(backgroundColor);
        plot.setDomainGridlineStroke(new BasicStroke(1));
        plot.setRangeGridlineStroke(new BasicStroke(1));
        plot.setDomainGridlinePaint(outlineColor);
        plot.setRangeGridlinePaint(outlineColor);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setOutlineVisible(false);

        ValueAxis valueAxis = plot.getRangeAxis();
        valueAxis.setRange(0, 8);
        valueAxis.setStandardTickUnits(new NumberTickUnitSource(true));
        valueAxis.setLabelPaint(labelColor);
        valueAxis.setAxisLinePaint(axisColor);
        valueAxis.setTickMarkPaint(labelColor);
        valueAxis.setTickLabelPaint(labelColor);
        valueAxis.setLabelFont(new Font("Lato", Font.BOLD, 14));
        valueAxis.setTickLabelFont(new Font("Lato", Font.PLAIN, 12));

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("H:mm"));
        dateAxis.setLabelPaint(labelColor);
        dateAxis.setAxisLinePaint(axisColor);
        dateAxis.setTickMarkPaint(labelColor);
        dateAxis.setTickLabelPaint(labelColor);
        dateAxis.setLabelFont(new Font("Lato", Font.BOLD, 14));
        dateAxis.setTickLabelFont(new Font("Lato", Font.PLAIN, 12));

        return convertToPNG(chart);
    }
}
