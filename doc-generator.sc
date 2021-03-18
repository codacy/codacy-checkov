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
    IaC: String,
    Url: Option[String]
)
implicit val checkovCheckRW = upickle.default.macroRW[CheckovCheck]

val version = os
  .read(os.pwd / "requirements.txt")
  .linesIterator
  .collectFirst { case s"checkov==$version" =>
    version.trim
  }
  .get

/*
 * We want to parse an output like this:
 *
 * |    | Id        | Type     | Entity                                | Policy                                                                              | IaC            |
 * |----|-----------|----------|---------------------------------------|-------------------------------------------------------------------------------------|----------------|
 * |  0 | CKV_AWS_1 | data     | aws_iam_policy_document               | Ensure IAM policies that allow full "*-*" administrative privileges are not created | Terraform      |
 * |  1 | CKV_AWS_1 | resource | serverless_aws                        | Ensure IAM policies that allow full "*-*" administrative privileges are not created | serverless     |
 * |  2 | CKV_AWS_2 | resource | aws_lb_listener                       | Ensure ALB protocol is HTTPS                                                        | Terraform      |
 * |  3 | CKV_AWS_2 | resource | AWS::ElasticLoadBalancingV2::Listener | Ensure ALB protocol is HTTPS                                                        | Cloudformation |
 * |  4 | CKV_AWS_3 | resource | aws_ebs_volume                        | Ensure all data stored in the EBS is securely encrypted                             | Terraform      |
 * |  5 | CKV_AWS_3 | resource | AWS::EC2::Volume                      | Ensure all data stored in the EBS is securely encrypted                             | Cloudformation |
 * |  6 | CKV_AWS_5 | resource | aws_elasticsearch_domain              | Ensure all data stored in the Elasticsearch is securely encrypted at rest           | Terraform      |
 * |  7 | CKV_AWS_5 | resource | AWS::Elasticsearch::Domain            | Ensure all data stored in the Elasticsearch is securely encrypted at rest           | Cloudformation |
 * |  8 | CKV_AWS_6 | resource | aws_elasticsearch_domain              | Ensure all Elasticsearch has node-to-node encryption enabled                        | Terraform      |
 *
 * The idea is to split by `|` character, and take the fields by column index, based on the indexes of the head.
 * This allows the doc-generator to be robust in case they insert new columns in the middle.
 */

val lines = os
  .proc("checkov", "-l")
  .call()
  .out
  .lines()
  .view
  .map(_.split('|').drop(2).map(_.trim))

val guidelines = ujson
  .read(
    requests.get("https://www.bridgecrew.cloud/api/v1/guidelines")
  )("guidelines")
  .obj

val ids = lines.head.zipWithIndex.toMap

val checkovChecks =
  lines
    .drop(2)
    .filter(_.length == ids.size)
    .map { arr =>
      val id = arr(ids("Id"));
      CheckovCheck(
        Id = id,
        Type = arr(ids("Type")),
        Entity = arr(ids("Entity")),
        Policy = arr(ids("Policy")),
        IaC = arr(ids("IaC")),
        Url = guidelines.get(id).map(_.str)
      )
    }
    .groupBy(_.Id)
    .map(_._2.head)

def categoryAndSubcategoryOf(patternId: String): (Pattern.Category, Option[Pattern.Subcategory]) = patternId match {
  case "CKV_AWS_24" => (Pattern.Category.Security, None)
  case _ => (Pattern.Category.ErrorProne, None)
}

val patternSpecifications = checkovChecks.map { check =>
  val (category, subcategory) = categoryAndSubcategoryOf(check.Id)
  Pattern.Specification(
    Pattern.Id(check.Id),
    Result.Level.Warn,
    category,
    subcategory,
    enabled = true
  )
}

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

os.remove.all(os.pwd / "docs" / "description")

os.write.over(
  os.pwd / "docs" / "description" / "description.json",
  Json.prettyPrint(Json.toJson(patternDescriptions)) + "\n",
  createFolders = true
)

checkovChecks.collect { case CheckovCheck(id, _, _, _, _, Some(url)) =>
  os.write.over(
    os.pwd / "docs" / "description" / s"$id.md",
    s"More information [here]($url).\n"
  )
}
