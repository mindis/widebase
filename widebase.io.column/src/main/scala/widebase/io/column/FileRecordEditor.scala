package widebase.io.column

import java.io. { File, RandomAccessFile }
import java.sql.Timestamp

import org.joda.time. {

  LocalDate,
  LocalDateTime,
  LocalTime,
  Minutes,
  Seconds,
  YearMonth

}

import scala.collection.mutable.ArrayBuffer

import widebase.data.Datatype

import widebase.db.column.MixedTypeException
import widebase.io.VariantReader
import widebase.io.file.FileVariantWriter

import widebase.io.filter. {

  InvalidMagicException,
  MagicId,
  MagicNotFoundException,
  StreamFilter,
  WrongMagicException

}

/** Edits records within column of directory table.
 *
 * @param path of database
 *
 * @author myst3r10n
 */
class FileRecordEditor(path: String) {

  import widebase.data

  /** Sets values within column of directory table.
    *
    * @param name of table
    * @param label of column
    * @param field of column
    * @param value to add
    * @param parted partition name
    * @param segmented path of segment
   */
  def set(
    name: String,
    label: Any,
    field: Int,
    value: Any)
    (implicit parted: String = null, segmented: File = null) {

    if(value.isInstanceOf[Symbol])
      throw new UnsupportedOperationException(Datatype.Symbol.toString)
    if(value.isInstanceOf[String])
      throw new UnsupportedOperationException(Datatype.String.toString)

    var dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    if(parted != null)
      dir += "/" + parted

    val filename = dir + "/" + name + "/" + label.toString

    val reader =
      new VariantReader(
        new RandomAccessFile(filename, "r").getChannel,
        StreamFilter.None) {

      override val charset = props.charsets.set

      override def capacity = props.capacities.set
      override def order = props.orders.set

    }

    // Read magic
    reader.mode = Datatype.String
    val magic =
      reader.readString(MagicId.Column.toString.getBytes(reader.charset).size)

    if(magic.isEmpty)
      throw MagicNotFoundException(filename)

    try {

      if(MagicId.withName(magic) != MagicId.Column)
        throw WrongMagicException(filename, magic)

    } catch {
      case e: NoSuchElementException =>
        throw InvalidMagicException(filename, magic)
    }

    // Read column type
    if(reader.mode != Datatype.Byte)
      reader.mode = Datatype.Byte

    val typeOf = Datatype(reader.read)

    if(typeOf == Datatype.Symbol)
      throw new UnsupportedOperationException(Datatype.Symbol.toString)

    if(typeOf == Datatype.String)
      throw new UnsupportedOperationException(Datatype.String.toString)

    value match {

      case value: Boolean =>
        if(typeOf != Datatype.Bool)
          throw MixedTypeException(Datatype.Bool, typeOf)

      case value: Byte =>
        if(typeOf != Datatype.Byte)
          throw MixedTypeException(Datatype.Byte, typeOf)

      case value: Char =>
        if(typeOf != Datatype.Char)
          throw MixedTypeException(Datatype.Char, typeOf)

      case value: Double =>
        if(typeOf != Datatype.Double)
          throw MixedTypeException(Datatype.Double, typeOf)

      case value: Float =>
        if(typeOf != Datatype.Float)
          throw MixedTypeException(Datatype.Float, typeOf)

      case value: Int =>
        if(typeOf != Datatype.Int)
          throw MixedTypeException(Datatype.Int, typeOf)

      case value: Long =>
        if(typeOf != Datatype.Long)
          throw MixedTypeException(Datatype.Long, typeOf)

      case value: Short =>
        if(typeOf != Datatype.Short)
          throw MixedTypeException(Datatype.Short, typeOf)

      case value: YearMonth =>
        if(typeOf != Datatype.Month)
          throw MixedTypeException(Datatype.Month, typeOf)

      case value: LocalDate =>
        if(typeOf != Datatype.Date)
          throw MixedTypeException(Datatype.Date, typeOf)

      case value: Minutes =>
        if(typeOf != Datatype.Minute)
          throw MixedTypeException(Datatype.Minute, typeOf)

      case value: Seconds =>
        if(typeOf != Datatype.Second)
          throw MixedTypeException(Datatype.Second, typeOf)

      case value: LocalTime =>
        if(typeOf != Datatype.Time)
          throw MixedTypeException(Datatype.Time, typeOf)

      case value: LocalDateTime =>
        if(typeOf != Datatype.DateTime)
          throw MixedTypeException(Datatype.DateTime, typeOf)

      case value: Timestamp =>
        if(typeOf != Datatype.Timestamp)
          throw MixedTypeException(Datatype.Timestamp, typeOf)

    }

    // Read column length
    reader.mode = Datatype.Int
    val records = reader.readInt

    reader.close

    val channel = new RandomAccessFile(filename, "rw").getChannel
    channel.tryLock
    val writer = new FileVariantWriter(channel) {

      override val charset = props.charsets.set

      override def capacity = props.capacities.set
      override def order = props.orders.set

    }

    writer.mode = Datatype.Byte

    val offset = (MagicId.Column.toString.getBytes).size + 1 + data.sizeOf.int

    typeOf match {

      case typeOf if
        typeOf == Datatype.None ||
        typeOf == Datatype.Bool ||
        typeOf == Datatype.Byte ||
        typeOf == Datatype.Month => writer.position = offset + field

      case Datatype.Char => writer.position = offset +
        (field * data.sizeOf.char)
      case Datatype.Double => writer.position = offset +
        (field * data.sizeOf.double)
      case Datatype.Float => writer.position = offset +
        (field * data.sizeOf.float)

      case typeOf if
        typeOf == Datatype.Int ||
        typeOf == Datatype.Date ||
        typeOf == Datatype.Minute ||
        typeOf == Datatype.Second ||
        typeOf == Datatype.Time => writer.position = offset +
          (field * data.sizeOf.int)

      case typeOf if
        typeOf == Datatype.Long ||
        typeOf == Datatype.DateTime ||
        typeOf == Datatype.Timestamp => writer.position = offset +
          (field * data.sizeOf.long)

      case Datatype.Short => writer.position = offset +
        (field * data.sizeOf.short)

    }

    writer.mode = typeOf

    value match {

      case value: Boolean => writer.write(value)
      case value: Byte => writer.write(value)
      case value: Char => writer.write(value)
      case value: Double => writer.write(value)
      case value: Float => writer.write(value)
      case value: Int => writer.write(value)
      case value: Long => writer.write(value)
      case value: Short => writer.write(value)
      case value: YearMonth => writer.write(value)
      case value: LocalDate => writer.write(value)
      case value: Minutes => writer.write(value)
      case value: Seconds => writer.write(value)
      case value: LocalTime => writer.write(value)
      case value: LocalDateTime => writer.write(value)
      case value: Timestamp => writer.write(value)

    }

    writer.close

  }

