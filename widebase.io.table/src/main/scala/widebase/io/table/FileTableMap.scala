package widebase.io.table

import java.io.File

import org.joda.time. { LocalDate, YearMonth, Years }

import widebase.db.table. { PartitionMap, Table }
import widebase.io.column. { FileColumnLoader, FileColumnMapper }

/** Map tables from database.
 *
 * @param path of database
 *
 * @author myst3r10n
 */
abstract class FileTableMap(path: String) {

  /** Map directory table from database.
    *
    * @param name of table
    * @param parted partition name
    * @param segmented path of segment
    *
    * @return [[widebase.db.column.Table]]
   */
  def apply(name: String)
  (implicit parted: String = null, segmented: File = null) = {

    val table = new Table

    val loader = new FileColumnLoader(path)

    val labels = loader.load(name, ".d")(parted, segmented).strings

    val mapper = new FileColumnMapper(path)

    labels.foreach(label =>
      table ++= (label, mapper.map(name, label)(parted, segmented)))

    table

  }

  /** Map partitioned directory table from database by [[org.joda.time.LocalDate]]
    *
    * @param name of table
    * @param from the [[org.joda.time.LocalDate]] from
    * @param till the [[org.joda.time.LocalDate]] till
    * @param segmented path of segment
    *
    * @return [[widebase.collection.mutable.PartitionMap]]
   */
  def dates(
    name: String,
    from: LocalDate,
    till: LocalDate)(implicit segmented: File = null) = {

    val dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    var parts = new PartitionMap

    var i = from

    while(i.compareTo(till) < 0) {

      val partition = i.toString

      if((new File(dir + "/" + partition + "/" + name)).exists)
        parts += partition -> this(name)(partition, segmented)

      i = i.plusDays(1)

    }

    parts

  }

  /** Map partitioned directory table from database by [[scala.Int]]
    *
    * @param name of table
    * @param from the [[scala.Int]] from
    * @param till the [[scala.Int]] till
    * @param segmented path of segment
    *
    * @return [[widebase.collection.mutable.PartitionMap]]
   */
  def ints(
    name: String,
    from: Int,
    till: Int)(implicit segmented: File = null) = {

    val dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    var parts = new PartitionMap

    var i = from

    for(i <- from to till) {

      val partition = i.toString

      if((new File(dir + "/" + partition + "/" + name)).exists)
        parts += partition -> this(name)(partition, segmented)

    }

    parts

  }

  /** Map partitioned directory table from database by [[org.joda.time.YearMonth]]
    *
    * @param name of table
    * @param from the [[org.joda.time.YearMonth]] from
    * @param till the [[org.joda.time.YearMonth]] till
    * @param segmented path of segment
    *
    * @return [[widebase.collection.mutable.PartitionMap]]
   */
  def months(
    name: String,
    from: YearMonth,
    till: YearMonth)(implicit segmented: File = null) = {

    val dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    var parts = new PartitionMap

    var i = from

    while(i.compareTo(till) < 0) {

      val partition = i.toString

      if((new File(dir + "/" + partition + "/" + name)).exists)
        parts += partition -> this(name)(partition, segmented)

      i = i.plusMonths(1)

    }

    parts

  }

  /** Map partitioned directory table from database by [[org.joda.time.Years]]
    *
    * @param name of table
    * @param from the [[org.joda.time.Years]] from
    * @param till the [[org.joda.time.Years]] till
    * @param segmented path of segment
    *
    * @return [[widebase.collection.mutable.PartitionMap]]
   */
  def years(
    name: String,
    from: Years,
    till: Years)(implicit segmented: File = null) = {

    val dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    var parts = new PartitionMap

    var i = from

    while(i.compareTo(till) < 0) {

      val partition = i.toString

      if((new File(dir + "/" + partition + "/" + name)).exists)
        parts += partition -> this(name)(partition, segmented)

      i = i.plus(1)

    }

    parts

  }
}

