package io.underscore.tz

import scodec._
import scodec.codecs._
import scodec.bits._

// See: man tzfile
object TzCodecs extends App {

  case class TzFileHeader(
    version   : Int, // 0, '2', or '3'
    future    : Vector[Int],
    utcCount  : Long,
    stdCount  : Long,
    leapCount : Long,
    timeCount : Long,
    typeCount : Long,
    abbrCount : Long)

 case class TimeTypeInfo(
   utcOffsetSeconds : Int,
   dstFlag          : Int,
   abbrIndex        : Int
 ) {
   lazy val isDst: Boolean = dstFlag != 0
 }

 case class LeapInfo(
   occurs       : Long,
   totalApplied : Long)

  case class TzFile(
    header             : TzFileHeader,
    transitionTimes    : Vector[Int],
    transitionTypes    : Vector[Int],
    typeInfos          : Vector[TimeTypeInfo],
    abbreviations      : Vector[Int],
    leapInfos          : Vector[LeapInfo],
    standardIndicators : Vector[Int],
    utcIndicators      : Vector[Int]
  )

  lazy val tzFile: Codec[TzFile] =
    header.flatPrepend { hdr =>
      ( ("transition times"         | vectorOfN(provide(hdr.timeCount.toInt), int32)    ) ::
        ("local time chars"         | vectorOfN(provide(hdr.timeCount.toInt), uint8 )   ) ::
        ("time types"               | vectorOfN(provide(hdr.typeCount.toInt), ttInfo)   ) ::
        ("timezone abbreviations"   | vectorOfN(provide(hdr.abbrCount.toInt), int8)     ) ::
        ("leap second info"         | vectorOfN(provide(hdr.leapCount.toInt), leapInfo) ) ::
        ("standard/wall indicators" | vectorOfN(provide(hdr.stdCount.toInt),  uint8)    ) ::
        ("UTC/local indicators"     | vectorOfN(provide(hdr.utcCount.toInt),  uint8)    ) )
    }.as[TzFile]

  implicit val header: Codec[TzFileHeader] = (
    ("magic file identifier"             | constant(magicValue)     ) ::
    ("format version"                    | fixedSizeBytes(1, uint8) ) ::
    ("reserved for future use"           | vectorOfN(provide(15), uint8) ) ::
    ("count of UTC/local indicators"     | uint32                   ) ::
    ("count of standard/wall indicators" | uint32                   ) ::
    ("count of leap seconds"             | uint32                   ) ::
    ("number of transition times"        | uint32                   ) ::
    ("count of local time types"         | uint32                   ) ::
    ("number of abbreciations strings"   | uint32                   )
  ).as[TzFileHeader]

  implicit val ttInfo: Codec[TimeTypeInfo] = (
    ("seconds added to UTC"               | int32) ::
    ("summer time? (non-zero for true)"   | uint8) ::
    ("index into time zone abbreviations" | uint8)
  ).as[TimeTypeInfo]

  implicit val leapInfo: Codec[LeapInfo] = (
    ("time at which leap second occurs"                      | uint32) ::
    ("total number of seconds to be applied after occurance" | uint32)
  ).as[LeapInfo]

  lazy val magicValue = ByteVector("TZif".getBytes("utf-8"))
  lazy val zeros = hex"000000000000000000000000000000"

  def read() = {
    import java.nio.file.{FileSystems, Path, Files}
    //val path = FileSystems.getDefault.getPath("/usr/share/zoneinfo/Europe/London")
    //val path = FileSystems.getDefault.getPath("/usr/share/zoneinfo/UCT")
    val path = FileSystems.getDefault.getPath("/usr/share/zoneinfo/America/New_York")
    val in = Files.newInputStream(path)
    try {
      val bv = BitVector.fromInputStream(in)
      println(tzFile.decode(bv))
    } finally in.close
  }

  read()

}

