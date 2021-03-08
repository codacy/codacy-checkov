import $ivy.`com.codacy::codacy-engine-scala-seed:5.0.3`

import com.codacy.plugins.api.results.Pattern
import com.codacy.plugins.api.results.Result
import com.codacy.plugins.api.results.Tool

import com.codacy.plugins.api._
import play.api.libs.json.Json

case class CheckovCheck(
    Id: String,
    Type: String,
    Entity: String,
    Policy: String,
    IaC: String
)
implicit val checkovCheckRW = upickle.default.macroRW[CheckovCheck]

val version = os
  .read(os.pwd / "requirements.txt")
  .linesIterator
  .collectFirst { case s"checkov==$version" =>
    version.trim
  }
  .get

val lines = os
  .proc("checkov", "-l")
  .call()
  .out
  .lines()
  .view
  .map(_.split('|').drop(2).map(_.trim))

val ids = lines.head.zipWithIndex.toMap

val checkovChecks =
  lines
    .drop(2)
    .filter(_.length == ids.size)
    .map(arr =>
      CheckovCheck(
        Id = arr(ids("Id")),
        Type = arr(ids("Type")),
        Entity = arr(ids("Entity")),
        Policy = arr(ids("Policy")),
        IaC = arr(ids("IaC"))
      )
    )
    .groupBy(_.Id)
    .map(_._2.head)

val patternSpecifications = checkovChecks.map(check =>
  Pattern.Specification(
    Pattern.Id(check.Id),
    Result.Level.Warn,
    Pattern.Category.BestPractice,
    None
  )
)

val patternDescriptions = checkovChecks.map(check =>
  Pattern.Description(
    Pattern.Id(check.Id),
    Pattern.Title(check.Policy),
    None,
    None
  )
)

val specification = Tool.Specification(
  Tool.Name("checkov"),
  Some(Tool.Version(version)),
  patternSpecifications.toSet
)

os.write.over(
  os.pwd / "docs" / "patterns.json",
  Json.prettyPrint(Json.toJson(specification)) + "\n"
)
os.write.over(
  os.pwd / "docs" / "description" / "description.json",
  Json.prettyPrint(Json.toJson(patternDescriptions)) + "\n"
)
