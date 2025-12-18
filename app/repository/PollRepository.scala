package repository

import javax.inject.{Inject, Singleton}
import slick.jdbc.PostgresProfile.api._
import models._
import scala.concurrent.{ExecutionContext, Future}
import repository.Mappings.localDateTimeColumnType
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

@Singleton
class PollRepository @Inject()(implicit ec: ExecutionContext) {

  private val db = Database.forConfig("slick.dbs.default.db")
  private val polls = TableQuery[PollsTable]
  private val questions = TableQuery[QuestionsTable]
  private val options = TableQuery[OptionsTable]
  private val answers = TableQuery[AnswersTable]
  private val votes = TableQuery[VotesTable]

  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  /** Vloží nový poll s otázkami a možnostmi */
  def insertPoll(poll: Poll): Future[Long] = {
    val status: String = poll.status.filter(_.trim.nonEmpty).getOrElse("active")
    val slug: String = poll.slug.filter(_.trim.nonEmpty).getOrElse(generateSlug(poll.title, System.currentTimeMillis()))

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
            _ <- DBIO.sequence(q.options.map(opt => options += OptionRow(questionId = qId, text = opt)))
          } yield ()
        }
      )
    } yield pollId

    db.run(action.transactionally)
  }

  /** Vrátí poll s otázkami, možnostmi a allowVote podle IP adresy */
  def getPollWithQuestionsAndOptions(pollId: Long, ipAddress: String): Future[Option[PollJson]] = {
    val query = for {
      ((q, o), a) <- questions.filter(_.pollId === pollId)
        .join(options).on(_.id === _.questionId)
        .joinLeft(answers).on { case ((_, o), a) => o.id === a.optionId }
    } yield (q, o, a)

    val submissionCountQuery = answers.filter(_.pollId === pollId).map(_.submissionId).distinct.length.result
    val votedQuery = votes.filter(v => v.pollId === pollId && v.ipAddress === ipAddress).exists.result
    val pollRowQuery = polls.filter(_.id === pollId).result.headOption

    for {
      rows <- db.run(query.result)
      totalSubmissions <- db.run(submissionCountQuery)
      voted <- db.run(votedQuery)
      pollRowOpt <- db.run(pollRowQuery)
    } yield pollRowOpt.map { p =>
      val questionsMap = rows.groupBy { case (q, _, _) => q.id }
      val questionsJson = questionsMap.toSeq
  // 1️⃣ seřadíme otázky podle ID, aby bylo pořadí stejné
  .sortBy { case (_, qRows) => qRows.head._1.id }
  .map { case (_, qRows) =>
    val q = qRows.head._1

    // 2️⃣ seskupíme možnosti podle id a seřadíme je
    val optionsMap = qRows.groupBy(_._2.id)
    val optionJsons = optionsMap.toSeq
      .sortBy(_._1) // řadíme podle option.id
      .map { case (optId, rs) =>
        val optionRow = qRows.find { case (_, opt, _) => opt.id == optId }.get._2
        OptionJson(
          id = optionRow.id,
          text = optionRow.text,
          votes = rs.map(_._3).flatten.size
        )
      }

    // 3️⃣ složíme JSON otázky
    QuestionJson(
      id = q.id,
      text = q.text,
      allowMultiple = q.allowMultiple,
      options = optionJsons,
      totalVotes = optionJsons.map(_.votes).sum
    )
  }


      PollJson(
        id = p.id,
        title = p.title,
        showResults = p.showResults,
        createdAt = p.createdAt.format(dateFormatter),
        questions = questionsJson,
        totalVotes = totalSubmissions,
        status = p.status,
        slug = p.slug,
        allowVote = Some(!voted)
      )
    }
  }

  /** Stejné jako getPoll podle slug */
  def getPollWithQuestionsAndOptionsBySlug(slug: String, clientIp: String): Future[Option[PollJson]] = {
  db.run(polls.filter(_.slug === slug).result.headOption).flatMap {
    case Some(poll) =>
      getPollWithQuestionsAndOptions(poll.id, clientIp)
    case None =>
      Future.successful(None)
  }
}


  /** Vloží hlas */
  def insertVote(pollId: Long, ipAddress: String): Future[Long] = {
    val vote = VoteRow(pollId = pollId, ipAddress = ipAddress)
    db.run((votes returning votes.map(_.id)) += vote)
  }

  /** Kontrola, zda IP už hlasovala */
  def hasVoted(pollId: Long, ipAddress: String): Future[Boolean] = {
    db.run(votes.filter(v => v.pollId === pollId && v.ipAddress === ipAddress).exists.result)
  }

  /** Smazání pollu */
  def deletePoll(id: Long): Future[Boolean] = {
    val action = for {
      _ <- answers.filter(_.pollId === id).delete
      _ <- options.filter(_.questionId in questions.filter(_.pollId === id).map(_.id)).delete
      _ <- questions.filter(_.pollId === id).delete
      deleted <- polls.filter(_.id === id).delete
    } yield deleted > 0
    db.run(action.transactionally)
  }

  /** Změna statusu pollu */
  def updateStatus(pollId: Long, newStatus: String): Future[Int] = {
    db.run(polls.filter(_.id === pollId).map(_.status).update(newStatus))
  }

  /** Pomocná metoda na generování slug */
  private def generateSlug(title: Option[String], uniqueId: Long): String = {
    val base = title.getOrElse("anketa")
      .toLowerCase
      .replaceAll("[^a-z0-9]+", "-")
      .stripPrefix("-")
      .stripSuffix("-")
      .take(50)
    s"$base-$uniqueId"
  }

def insertAnswers(pollId: Long, answersList: Seq[AnswerDto], userId: Option[Long] = None): Future[Unit] = {
  val actions = answersList.map { a =>
    DBIO.sequence(a.selectedOptionIds.map { optId =>
      answers += AnswerRow(
        id = 0, // auto-increment
        pollId = pollId,
        questionId = a.questionId,
        optionId = optId,
        userId = userId,
        createdAt = LocalDateTime.now(),
        submissionId = java.util.UUID.randomUUID().toString
      )
    })
  }
  db.run(DBIO.sequence(actions).transactionally).map(_ => ())
}





}


