package widebase.ui.chart

import org.jfree.chart.JFreeChart

import scala.swing. { Component, Publisher }

/** Panel of chart.
 * 
 * @param chart of panel
 *
 * @author myst3r10n
 */
class ChartPanel(chart: JFreeChart) extends Component with Publisher {

  /** The [[org.jfree.chart.ChartPanel]] object. */
  override lazy val peer =
    new org.jfree.chart.ChartPanel(chart)
      with ShiftableChartPanel
      with ZoomableChartPanel {

      setMouseZoomable(false)
      setZoomInFactor(0.9)
      setZoomOutFactor(1.1)

      // Prevent stretching
      setMinimumDrawWidth(0)
      setMinimumDrawHeight(0)
      setMaximumDrawWidth(Int.MaxValue)
      setMaximumDrawHeight(Int.MaxValue)

      override def paintComponent(g: java.awt.Graphics) {

        super.paintComponent(g)

      }
    }
}

