package repository

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp
import java.time.LocalDateTime

object Mappings {
  implicit val localDateTimeColumnType: BaseColumnType[LocalDateTime] =
    MappedColumnType.base[LocalDateTime, Timestamp](
      ldt => Timestamp.valueOf(ldt),
      ts => ts.toLocalDateTime
    )
}
