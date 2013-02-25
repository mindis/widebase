package widebase.ui.test

import java.text.SimpleDateFormat

import net.liftweb.common. { Loggable, Logger }

import org.joda.time. { LocalDate, LocalDateTime }
import org.joda.time.format.DateTimeFormat

import scala.util.Random

import widebase.db.table. { Table, TemplateTable }

/* Test Scatter chart for table file, directory table and partitioned table.
 *
 * @author myst3r10n
 */
object Scatter extends Logger with Loggable {

  protected var debug: Boolean = _
  protected var parts: Int = _
  protected var records: Int = _

  // Init DB
  val dbi =
    widebase.db.instance(System.getProperty("user.dir") + "/usr/data/test/db")

  // Init API
  import dbi.tables._

  // Init DSL
  import widebase.dsl.conversion._
  import widebase.dsl.datatype._
  import widebase.dsl.function._

  // Init chart
  import widebase.ui._

  case class Data(val x: Double, val y: Double)

  case class DataTable(
    table: Table = Table(string("x", "y"), double(), double()))
    extends TemplateTable[Data] {

    val x = table("x").d
    val y = table("y").d

    def +=(data: Data) = {

      x += data.x
      y += data.y

      this

    }

    def +=(x: Double, y: Double): DataTable =
      this += Data(x, y)

    def ++=(table: DataTable) = {

      for(r <- 0 to table.records.length - 1)
        this += table(r)

      this

    }

    def apply(index: Int) = Data(x(index), y(index))

    def filter(predicate: Data => Boolean) = {

      val filteredTable = new DataTable

      for(r <- 0 to records.length - 1)
        if(predicate(this(r)))
          filteredTable += this(r)

      filteredTable

    }

    def peer = table

  }

  val millis = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S")
    .parse("2012-01-23 12:34:56.789").getTime

  def diff(started: Long, stopped: Long) =
    "%.3f s".format((stopped - started).toDouble / 1000)

  def main(args: Array[String]) {

    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")

    var from = 1
    val till = 100

    saveTable("plotScatter", fillTable(from, till))
    println("")
    saveDirTable("dirPlotScatter", fillTable(from, till))

    plotTable("plotScatter")
    figure += 1
    plotDirTable("dirPlotScatter")

  }

  def fillTable(from: Int, till: Int) = {

    var started = 0L

    val table = new DataTable

    var records = 0
    var move = from

    started = System.currentTimeMillis
    for(i <- from to till) {

      if(Random.nextBoolean)
        table.x += (math.random * 10).toInt
      else
        table.x += -(math.random * 10).toInt

      if(Random.nextBoolean)
        table.y += (math.random * 10).toInt
      else
        table.y += -(math.random * 10).toInt

      records += 1

    }
    info("Table filled " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table

  }

  def saveTable(name: String, table: DataTable) {

    var started = 0L

    started = System.currentTimeMillis
    save(name, table.peer)
    info("Table saved " + table.records.length + " records in " +
      diff(started, System.currentTimeMillis))

    table.peer.columns.foreach(column => column.clear)

  }

  def saveDirTable(name: String, table: DataTable) {

    var started = 0L

    started = System.currentTimeMillis
    save.dir(name, table.peer)
    info("Dir table saved " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table.peer.columns.foreach(column => column.clear)

  }

  def savePartedDirTable(name: String, table: DataTable) {

    var started = 0L

    started = System.currentTimeMillis
    save.dir(name, table.peer)('daily)
    info("Parted dir table saved " + records + " records in " +
      diff(started, System.currentTimeMillis))

    table.peer.columns.foreach(column => column.clear)

  }

  def plotTable(name: String) {

    val table = DataTable(load(name))

    scatter(table.x, table.y)

  }

  def plotDirTable(name: String) {

    val table = DataTable(map(name))

    scatter(table.x, table.y)

  }
}

