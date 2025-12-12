package repository

import javax.inject.{Inject, Singleton}
import slick.jdbc.PostgresProfile.api._
import models._
import scala.concurrent.{ExecutionContext, Future}
import repository.Mappings.localDateTimeColumnType
import java.time.format.DateTimeFormatter

@Singleton
class PollRepository @Inject()(implicit ec: ExecutionContext) {

  private val db = Database.forConfig("slick.dbs.default.db")

  private val polls = TableQuery[PollsTable]
  private val questions = TableQuery[QuestionsTable]
  private val options = TableQuery[OptionsTable]
  private val answers = TableQuery[AnswersTable]

  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  /** Vložení nového pollu s otázkami a možnostmi */
  def insertPoll(poll: Poll): Future[Long] = {

    val status: String = poll.status match {
      case Some(s) if s.trim.nonEmpty => s
      case _ => "active"
    }

    val slug: String = poll.slug match {
      case Some(s) if s.trim.nonEmpty => s
      case _ => generateSlug(poll.title, System.currentTimeMillis())
    }

    val pollRow = PollRow(
      id = 0,
      createdAt = poll.createdAt,
      title = poll.title,
      showResults = poll.showResults,
      status = status,
      slug = slug
    )

    val action = for {
      pollId <- (polls returning polls.map(_.id)) += pollRow
      _ <- DBIO.sequence(
        poll.questions.map { q =>
          for {
            qId <- (questions returning questions.map(_.id)) += QuestionRow(
              pollId = pollId,
              text = q.text,
              allowMultiple = q.allowMultiple
            )
            _ <- DBIO.sequence(
              q.options.map(opt => options += OptionRow(questionId = qId, text = opt))
            )
          } yield ()
        }
      )
    } yield pollId

    db.run(action.transactionally)
  }

  /** Načtení pollu s otázkami, možnostmi a počtem hlasů */
  def getPollWithQuestionsAndOptions(pollId: Long): Future[Option[PollJson]] = {
    val query = for {
      ((q, o), a) <- questions
        .filter(_.pollId === pollId)
        .join(options).on(_.id === _.questionId)
        .joinLeft(answers).on { case ((_, o), a) => o.id === a.optionId }
    } yield (q, o, a)

    val submissionCountQuery = answers
      .filter(_.pollId === pollId)
      .map(_.submissionId)
      .distinct
      .length
      .result

    for {
      rows <- db.run(query.result)
      totalSubmissions <- db.run(submissionCountQuery)
      pollRowOpt <- db.run(polls.filter(_.id === pollId).result.headOption)
    } yield {
      pollRowOpt.map { p =>
        val questionsMap = rows.groupBy { case (q, _, _) => q.id }

        val questionsJson = questionsMap.map { case (_, qRows) =>
          val q = qRows.head._1

          val optionsMap = qRows.groupBy(_._2.id).map { case (optId, rs) =>
            optId -> rs.map(_._3)
          }

          val optionJsons = optionsMap.map { case (optId, answersOpt) =>
            val optionRow = qRows.find { case (_, opt, _) => opt.id == optId }.get._2
            OptionJson(
              id = optionRow.id,
              text = optionRow.text,
              votes = answersOpt.flatten.size
            )
          }.toSeq

          QuestionJson(
            id = q.id,
            text = q.text,
            allowMultiple = q.allowMultiple,
            options = optionJsons,
            totalVotes = optionJsons.map(_.votes).sum
          )
        }.toSeq

        PollJson(
          id = p.id,
          createdAt = p.createdAt.format(dateFormatter),
          title = p.title,
          showResults = p.showResults,
          questions = questionsJson,
          totalVotes = totalSubmissions,
          status = p.status,
          slug = p.slug  // správně
        )
      }
    }
  }

  /** Smazání pollu */
  def deletePoll(id: Long): Future[Boolean] = {
    val action = for {
      _ <- answers.filter(_.pollId === id).delete
      _ <- options.filter(_.questionId in questions.filter(_.pollId === id).map(_.id)).delete
      _ <- questions.filter(_.pollId === id).delete
      deletedRows <- polls.filter(_.id === id).delete
    } yield deletedRows > 0

    db.run(action.transactionally)
  }

  /** Pomocná funkce pro generování unikátního slug */
  private def generateSlug(title: Option[String], uniqueId: Long): String = {
    val base = title.getOrElse("anketa")
      .toLowerCase
      .replaceAll("[^a-z0-9]+", "-")
      .stripPrefix("-")
      .stripSuffix("-")
      .take(50)
    s"$base-$uniqueId"
  }

} // konec třídy
