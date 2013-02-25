package widebase.testkit

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

import org.joda.time.format.DateTimeFormat

import widebase.data.Datatype

import widebase.db.table.Table

/* Test of load, map and save operations with various buffer size.
 *
 * @author myst3r10n
 */
object Capacity extends Logger with Loggable {

  // Init DSL
  import widebase.dsl.conversion._
  import widebase.dsl.datatype._
  import widebase.dsl.function._

  // Init Testkit
  import widebase.testkit._

  // Init DB
  val dbi =
    widebase.db.instance(System.getProperty("user.dir") + "/usr/data/test/db")

  // Init API
  import dbi.tables._

  val modes = Array(
    128,
    256,
    512,
    1024,
    2048,
    4096,
    8192,
    16384,
    32768,
    65536)

  val millis = LocalDateTime.parse(
    "2012-01-23 12:34:56.789",
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")).toDateTime.getMillis

  var parts: Int = _
  var records: Int = _

  def main(args: Array[String]) {

    parts = 10
    records = 25000

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

    val table = Table(
      string(
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
        "String"),
      bool(),
      byte(),
      char(),
      double(),
      float(),
      int(),
      long(),
      short(),
      month(),
      date(),
      minute(),
      second(),
      time(),
      datetime(),
      timestamp(),
      symbol(),
      string())

    var boolCol = table("Bool").b
    var byteCol = table("Byte").x
    var charCol = table("Char").c
    var doubleCol = table("Double").d
    var floatCol = table("Float").f
    var intCol = table("Int").i
    var longCol = table("Long").l
    var shortCol = table("Short").s
    var monthCol = table("Month").M
    var dateCol = table("Date").D
    var minuteCol = table("Minute").U
    var secondCol = table("Second").V
    var timeCol = table("Time").T
    var datetimeCol = table("DateTime").Z
    var timestampCol = table("Timestamp").P
    var symbolCol = table("Symbol").Y
    var stringCol = table("String").S

    started = System.currentTimeMillis
    for(r <- 1 to records) {

      boolCol += true
      byteCol += Byte.MaxValue
      charCol += Char.MaxValue
      doubleCol += Double.MaxValue
      floatCol += Float.MaxValue
      intCol += Int.MaxValue
      longCol += Long.MaxValue
      shortCol += Short.MaxValue
      monthCol += new YearMonth(millis)
      dateCol += new LocalDate(millis)
      minuteCol += Minutes.minutes(12)
      secondCol += Seconds.seconds(34)
      timeCol += new LocalTime(millis)
      datetimeCol += new LocalDateTime(millis)
      timestampCol += new Timestamp(millis)
      symbolCol += 'Hello
      stringCol += "World!"

    }
    info("Table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      save(name, table)
      info("Table saved " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }

    table.columns.foreach(column => column.clear)

    var loaded: Table = null

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      loaded = load(name)
      info("Table loaded " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }

    boolCol = loaded("Bool").b
    byteCol = loaded("Byte").x
    charCol = loaded("Char").c
    doubleCol = loaded("Double").d
    floatCol = loaded("Float").f
    intCol = loaded("Int").i
    longCol = loaded("Long").l
    shortCol = loaded("Short").s
    monthCol = loaded("Month").M
    dateCol = loaded("Date").D
    minuteCol = loaded("Minute").U
    secondCol = loaded("Second").V
    timeCol = loaded("Time").T
    datetimeCol = loaded("DateTime").Z
    timestampCol = loaded("Timestamp").P
    symbolCol = loaded("Symbol").Y
    stringCol = loaded("String").S

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      for(r <- 0 to records - 1) {

        assert(boolCol(r) == true, error("Value unexpected: " + boolCol(r)))
        assert(byteCol(r) == Byte.MaxValue, error("Value unexpected: " + byteCol(r)))
        assert(charCol(r) == Char.MaxValue, error("Value unexpected: " + charCol(r)))
        assert(doubleCol(r) == Double.MaxValue, error("Value unexpected: " + doubleCol(r)))
        assert(floatCol(r) == Float.MaxValue, error("Value unexpected: " + floatCol(r)))
        assert(intCol(r) == Int.MaxValue, error("Value unexpected: " + intCol(r)))
        assert(longCol(r) == Long.MaxValue, error("Value unexpected: " + longCol(r)))
        assert(shortCol(r) == Short.MaxValue, error("Value unexpected: " + shortCol(r)))
        assert(monthCol(r) == new YearMonth(millis), error("Value unexpected: " + monthCol(r)))
        assert(dateCol(r) == new LocalDate(millis), error("Value unexpected: " + dateCol(r)))
        assert(minuteCol(r) == Minutes.minutes(12), error("Value unexpected: " + minuteCol(r)))
        assert(secondCol(r) == Seconds.seconds(34), error("Value unexpected: " + secondCol(r)))
        assert(timeCol(r) == new LocalTime(millis), error("Value unexpected: " + timeCol(r)))
        assert(datetimeCol(r) == new LocalDateTime(millis), error("Value unexpected: " + datetimeCol(r)))
        assert(timestampCol(r) == new Timestamp(millis), error("Value unexpected: " + timestampCol(r)))
        assert(symbolCol(r) == 'Hello, error("Value unexpected: " + symbolCol(r)))
        assert(stringCol(r) == "World!", error("Value unexpected: " + stringCol(r)))

      }
      info("Table iterated " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by + " + mode + " bytes")

    }
  }

  def dirTable(name: String, records: Int) {

    var started = 0L

    val table = Table(
      string(
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
        "String"),
      bool(),
      byte(),
      char(),
      double(),
      float(),
      int(),
      long(),
      short(),
      month(),
      date(),
      minute(),
      second(),
      time(),
      datetime(),
      timestamp(),
      symbol(),
      string())

    var boolCol = table("Bool").b
    var byteCol = table("Byte").x
    var charCol = table("Char").c
    var doubleCol = table("Double").d
    var floatCol = table("Float").f
    var intCol = table("Int").i
    var longCol = table("Long").l
    var shortCol = table("Short").s
    var monthCol = table("Month").M
    var dateCol = table("Date").D
    var minuteCol = table("Minute").U
    var secondCol = table("Second").V
    var timeCol = table("Time").T
    var datetimeCol = table("DateTime").Z
    var timestampCol = table("Timestamp").P
    var symbolCol = table("Symbol").Y
    var stringCol = table("String").S

    started = System.currentTimeMillis
    for(r <- 1 to records) {

      boolCol += true
      byteCol += Byte.MaxValue
      charCol += Char.MaxValue
      doubleCol += Double.MaxValue
      floatCol += Float.MaxValue
      intCol += Int.MaxValue
      longCol += Long.MaxValue
      shortCol += Short.MaxValue
      monthCol += new YearMonth(millis)
      dateCol += new LocalDate(millis)
      minuteCol += Minutes.minutes(12)
      secondCol += Seconds.seconds(34)
      timeCol += new LocalTime(millis)
      datetimeCol += new LocalDateTime(millis)
      timestampCol += new Timestamp(millis)
      symbolCol += 'Hello
      stringCol += "World!"

    }
    info("Dir table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      save.dir(name, table)
      info("Dir table saved " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }


    table.columns.foreach(column => column.clear)

    var loaded: Table = null

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      loaded = load.dir(name)
      info("Dir table loaded " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }

    boolCol = loaded("Bool").b
    byteCol = loaded("Byte").x
    charCol = loaded("Char").c
    doubleCol = loaded("Double").d
    floatCol = loaded("Float").f
    intCol = loaded("Int").i
    longCol = loaded("Long").l
    shortCol = loaded("Short").s
    monthCol = loaded("Month").M
    dateCol = loaded("Date").D
    minuteCol = loaded("Minute").U
    secondCol = loaded("Second").V
    timeCol = loaded("Time").T
    datetimeCol = loaded("DateTime").Z
    timestampCol = loaded("Timestamp").P
    symbolCol = loaded("Symbol").Y
    stringCol = loaded("String").S

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      for(r <- 0 to records - 1) {

        assert(boolCol(r) == true, error("Value unexpected: " + boolCol(r)))
        assert(byteCol(r) == Byte.MaxValue, error("Value unexpected: " + byteCol(r)))
        assert(charCol(r) == Char.MaxValue, error("Value unexpected: " + charCol(r)))
        assert(doubleCol(r) == Double.MaxValue, error("Value unexpected: " + doubleCol(r)))
        assert(floatCol(r) == Float.MaxValue, error("Value unexpected: " + floatCol(r)))
        assert(intCol(r) == Int.MaxValue, error("Value unexpected: " + intCol(r)))
        assert(longCol(r) == Long.MaxValue, error("Value unexpected: " + longCol(r)))
        assert(shortCol(r) == Short.MaxValue, error("Value unexpected: " + shortCol(r)))
        assert(monthCol(r) == new YearMonth(millis), error("Value unexpected: " + monthCol(r)))
        assert(dateCol(r) == new LocalDate(millis), error("Value unexpected: " + dateCol(r)))
        assert(minuteCol(r) == Minutes.minutes(12), error("Value unexpected: " + minuteCol(r)))
        assert(secondCol(r) == Seconds.seconds(34), error("Value unexpected: " + secondCol(r)))
        assert(timeCol(r) == new LocalTime(millis), error("Value unexpected: " + timeCol(r)))
        assert(datetimeCol(r) == new LocalDateTime(millis), error("Value unexpected: " + datetimeCol(r)))
        assert(timestampCol(r) == new Timestamp(millis), error("Value unexpected: " + timestampCol(r)))
        assert(symbolCol(r) == 'Hello, error("Value unexpected: " + symbolCol(r)))
        assert(stringCol(r) == "World!", error("Value unexpected: " + stringCol(r)))

      }
      info("Dir table loaded iterated " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by + " + mode + " bytes")

    }

    loaded.columns.foreach(column => column.clear)

    var mapped: Table = null

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      mapped = map(name)
      info("Dir table mapped " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }

    boolCol = mapped("Bool").b
    byteCol = mapped("Byte").x
    charCol = mapped("Char").c
    doubleCol = mapped("Double").d
    floatCol = mapped("Float").f
    intCol = mapped("Int").i
    longCol = mapped("Long").l
    shortCol = mapped("Short").s
    monthCol = mapped("Month").M
    dateCol = mapped("Date").D
    minuteCol = mapped("Minute").U
    secondCol = mapped("Second").V
    timeCol = mapped("Time").T
    datetimeCol = mapped("DateTime").Z
    timestampCol = mapped("Timestamp").P
    symbolCol = mapped("Symbol").Y
    stringCol = mapped("String").S

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      for(r <- 0 to records - 1) {

        assert(boolCol(r) == true, error("Value unexpected: " + boolCol(r)))
        assert(byteCol(r) == Byte.MaxValue, error("Value unexpected: " + byteCol(r)))
        assert(charCol(r) == Char.MaxValue, error("Value unexpected: " + charCol(r)))
        assert(doubleCol(r) == Double.MaxValue, error("Value unexpected: " + doubleCol(r)))
        assert(floatCol(r) == Float.MaxValue, error("Value unexpected: " + floatCol(r)))
        assert(intCol(r) == Int.MaxValue, error("Value unexpected: " + intCol(r)))
        assert(longCol(r) == Long.MaxValue, error("Value unexpected: " + longCol(r)))
        assert(shortCol(r) == Short.MaxValue, error("Value unexpected: " + shortCol(r)))
        assert(monthCol(r) == new YearMonth(millis), error("Value unexpected: " + monthCol(r)))
        assert(dateCol(r) == new LocalDate(millis), error("Value unexpected: " + dateCol(r)))
        assert(minuteCol(r) == Minutes.minutes(12), error("Value unexpected: " + minuteCol(r)))
        assert(secondCol(r) == Seconds.seconds(34), error("Value unexpected: " + secondCol(r)))
        assert(timeCol(r) == new LocalTime(millis), error("Value unexpected: " + timeCol(r)))
        assert(datetimeCol(r) == new LocalDateTime(millis), error("Value unexpected: " + datetimeCol(r)))
        assert(timestampCol(r) == new Timestamp(millis), error("Value unexpected: " + timestampCol(r)))
        assert(symbolCol(r) == 'Hello, error("Value unexpected: " + symbolCol(r)))
        assert(stringCol(r) == "World!", error("Value unexpected: " + stringCol(r)))

      }
      info("Dir table mapped iterated " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by + " + mode + " bytes")

    }
  }

  def partedDirTable(
    name: String,
    parts: Int,
    records: Int) {

    var started = 0L

    val table = Table(
      string(
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
        "String"),
      date(),
      bool(),
      byte(),
      char(),
      double(),
      float(),
      int(),
      long(),
      short(),
      month(),
      date(),
      minute(),
      second(),
      time(),
      datetime(),
      timestamp(),
      symbol(),
      string())

    var partitionCol = table("Partition").D
    var boolCol = table("Bool").b
    var byteCol = table("Byte").x
    var charCol = table("Char").c
    var doubleCol = table("Double").d
    var floatCol = table("Float").f
    var intCol = table("Int").i
    var longCol = table("Long").l
    var shortCol = table("Short").s
    var monthCol = table("Month").M
    var dateCol = table("Date").D
    var minuteCol = table("Minute").U
    var secondCol = table("Second").V
    var timeCol = table("Time").T
    var datetimeCol = table("DateTime").Z
    var timestampCol = table("Timestamp").P
    var symbolCol = table("Symbol").Y
    var stringCol = table("String").S

    var partition = new LocalDate(millis)

    started = System.currentTimeMillis
    for(p <- 1 to parts) {

      for(r <- 1 to records / parts) {

        partitionCol += partition
        boolCol += true
        byteCol += Byte.MaxValue
        charCol += Char.MaxValue
        doubleCol += Double.MaxValue
        floatCol += Float.MaxValue
        intCol += Int.MaxValue
        longCol += Long.MaxValue
        shortCol += Short.MaxValue
        monthCol += new YearMonth(millis)
        dateCol += new LocalDate(millis)
        minuteCol += Minutes.minutes(12)
        secondCol += Seconds.seconds(34)
        timeCol += new LocalTime(millis)
        datetimeCol += new LocalDateTime(millis)
        timestampCol += new Timestamp(millis)
        symbolCol += 'Hello
        stringCol += "World!"

      }

      partition = partition.plusDays(1)

    }
    info("Parted dir table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    started = System.currentTimeMillis
    save.dir(name, table)('daily)
    info("Parted dir table saved " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table.columns.foreach(column => column.clear)

    var loaded: Iterable[Table] = null

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      loaded = load.dates(
        name,
        new LocalDate(millis),
        new LocalDate(millis).plusDays(parts)).tables
      info("Parted dir table loaded " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      var partition = new LocalDate(millis)

      started = System.currentTimeMillis
      for(table <- loaded) {

        partitionCol = table("Partition").D
        boolCol = table("Bool").b
        byteCol = table("Byte").x
        charCol = table("Char").c
        doubleCol = table("Double").d
        floatCol = table("Float").f
        intCol = table("Int").i
        longCol = table("Long").l
        shortCol = table("Short").s
        monthCol = table("Month").M
        dateCol = table("Date").D
        minuteCol = table("Minute").U
        secondCol = table("Second").V
        timeCol = table("Time").T
        datetimeCol = table("DateTime").Z
        timestampCol = table("Timestamp").P
        symbolCol = table("Symbol").Y
        stringCol = table("String").S

        for(r <- 0 to records / parts - 1) {

          assert(partitionCol(r) == partition, error("Value unexpected: " + partitionCol(r)))
          assert(boolCol(r) == true, error("Value unexpected: " + boolCol(r)))
          assert(byteCol(r) == Byte.MaxValue, error("Value unexpected: " + byteCol(r)))
          assert(charCol(r) == Char.MaxValue, error("Value unexpected: " + charCol(r)))
          assert(doubleCol(r) == Double.MaxValue, error("Value unexpected: " + doubleCol(r)))
          assert(floatCol(r) == Float.MaxValue, error("Value unexpected: " + floatCol(r)))
          assert(intCol(r) == Int.MaxValue, error("Value unexpected: " + intCol(r)))
          assert(longCol(r) == Long.MaxValue, error("Value unexpected: " + longCol(r)))
          assert(shortCol(r) == Short.MaxValue, error("Value unexpected: " + shortCol(r)))
          assert(monthCol(r) == new YearMonth(millis), error("Value unexpected: " + monthCol(r)))
          assert(dateCol(r) == new LocalDate(millis), error("Value unexpected: " + dateCol(r)))
          assert(minuteCol(r) == Minutes.minutes(12), error("Value unexpected: " + minuteCol(r)))
          assert(secondCol(r) == Seconds.seconds(34), error("Value unexpected: " + secondCol(r)))
          assert(timeCol(r) == new LocalTime(millis), error("Value unexpected: " + timeCol(r)))
          assert(datetimeCol(r) == new LocalDateTime(millis), error("Value unexpected: " + datetimeCol(r)))
          assert(timestampCol(r) == new Timestamp(millis), error("Value unexpected: " + timestampCol(r)))
          assert(symbolCol(r) == 'Hello, error("Value unexpected: " + symbolCol(r)))
          assert(stringCol(r) == "World!", error("Value unexpected: " + stringCol(r)))

        }

        partition = partition.plusDays(1)

      }
      info("Parted dir table loaded iterated " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by + " + mode + " bytes")

    }

    loaded.foreach(table => table.columns.foreach(column => column.clear))

    var mapped: Iterable[Table] = null

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      started = System.currentTimeMillis
      mapped = map.dates(
        name,
        new LocalDate(millis),
        new LocalDate(millis).plusDays(parts)).tables
      info("Parted dir table mapped " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by " + mode + " bytes")

    }

    modes.foreach { mode =>

      System.setProperty("widebase.io.capacity", mode.toString)

      partition = new LocalDate(millis)

      started = System.currentTimeMillis
      for(table <- mapped) {

        partitionCol = table("Partition").D
        boolCol = table("Bool").b
        byteCol = table("Byte").x
        charCol = table("Char").c
        doubleCol = table("Double").d
        floatCol = table("Float").f
        intCol = table("Int").i
        longCol = table("Long").l
        shortCol = table("Short").s
        monthCol = table("Month").M
        dateCol = table("Date").D
        minuteCol = table("Minute").U
        secondCol = table("Second").V
        timeCol = table("Time").T
        datetimeCol = table("DateTime").Z
        timestampCol = table("Timestamp").P
        symbolCol = table("Symbol").Y
        stringCol = table("String").S

        for(r <- 0 to records / parts - 1) {

          assert(partitionCol(r) == partition, error("Value unexpected: " + partitionCol(r)))
          assert(boolCol(r) == true, error("Value unexpected: " + boolCol(r)))
          assert(byteCol(r) == Byte.MaxValue, error("Value unexpected: " + byteCol(r)))
          assert(charCol(r) == Char.MaxValue, error("Value unexpected: " + charCol(r)))
          assert(doubleCol(r) == Double.MaxValue, error("Value unexpected: " + doubleCol(r)))
          assert(floatCol(r) == Float.MaxValue, error("Value unexpected: " + floatCol(r)))
          assert(intCol(r) == Int.MaxValue, error("Value unexpected: " + intCol(r)))
          assert(longCol(r) == Long.MaxValue, error("Value unexpected: " + longCol(r)))
          assert(shortCol(r) == Short.MaxValue, error("Value unexpected: " + shortCol(r)))
          assert(monthCol(r) == new YearMonth(millis), error("Value unexpected: " + monthCol(r)))
          assert(dateCol(r) == new LocalDate(millis), error("Value unexpected: " + dateCol(r)))
          assert(minuteCol(r) == Minutes.minutes(12), error("Value unexpected: " + minuteCol(r)))
          assert(secondCol(r) == Seconds.seconds(34), error("Value unexpected: " + secondCol(r)))
          assert(timeCol(r) == new LocalTime(millis), error("Value unexpected: " + timeCol(r)))
          assert(datetimeCol(r) == new LocalDateTime(millis), error("Value unexpected: " + datetimeCol(r)))
          assert(timestampCol(r) == new Timestamp(millis), error("Value unexpected: " + timestampCol(r)))
          assert(symbolCol(r) == 'Hello, error("Value unexpected: " + symbolCol(r)))
          assert(stringCol(r) == "World!", error("Value unexpected: " + stringCol(r)))

        }

        partition = partition.plusDays(1)

      }
      info("Parted dir table mapped iterated " + records + " records in " +
        diff(started, System.currentTimeMillis) + " by + " + mode + " bytes")

    }
  }
}
