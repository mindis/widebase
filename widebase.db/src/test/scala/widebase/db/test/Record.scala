package widebase.db.test

import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat

import net.liftweb.common. { Loggable, Logger }

import org.joda.time. {

  LocalDate,
  LocalDateTime,
  LocalTime,
  Minutes,
  Seconds,
  YearMonth

}

import widebase.data.Datatype

import widebase.db.column. {

  BoolColumn,
  ByteColumn,
  CharColumn,
  DoubleColumn,
  FloatColumn,
  IntColumn,
  LongColumn,
  ShortColumn,
  MonthColumn,
  DateColumn,
  MinuteColumn,
  SecondColumn,
  TimeColumn,
  DateTimeColumn,
  TimestampColumn,
  SymbolColumn,
  StringColumn

}

import widebase.db.table.Table

/* Test of load, map and save operations with records.
 *
 * @author myst3r10n
 */
object Record extends Logger with Loggable {

  protected var debug: Boolean = _
  protected var parts: Int = _
  protected var records: Int = _

  // Init DB
  val dbi =
    widebase.db.instance(System.getProperty("user.dir") + "/usr/data/test/db")

  // Init API
  import dbi.tables._

  val millis = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S")
    .parse("2012-01-23 12:34:56.789").getTime

  def diff(started: Long, stopped: Long) =
    "%.3f s".format((stopped - started).toDouble / 1000)

  def main(args: Array[String]) {

    debug = false
    parts = 10
    records = 2500

    var i = 0

    while(i < args.length) {

      args(i) match {

        case "-d" => debug = true

        case "-p" =>
          i += 1
          parts = args(i).toInt

        case "-r" =>
          i += 1
          records = args(i).toInt

        case _ =>
          error("Unfamiliar with argument: " + args(i))
          sys.exit(1)

      }

      i += 1

    }

    if(records / parts <= 0) {

      error("records / parts must be >= 1")
      sys.exit(1)

    }

    table("table", records)
    println("")
    dirTable("dirTable", records)
    println("")
    partedDirTable("dirTable", parts, records)

  }

