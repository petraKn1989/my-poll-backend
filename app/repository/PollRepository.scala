package repository

import javax.inject.{Inject, Singleton}
import slick.jdbc.SQLiteProfile.api._
import models._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PollRepository @Inject()(implicit ec: ExecutionContext) {

  private val db = Database.forConfig("slick.dbs.default.db")

  private val polls = TableQuery[PollsTable]
  private val questions = TableQuery[QuestionsTable]
  private val options = TableQuery[OptionsTable]
  private val answers = TableQuery[AnswersTable]

  /** Vložení nového pollu s otázkami a možnostmi */
  def insertPoll(poll: Poll): Future[Long] = {
    val action = for {
      pollId <- (polls returning polls.map(_.id)) += PollRow(
        createdAt = poll.createdAt
      )
      _ <- DBIO.sequence(
        poll.questions.map { q =>
          for {
            qId <- (questions returning questions.map(_.id)) += QuestionRow(
              pollId = pollId,
              text = q.text,
              allowMultiple = q.allowMultiple
            )
            _ <- DBIO.sequence(
              q.options.map { opt =>
                options += OptionRow(
                  questionId = qId,
                  text = opt
                )
              }
            )
          } yield ()
        }
      )
    } yield pollId

    db.run(action.transactionally)
  }

  /** Načtení pollu s otázkami, možnostmi a počtem hlasů */
def getPollWithQuestionsAndOptions(pollId: Long): Future[Option[PollJson]] = {

  // 1️⃣ JOIN otázek, možností a odpovědí
  val query = for {
    ((q, o), a) <- questions
      .filter(_.pollId === pollId)
      .join(options).on(_.id === _.questionId)
      .joinLeft(answers).on { case ((q, o), a) => o.id === a.optionId }
  } yield (q, o, a)

  // 2️⃣ Počet unikátních hlasování podle submissionId
  val submissionCountQuery =
    answers
      .filter(_.pollId === pollId)
      .map(_.submissionId)
      .distinct
      .length
      .result

  for {
    rows <- db.run(query.result)
    totalSubmissions <- db.run(submissionCountQuery)
    pollRow <- db.run(polls.filter(_.id === pollId).result.headOption)
  } yield {

    if (rows.isEmpty || pollRow.isEmpty) {
      None
    } else {
      val questionsMap = rows.groupBy { case (q, _, _) => q.id }

      val questionsJson = questionsMap.map { case (_, qRows) =>
        val q = qRows.head._1

        // seskupení možností podle optionId
        val optionsMap = qRows.groupBy(_._2.id).map { case (optId, rs) =>
          optId -> rs.map(_._3)
        }

        val optionJsons = optionsMap.map { case (optId, answersOpt) =>
          val optionRow = qRows.find { case (_, opt, _) => opt.id == optId }.get._2
          OptionJson(
            id = optionRow.id,
            text = optionRow.text,
            votes = answersOpt.flatten.size // počet hlasů pro tuto možnost
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

      Some(
        PollJson(
          id = pollRow.get.id,
          createdAt = pollRow.get.createdAt,
          questions = questionsJson,
          totalVotes = totalSubmissions // počet unikátních hlasování
        )
      )
    }
  }
}






}
