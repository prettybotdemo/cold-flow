package com.warm.flow.core.chart;

import com.warm.flow.core.utils.DrawUtils;
import com.warm.tools.utils.ObjectUtil;
import com.warm.tools.utils.StringUtils;

import java.awt.*;

/**
 * 流程图开始或者结束节点
 */
public class SkipChart implements FlowChart {
    private int[] xPoints;
    private int[] yPoints;

    private TextChart textChart;

    public TextChart getTextImage() {
        return textChart;
    }

    public SkipChart setTextImage(TextChart textChart) {
        this.textChart = textChart;
        return this;
    }

    public SkipChart(int[] xPoints, int[] yPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }

    public SkipChart(int[] xPoints, int[] yPoints, TextChart textChart) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.textChart = textChart;
    }

    public int[] getxPoints() {
        return xPoints;
    }

    public SkipChart setxPoints(int[] xPoints) {
        this.xPoints = xPoints;
        return this;
    }

    public int[] getyPoints() {
        return yPoints;
    }

    public SkipChart setyPoints(int[] yPoints) {
        this.yPoints = yPoints;
        return this;
    }

    @Override
    public void draw(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        // 画跳转线
        graphics.drawPolyline(xPoints, yPoints, xPoints.length);
        // 画箭头， 判断箭头朝向
        int[] xArrow;
        int[] yArrow;
        int xEndOne = xPoints[xPoints.length - 1];
        int xEndTwo = xPoints[xPoints.length - 2];
        int yEndOne = yPoints[yPoints.length - 1];
        int yEndTwo = yPoints[yPoints.length - 2];
        if (xEndOne == xEndTwo) {
            xArrow = new int[]{xEndOne - 5, xEndOne, xEndOne + 5};
            // 1、 如果最后两个左边x相同，最后一个 < 倒数第二个，朝下
            if (yEndOne > yEndTwo) {
                yArrow = new int[]{yEndOne - 10, yEndOne, yEndOne - 10};
            } else  {
                // 2、 如果最后两个左边x相同，最后一个 > 倒数第二个，朝上
                yArrow = new int[]{yEndOne + 10, yEndOne, yEndOne + 10};
            }
        } else  {
            yArrow = new int[]{yEndOne - 5, yEndOne, yEndOne + 5};
            // 3、 如果最后两个左边y相同，最后一个 < 倒数第二个，朝左
            if (xEndOne < xEndTwo) {
                xArrow = new int[]{xEndOne + 10, xEndOne, xEndOne + 10};
            } else  {
                // 4、 如果最后两个左边y相同，最后一个 > 倒数第二个，朝右
                xArrow = new int[]{xEndOne - 10, xEndOne, xEndOne - 10};
            }
        }
        graphics.fillPolygon(xArrow, yArrow, 3);
        if (ObjectUtil.isNotNull(textChart) && StringUtils.isNotEmpty(textChart.getTitle())) {
            textChart.setxText(textChart.getxText() - DrawUtils.stringWidth(textChart.getTitle()) / 2);
            textChart.setyText(textChart.getyText() - 10);
            // 填充文字说明
            textChart.draw(graphics);
        }
    }
}