  def table(name: String, records: Int) {

    var started = 0L

    val table = Table(StringColumn(
      "Bool",
      "Byte",
      "Char",
      "Double",
      "Float",
      "Int",
      "Long",
      "Short",
      "Month",
      "Date",
      "Minute",
      "Second",
      "Time",
      "DateTime",
      "Timestamp",
      "Symbol",
      "String"))

    started = System.currentTimeMillis
    for(r <- 1 to records)
      table += (
        true,
        Byte.MaxValue,
        Char.MaxValue,
        Double.MaxValue,
        Float.MaxValue,
        Int.MaxValue,
        Long.MaxValue,
        Short.MaxValue,
        new YearMonth(millis),
        new LocalDate(millis),
        Minutes.minutes(12),
        Seconds.seconds(34),
        new LocalTime(millis),
        new LocalDateTime(millis),
        new Timestamp(millis),
        'Hello,
        "World!")
    info("Table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    started = System.currentTimeMillis
    save(name, table)
    info("Table saved " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table.columns.foreach(column => column.clear)

    started = System.currentTimeMillis
    val loaded = load(name)
    info("Table loaded " + records + " records in " +
      diff(started, System.currentTimeMillis))

    var r = 0

    started = System.currentTimeMillis
    loaded.records.foreach { record =>

      r += 1

      if(debug || r == loaded.records.length)
        println("Record " + r + ": " + record)
      else
        record

    }
    info("Table iterated " + records + " records in " +
      diff(started, System.currentTimeMillis))

  }

  def dirTable(name: String, records: Int) {

    var started = 0L

    val table = Table(StringColumn(
      "Bool",
      "Byte",
      "Char",
      "Double",
      "Float",
      "Int",
      "Long",
      "Short",
      "Month",
      "Date",
      "Minute",
      "Second",
      "Time",
      "DateTime",
      "Timestamp",
      "Symbol",
      "String"))

    started = System.currentTimeMillis
    for(r <- 1 to records)
      table += (
        true,
        Byte.MaxValue,
        Char.MaxValue,
        Double.MaxValue,
        Float.MaxValue,
        Int.MaxValue,
        Long.MaxValue,
        Short.MaxValue,
        new YearMonth(millis),
        new LocalDate(millis),
        Minutes.minutes(12),
        Seconds.seconds(34),
        new LocalTime(millis),
        new LocalDateTime(millis),
        new Timestamp(millis),
        'Hello,
        "World!")
    info("Dir table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    started = System.currentTimeMillis
    save.dir(name, table)
    info("Dir table saved " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table.columns.foreach(column => column.clear)

    started = System.currentTimeMillis
    val loaded = load.dir(name)
    info("Dir table loaded " + records + " records in " +
      diff(started, System.currentTimeMillis))

    var loadedR = 0

    started = System.currentTimeMillis
    loaded.records.foreach { record =>

      loadedR += 1

      if(debug || loadedR == loaded.records.length)
        println("Record " + loadedR + ": " + record)
      else
        record

    }
    info("Dir table loaded iterated " + records + " records in " +
      diff(started, System.currentTimeMillis))

    loaded.columns.foreach(column => column.clear)

    started = System.currentTimeMillis
    val mapped = map(name)
    info("Dir table mapped " + records + " records in " +
      diff(started, System.currentTimeMillis))

    var mappedR = 0

    started = System.currentTimeMillis
    mapped.records.foreach { record =>

      mappedR += 1

      if(debug || mappedR == mapped.records.length)
        println("Record " + mappedR + ": " + record)
      else
        record

    }
    info("Dir table mapped iterated " + records + " records in " +
      diff(started, System.currentTimeMillis))

  }

  def partedDirTable(
    name: String,
    parts: Int,
    records: Int) {

    var started = 0L

    val table = Table(StringColumn(
      "Partition",
      "Bool",
      "Byte",
      "Char",
      "Double",
      "Float",
      "Int",
      "Long",
      "Short",
      "Month",
      "Date",
      "Minute",
      "Second",
      "Time",
      "DateTime",
      "Timestamp",
      "Symbol",
      "String"))

    var partition = new LocalDate(millis)

    started = System.currentTimeMillis
    for(p <- 1 to parts) {

      for(r <- 1 to records / parts)
        table += (
          partition,
          true,
          Byte.MaxValue,
          Char.MaxValue,
          Double.MaxValue,
          Float.MaxValue,
          Int.MaxValue,
          Long.MaxValue,
          Short.MaxValue,
          new YearMonth(millis),
          new LocalDate(millis),
          Minutes.minutes(12),
          Seconds.seconds(34),
          new LocalTime(millis),
          new LocalDateTime(millis),
          new Timestamp(millis),
          'Hello,
          "World!")

      partition = partition.plusDays(1)

    }
    info("Parted dir table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    started = System.currentTimeMillis
    save.dir(name, table)('daily)
    info("Parted dir table saved " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table.columns.foreach(column => column.clear)

    started = System.currentTimeMillis
    val loaded = load.dates(
      name,
      new LocalDate(millis),
      new LocalDate(millis).plusDays(parts)).tables
    info("Parted dir table loaded " + records + " records in " +
      diff(started, System.currentTimeMillis))

    var loadedP = 0

    started = System.currentTimeMillis
    for(table <- loaded) {

      loadedP += 1

      var r = 0

      table.records.foreach { record =>

        r += 1

        if(debug || (loadedP == parts && r == table.records.length / parts))
          println("Record " + r + ": " + record)
        else
          record

      }
    }
    info("Parted dir table loaded iterated " + records + " records in " +
      diff(started, System.currentTimeMillis))

    loaded.foreach(table => table.columns.foreach(column => column.clear))

    started = System.currentTimeMillis
    val mapped = map.dates(
      name,
      new LocalDate(millis),
      new LocalDate(millis).plusDays(parts)).tables
    info("Parted dir table mapped " + records + " records in " +
      diff(started, System.currentTimeMillis))

    var mappedP = 0

    started = System.currentTimeMillis
    for(table <- mapped) {

      mappedP += 1

      var r = 0

      table.records.foreach { record =>

        r += 1

        if(debug || (mappedP == parts && r == table.records.length / parts))
          println("Record " + r + ": " + record)
        else
          record

      }
    }
    info("Parted dir table mapped iterated " + records + " records in " +
      diff(started, System.currentTimeMillis))

  }
}

