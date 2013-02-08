package widebase.plot

import org.jfree.data.xy. { XYDataItem, XYSeriesWorkaround }

import widebase.db.column.TypedColumn

/** A partitioned table compatible `XYSeries`.
 *
 * @param x columns
 * @param y columns
 * @param name of series
 **/
class XYSeriesParted(
  protected val x: Array[TypedColumn[Double]],
  protected val y: Array[TypedColumn[Double]],
  name: String)
  extends XYSeriesWorkaround(name) {

  override def getDataItem(index: Int): XYDataItem =
    getRawDataItem(index)

  override def getItemCount = {

    var records = 0

    x.foreach(records += _.length)

    records

  }

  override def getRawDataItem(index: Int): XYDataItem = {

    val part = (index / x.head.length).toInt
    val record = index % x.head.length

    new XYDataItem(x(part)(record), y(part)(record))

  }
}

