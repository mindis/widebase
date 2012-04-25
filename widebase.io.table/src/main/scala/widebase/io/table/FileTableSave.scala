package widebase.io.table

import java.io. { File, RandomAccessFile }

import org.joda.time.LocalDate

import scala.collection.mutable. { ArrayBuffer, Map }

import vario.data.Datatype
import vario.file.FileVariantWriter
import vario.filter.StreamFilter
import vario.io.VariantWriter

import widebase.db.column.VariantColumn
import widebase.db.table.Table
import widebase.io.column. { ColumnWriter, FileColumnSaver }
import widebase.io.filter.MagicId

/** Saves tables into database.
 *
 * @param path of database
 *
 * @author myst3r10n
 */
abstract class FileTableSave(path: String) {

  import PartitionDomain.PartitionDomain
  import vario.filter.StreamFilter.StreamFilter

  /** Saves table into database.
    *
    * @param name of table
    * @param table self-explanatory
    * @param filter self-explanatory
    * @param segmented path of segment
   */
  def apply(
    name: String,
    table: Table,
    filter: StreamFilter = props.filters.saver)
    (implicit segmented: File = null) {

    val fileExtension =
      filter match {

        case StreamFilter.Gzip => ".gz"
        case _ => ""

      }

    var dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    val file = new File(dir + "/" + name + fileExtension)

    if(file.exists)
      file.delete

    val channel = new RandomAccessFile(file.getPath, "rw").getChannel
    channel.tryLock
    val vwriter = new VariantWriter(channel, filter) {

      override val charset = props.charsets.saver

      override def capacity = props.capacities.saver
      override def order = props.orders.saver

    }

    val writer = new ColumnWriter(vwriter)

    // Write column labels
    val labels = new VariantColumn('S)
    for(label <- table.labels)
      labels += label
    writer.write(labels)

    // Write column values
    table.columns.foreach(column => writer.write(column))

    writer.close

  }

  /** Saves table into directory table.
    *
    * @note Supports optional partitioned tables.
    *
    * @param name of table
    * @param table self-explanatory
    * @param parted partition symbol
    * @param segmented path of segment
   */
  def dir(
    name: String,
    table: Table)
    (implicit parted: Symbol = null, segmented: File = null) {

    if(parted != null) {

      val domain = parted match {

        case parted if
          parted == 'd ||
          parted == 'daily => PartitionDomain.Daily
        case parted if
          parted == 'm ||
          parted == 'monthly => PartitionDomain.Monthly
        case parted if
          parted == 'y ||
          parted == 'yearly => PartitionDomain.Yearly

      }

      this.parted(domain, name, table)(segmented)
      return

    }

    val saver = new FileColumnSaver(path)

    // Save column labels
    val labels = new VariantColumn('S)
    for(label <- table.labels)
      labels += label
    saver.save(name, ".d", labels, true)(null, segmented)

    // Save column values
    table.foreach { case (label, column) =>
      saver.save(name, label, column)(null, segmented) }

  }

  /** Saves table into partitioned directory table.
    *
    * @param domain of partition
    * @param name of table
    * @param table self-explanatory
    * @param segmented path of segment
   */
  def parted(
    domain: PartitionDomain,
    name: String,
    table: Table)(implicit segmented: File = null) {

    class SymbolCompanion(var writer: FileVariantWriter, var lastEnded: Int)
    class StringCompanion(var writer: FileVariantWriter, var lastEnded: Long)

    var dir: File = null

    var writers = ArrayBuffer[FileVariantWriter]()
    var symbolCompanions = Map[Int, SymbolCompanion]()
    var stringCompanions = Map[Int, StringCompanion]()

    def initWriters {

      table.foreach { case (name, column) =>

        val file = new File(dir.getPath + "/" + name)

        if(file.exists)
          file.delete

        val channel = new RandomAccessFile(file.getPath, "rw").getChannel
        channel.tryLock
        writers += new FileVariantWriter(channel, StreamFilter.None) {

          override val charset = props.charsets.parted

          override def capacity = props.capacities.parted
          override def order = props.orders.parted

          // Write magic
          mode = Datatype.String
          write(MagicId.Column.toString)

          // Write column type
          mode = Datatype.Byte
          write(column.typeOf.id.toByte)

          // Write column length
          mode = Datatype.Int
          write(0)

          // Set column value type
          if(column.typeOf == Datatype.Symbol)
            mode = Datatype.Int
          else if(column.typeOf == Datatype.String)
            mode = Datatype.Long
          else
            mode = column.typeOf

        }

        if(
          column.typeOf == Datatype.String ||
          column.typeOf == Datatype.Symbol) {

          var companion: File = null

          if(column.typeOf == Datatype.Symbol)
            companion = new File(file.getPath + ".sym")
          else if(column.typeOf == Datatype.String)
            companion = new File(file.getPath + ".str")

          if(companion.exists)
            companion.delete

          val channel =
            new RandomAccessFile(companion.getPath, "rw").getChannel
          channel.tryLock

          val writer = new FileVariantWriter(channel, StreamFilter.None) {

            override val charset = props.charsets.parted

            override def capacity = props.capacities.parted
            override def order = props.orders.parted

            mode = column.typeOf

          }

          column.typeOf match {

            case Datatype.Symbol => symbolCompanions +=
              writers.size - 1 -> new SymbolCompanion(writer, 0)

            case Datatype.String => stringCompanions +=
              writers.size - 1 -> new StringCompanion(writer, 0L)

          }
        }
      }
    }

    def writeRecords(records: Int) {

      writers.foreach { writer =>

        if(writer.isOpen) {

          writer.flush
          writer.mode = Datatype.Byte
          writer.position =
            (MagicId.Column.toString.getBytes(writer.charset)).size + 1

          writer.mode = Datatype.Int
          writer.write(records)

        }
      }
    }

    def releaseWriters {

      writers.foreach(writer =>
        if(writer.isOpen) writer.close)
      writers.clear

      symbolCompanions.values.foreach(companion =>
        if(companion.writer.isOpen)
          companion.writer.close)
      symbolCompanions.clear

      stringCompanions.values.foreach(companion =>
        if(companion.writer.isOpen)
          companion.writer.close)
      stringCompanions.clear

    }

    val labels = new VariantColumn('S)
    for(label <- table.labels)
      labels += label

    val saver = new FileColumnSaver(path)
    var numberOfRecordsOnPart = 0
    var lastInt: Option[Int] = scala.None
    var lastDate: LocalDate = null

    for(r <- 0 to table.records.size - 1) {

      var partition: String = null

      domain match {

        case PartitionDomain.Int =>
          val int = table.columns.head.ints(r)

          var newPartition = false

          if(lastInt.isEmpty)
            newPartition = true
          else if(lastInt.get != int) {

            println("End of int " + int + " passed, +" +
              numberOfRecordsOnPart + " records")

            newPartition = true

          }

          if(newPartition) {

            lastInt = Some(int)
            partition = lastInt.get.toString

          }

        case part if 
          domain == PartitionDomain.Yearly ||
          domain == PartitionDomain.Monthly ||
          domain == PartitionDomain.Daily =>

          val dateTime = table.columns.head.typeOf match {

            case Datatype.Long => table.columns.head.longs(r)
            case Datatype.Month => table.columns.head.months(r).toLocalDate(1).toDateMidnight.getMillis
            case Datatype.Date => table.columns.head.dates(r).toDateMidnight.getMillis
            case Datatype.Time => table.columns.head.times(r).getMillisOfDay
            case Datatype.DateTime => table.columns.head.dateTimes(r).toDateTime.getMillis
            case Datatype.Timestamp => table.columns.head.timestamps(r).getTime

          }

          var newPartition = false

          if(lastDate == null)
            newPartition = true
          else {

            val today = new LocalDate(dateTime)

            domain match {

              case PartitionDomain.Yearly =>
                if(lastDate.getYear != today.getYear) {

                  println("End of year " + lastDate.toString("yyyy") +
                  " passed, +" + numberOfRecordsOnPart + " records")

                  newPartition = true

                }

              case PartitionDomain.Monthly =>
                if(
                  lastDate.getYear != today.getYear ||
                  lastDate.getMonthOfYear != today.getMonthOfYear) {

                  println("End of month " + lastDate.toString("yyyy-MM") +
                    " passed, +" + numberOfRecordsOnPart + " records")

                  newPartition = true

                }

              case PartitionDomain.Daily =>
                if(
                  lastDate.getYear != today.getYear ||
                  lastDate.getMonthOfYear != today.getMonthOfYear ||
                  lastDate.getDayOfMonth != today.getDayOfMonth) {

                  println("End of day " + lastDate.toString("yyyy-MM-dd") +
                    " passed, +" + numberOfRecordsOnPart + " records")

                  newPartition = true

                }
            }
          }

          if(newPartition) {

            lastDate = new LocalDate(dateTime)

            domain match {

              case PartitionDomain.Yearly => partition = lastDate.toString("yyyy")
              case PartitionDomain.Monthly => partition = lastDate.toString("yyyy-MM")
              case PartitionDomain.Daily => partition = lastDate.toString("yyyy-MM-dd")

            }
          }
      }

      if(partition != null) {

        writeRecords(numberOfRecordsOnPart)
        releaseWriters

        // Set column labels
        saver.save(name, ".d", labels, true)(partition)

        dir =
          if(segmented == null)
            new File(path + "/" + partition + "/" + name)
          else
            new File(segmented.getPath + "/" + partition + "/" + name)

        initWriters
        numberOfRecordsOnPart = 0

      }

      var i = 0

      // Write column values
      table.columns.foreach { column => 

        column.typeOf match {

          case Datatype.Bool =>
            writers(i).write(column.bools(r))

          case Datatype.Byte =>
            writers(i).write(column.bytes(r))

          case Datatype.Char =>
            writers(i).write(column.chars(r))

          case Datatype.Double =>
            writers(i).write(column.doubles(r))

          case Datatype.Float =>
            writers(i).write(column.floats(r))

          case Datatype.Int =>
            writers(i).write(column.ints(r))

          case Datatype.Long =>
            writers(i).write(column.longs(r))

          case Datatype.Short =>
            writers(i).write(column.shorts(r))

          case Datatype.Month =>
            writers(i).write(column.months(r))

          case Datatype.Date =>
            writers(i).write(column.dates(r))

          case Datatype.Minute =>
            writers(i).write(column.minutes(r))

          case Datatype.Second =>
            writers(i).write(column.seconds(r))

          case Datatype.Time =>
            writers(i).write(column.times(r))

          case Datatype.DateTime =>
            writers(i).write(column.dateTimes(r))

          case Datatype.Timestamp =>
            writers(i).write(column.timestamps(r))

          case Datatype.Symbol =>
            symbolCompanions(i).lastEnded +=
              column.symbols(r).toString.getBytes(
                symbolCompanions(i).writer.charset).size - 1

            writers(i).write(symbolCompanions(i).lastEnded)
            symbolCompanions(i).writer.write(column.symbols(r))

          case Datatype.String =>
            stringCompanions(i).lastEnded +=
              column.strings(r).getBytes(
                stringCompanions(i).writer.charset).size

            writers(i).write(stringCompanions(i).lastEnded)
            stringCompanions(i).writer.write(column.strings(r))

        }

        i += 1

      }

      numberOfRecordsOnPart += 1

    }

    writeRecords(numberOfRecordsOnPart)
    releaseWriters

  }
}