  /** Upserts records into column of directory table.
    *
    * @param name of table
    * @param records the records to add
    * @param parted partition symbol
    * @param segmented path of segment
   */
  def upsert(
    name: String,
    records: ArrayBuffer[_]*)
    (implicit parted: String = null, segmented: File = null) {

    val loader = new FileColumnLoader(path)

    val labels = loader.load(name, ".d")(parted, segmented)

    var i = 0

    records.foreach { record =>

      i = 0

      labels.foreach { label =>

        append(name, label, record(i))(parted, segmented)

        i += 1

      }
    }
  }

  /** Adds values within column of directory table.
    *
    * @param name of table
    * @param label label of column
    * @param value the value to add
    * @param parted partition name
    * @param segmented path of segment
   */
  def append(
    name: String,
    label: Any,
    value: Any)
    (implicit parted: String = null, segmented: File = null) {

    var dir =
      if(segmented == null)
        path
      else
        segmented.getPath

    if(parted != null)
      dir += "/" + parted

    var filename = dir + "/" + name + "/" + label.toString

    val reader = new VariantReader(
      new RandomAccessFile(filename, "r").getChannel,
      StreamFilter.None) {

      override val charset = props.charsets.append

      override def capacity = props.capacities.append
      override def order = props.orders.append

    }

    // Read magic
    reader.mode = Datatype.String
    val magic =
      reader.readString(MagicId.Column.toString.getBytes(reader.charset).size)

    if(magic.isEmpty)
      throw MagicNotFoundException(filename)

    try {

      if(MagicId.withName(magic) != MagicId.Column)
        throw WrongMagicException(filename, magic)

    } catch {
      case e: NoSuchElementException =>
        throw InvalidMagicException(filename, magic)
    }

    // Read column type
    if(reader.mode != Datatype.Byte)
      reader.mode = Datatype.Byte

    val typeOf = Datatype(reader.read)

    value match {

      case value: Boolean =>
        if(typeOf != Datatype.Bool)
          throw MixedTypeException(Datatype.Bool, typeOf)

      case value: Byte =>
        if(typeOf != Datatype.Byte)
          throw MixedTypeException(Datatype.Byte, typeOf)

      case value: Char =>
        if(typeOf != Datatype.Char)
          throw MixedTypeException(Datatype.Char, typeOf)

      case value: Double =>
        if(typeOf != Datatype.Double)
          throw MixedTypeException(Datatype.Double, typeOf)

      case value: Float =>
        if(typeOf != Datatype.Float)
          throw MixedTypeException(Datatype.Float, typeOf)

      case value: Int =>
        if(typeOf != Datatype.Int)
          throw MixedTypeException(Datatype.Int, typeOf)

      case value: Long =>
        if(typeOf != Datatype.Long)
          throw MixedTypeException(Datatype.Long, typeOf)

      case value: Short =>
        if(typeOf != Datatype.Short)
          throw MixedTypeException(Datatype.Short, typeOf)

      case value: YearMonth =>
        if(typeOf != Datatype.Month)
          throw MixedTypeException(Datatype.Month, typeOf)

      case value: LocalDate =>
        if(typeOf != Datatype.Date)
          throw MixedTypeException(Datatype.Date, typeOf)

      case value: Minutes =>
        if(typeOf != Datatype.Minute)
          throw MixedTypeException(Datatype.Minute, typeOf)

      case value: Seconds =>
        if(typeOf != Datatype.Second)
          throw MixedTypeException(Datatype.Second, typeOf)

      case value: LocalTime =>
        if(typeOf != Datatype.Time)
          throw MixedTypeException(Datatype.Time, typeOf)

      case value: LocalDateTime =>
        if(typeOf != Datatype.DateTime)
          throw MixedTypeException(Datatype.DateTime, typeOf)

      case value: Timestamp =>
        if(typeOf != Datatype.Timestamp)
          throw MixedTypeException(Datatype.Timestamp, typeOf)

      case value: Symbol =>
        if(typeOf != Datatype.Symbol)
          throw MixedTypeException(Datatype.Symbol, typeOf)

      case value: String =>
        if(typeOf != Datatype.String)
          throw MixedTypeException(Datatype.String, typeOf)

    }

    // Read column length
    reader.mode = Datatype.Int
    val records = reader.readInt

    reader.close

    val channel = new RandomAccessFile(filename, "rw").getChannel
    channel.tryLock
    var writer = new FileVariantWriter(channel) {

      override val charset = props.charsets.append

      override def capacity = props.capacities.append
      override def order = props.orders.append

    }

    writer.mode = Datatype.Byte
    writer.position = (MagicId.Column.toString.getBytes).size + 1

    // Write column length
    writer.mode = Datatype.Int
    writer.write(records + 1)
    writer.flush

    var companion: FileVariantWriter = null

    if(typeOf == Datatype.Symbol || typeOf == Datatype.String) {

      if(typeOf == Datatype.Symbol)
        filename += ".sym"
      else if(typeOf == Datatype.String)
        filename += ".str"

      writer.mode = Datatype.Long

      var channel = new RandomAccessFile(filename, "rw").getChannel
      channel.tryLock
      companion = new FileVariantWriter(channel) {

        override val charset = props.charsets.append

        override def capacity = props.capacities.append
        override def order = props.orders.append

      }
    }

    writer.mode = Datatype.Byte
    writer.position = writer.size

    if(companion == null)
      writer.mode = typeOf
    else {

      companion.mode = Datatype.Byte
      companion.position = companion.size
      companion.mode = typeOf

    }

    value match {

      case value: Boolean => writer.write(value)
      case value: Byte => writer.write(value)
      case value: Char => writer.write(value)
      case value: Double => writer.write(value)
      case value: Float => writer.write(value)
      case value: Int => writer.write(value)
      case value: Long => writer.write(value)
      case value: Short => writer.write(value)
      case value: YearMonth => writer.write(value)
      case value: LocalDate => writer.write(value)
      case value: Minutes => writer.write(value)
      case value: Seconds => writer.write(value)
      case value: LocalTime => writer.write(value)
      case value: LocalDateTime => writer.write(value)
      case value: Timestamp => writer.write(value)
      case value: Symbol =>
        writer.mode = Datatype.Long
        writer.write((companion.size +
          value.toString.getBytes(companion.charset).size - 1))
        companion.write(value, false)

      case value: String =>
        writer.mode = Datatype.Long
        writer.write(companion.size + value.getBytes(companion.charset).size)
        companion.write(value, false)

    }

    if(companion != null)
      companion.close

    writer.close

  }
}

