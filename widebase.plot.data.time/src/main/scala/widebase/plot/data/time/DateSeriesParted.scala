package widebase.plot.data.time

import org.jfree.data.time. { Day, TimeSeriesDataItem }

import widebase.db.column. { DateColumn, TypedColumn }

/** A partitioned table compatible `TimeSeries`.
 *
 * @param events series of event columns
 * @param values series of value columns
 * @param name of series
 *
 * @author myst3r10n
 **/
class DateSeriesParted(
  protected val events: Array[DateColumn],
  protected val values: Array[TypedColumn[Number]],
  name: String)
  extends TimeSeriesPartedLike(name) {

  protected val records = {

    var total = 0

    events.foreach(total += _.length)

    total

  }

  override def getRawDataItem(index: Int): TimeSeriesDataItem = {

    val part = (index / events.head.length).toInt
    val record = index % events.head.length

    val event = events(part)(record)

    new TimeSeriesDataItem(
      new Day(event.getDayOfMonth, event.getMonthOfYear, event.getYear),
      values(part)(record))

  }
}

