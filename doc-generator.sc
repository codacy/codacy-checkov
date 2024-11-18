//> using scala "2"
//> using dep com.codacy::codacy-engine-scala-seed:6.1.3
//> using dep com.lihaoyi::os-lib:0.10.2
//> using dep com.lihaoyi::upickle:3.3.1
//> using dep com.lihaoyi::requests:0.8.3

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
implicit val checkovCheckRW: upickle.default.ReadWriter[CheckovCheck] =
  upickle.default.macroRW[CheckovCheck]

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
    requests.get("https://www.bridgecrew.cloud/api/v2/guidelines")
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

def categoryAndSubcategoryOf(
    patternId: String
): (Pattern.Category, Option[Pattern.Subcategory], Option[Pattern.ScanType.Value]) = patternId match {
  case id if id.startsWith("CKV_SECRET_") =>
    (Pattern.Category.Security, Some(Pattern.Subcategory.Cryptography), Some(Pattern.ScanType.Secrets))
  case "CKV_AWS_1" | "CKV_AWS_9" | "CKV_AWS_10" | "CKV_AWS_11" | "CKV_AWS_12" |
      "CKV_AWS_13" | "CKV_AWS_14" | "CKV_AWS_15" | "CKV_AWS_17" | "CKV_AWS_20" |
      "CKV_AWS_32" | "CKV_AWS_33" | "CKV_AWS_40" | "CKV_AWS_41" | "CKV_AWS_45" |
      "CKV_AWS_46" | "CKV_AWS_53" | "CKV_AWS_54" | "CKV_AWS_55" | "CKV_AWS_56" |
      "CKV_AWS_57" | "CKV_AWS_59" | "CKV_AWS_60" | "CKV_AWS_61" | "CKV_AWS_62" |
      "CKV_AWS_63" | "CKV_AWS_70" | "CKV_AWS_87" | "CKV_AWS_89" | "CKV_AWS_93" |
      "CKV_AWS_100" | "CKV_AWS_107" | "CKV_AWS_108" | "CKV_AWS_109" |
      "CKV_AWS_110" | "CKV_AWS_111" | "CKV_AZURE_1" | "CKV_AZURE_5" |
      "CKV_AZURE_9" | "CKV_AZURE_10" | "CKV_AZURE_11" | "CKV_AZURE_13" |
      "CKV_AZURE_34" | "CKV_AZURE_35" | "CKV_AZURE_39" | "CKV_AZURE_40" |
      "CKV_AZURE_41" | "CKV_AZURE_42" | "CKV_AZURE_45" | "CKV_AZURE_49" |
      "CKV_GCP_2" | "CKV_GCP_3" | "CKV_GCP_4" | "CKV_GCP_7" | "CKV_GCP_11" |
      "CKV_GCP_13" | "CKV_GCP_15" | "CKV_GCP_18" | "CKV_GCP_19" | "CKV_GCP_28" |
      "CKV_GCP_29" | "CKV_GCP_41" | "CKV_GCP_42" | "CKV_GCP_44" | "CKV_GCP_45" |
      "CKV_GCP_46" | "CKV_GCP_47" | "CKV_GCP_48" | "CKV_GCP_49" | "CKV_GCP_60" |
      "CKV_GIT_1" | "CKV_LIN_1" | "CKV_LIN_2" =>
    (Pattern.Category.Security, Some(Pattern.Subcategory.Auth), Some(Pattern.ScanType.IaC))
  case "CKV_AWS_18" | "CKV_AWS_35" | "CKV_AWS_36" | "CKV_AWS_37" |
      "CKV_AWS_48" | "CKV_AWS_50" | "CKV_AWS_66" | "CKV_AWS_67" | "CKV_AWS_71" |
      "CKV_AWS_75" | "CKV_AWS_76" | "CKV_AWS_80" | "CKV_AWS_84" | "CKV_AWS_85" |
      "CKV_AWS_86" | "CKV_AWS_91" | "CKV_AWS_92" | "CKV_AWS_95" |
      "CKV_AWS_101" | "CKV_AWS_104" | "CKV_AZURE_4" | "CKV_AZURE_12" |
      "CKV_AZURE_23" | "CKV_AZURE_24" | "CKV_AZURE_25" | "CKV_AZURE_26" |
      "CKV_AZURE_27" | "CKV_AZURE_30" | "CKV_AZURE_31" | "CKV_AZURE_33" |
      "CKV_AZURE_37" | "CKV_AZURE_38" | "CKV_GCP_1" | "CKV_GCP_26" |
      "CKV_GCP_51" | "CKV_GCP_52" | "CKV_GCP_53" | "CKV_GCP_54" | "CKV_GCP_55" |
      "CKV_GCP_56" | "CKV_GCP_57" | "CKV_GCP_62" | "CKV_GCP_63" =>
    (Pattern.Category.Security, Some(Pattern.Subcategory.Visibility), Some(Pattern.ScanType.IaC))
  case "CKV_AWS_2" | "CKV_AWS_3" | "CKV_AWS_5" | "CKV_AWS_6" | "CKV_AWS_7" |
      "CKV_AWS_8" | "CKV_AWS_16" | "CKV_AWS_19" | "CKV_AWS_22" | "CKV_AWS_24" |
      "CKV_AWS_25" | "CKV_AWS_26" | "CKV_AWS_27" | "CKV_AWS_29" | "CKV_AWS_30" |
      "CKV_AWS_31" | "CKV_AWS_34" | "CKV_AWS_38" | "CKV_AWS_39" | "CKV_AWS_42" |
      "CKV_AWS_43" | "CKV_AWS_44" | "CKV_AWS_47" | "CKV_AWS_49" | "CKV_AWS_51" |
      "CKV_AWS_52" | "CKV_AWS_58" | "CKV_AWS_64" | "CKV_AWS_68" | "CKV_AWS_69" |
      "CKV_AWS_72" | "CKV_AWS_74" | "CKV_AWS_77" | "CKV_AWS_78" | "CKV_AWS_79" |
      "CKV_AWS_81" | "CKV_AWS_82" | "CKV_AWS_83" | "CKV_AWS_88" | "CKV_AWS_90" |
      "CKV_AWS_94" | "CKV_AWS_96" | "CKV_AWS_97" | "CKV_AWS_98" | "CKV_AWS_99" |
      "CKV_AWS_102" | "CKV_AWS_103" | "CKV_AWS_105" | "CKV_AWS_106" |
      "CKV_AZURE_2" | "CKV_AZURE_3" | "CKV_AZURE_6" | "CKV_AZURE_7" |
      "CKV_AZURE_8" | "CKV_AZURE_14" | "CKV_AZURE_15" | "CKV_AZURE_16" |
      "CKV_AZURE_17" | "CKV_AZURE_18" | "CKV_AZURE_20" | "CKV_AZURE_21" |
      "CKV_AZURE_22" | "CKV_AZURE_28" | "CKV_AZURE_29" | "CKV_AZURE_36" |
      "CKV_AZURE_43" | "CKV_AZURE_44" | "CKV_AZURE_47" | "CKV_AZURE_48" |
      "CKV_AZURE_50" | "CKV_AZURE_52" | "CKV_AZURE_53" | "CKV_AZURE_54" |
      "CKV_GCP_6" | "CKV_GCP_8" | "CKV_GCP_12" | "CKV_GCP_16" | "CKV_GCP_17" |
      "CKV_GCP_20" | "CKV_GCP_24" | "CKV_GCP_25" | "CKV_GCP_27" | "CKV_GCP_30" |
      "CKV_GCP_31" | "CKV_GCP_32" | "CKV_GCP_33" | "CKV_GCP_34" | "CKV_GCP_35" |
      "CKV_GCP_36" | "CKV_GCP_37" | "CKV_GCP_38" | "CKV_GCP_39" | "CKV_GCP_40" |
      "CKV_GCP_43" | "CKV_GCP_50" | "CKV_GCP_58" | "CKV_GCP_59" | "CKV_K8S_1" |
      "CKV_K8S_2" | "CKV_K8S_3" | "CKV_K8S_4" | "CKV_K8S_5" | "CKV_K8S_6" |
      "CKV_K8S_7" | "CKV_K8S_16" | "CKV_K8S_17" | "CKV_K8S_18" | "CKV_K8S_19" |
      "CKV_K8S_20" | "CKV_K8S_21" | "CKV_K8S_22" | "CKV_K8S_23" | "CKV_K8S_24" |
      "CKV_K8S_25" | "CKV_K8S_26" | "CKV_K8S_27" | "CKV_K8S_28" | "CKV_K8S_29" |
      "CKV_K8S_30" | "CKV_K8S_31" | "CKV_K8S_32" | "CKV_K8S_33" | "CKV_K8S_34" |
      "CKV_K8S_35" | "CKV_K8S_36" | "CKV_K8S_37" | "CKV_K8S_38" | "CKV_K8S_39" |
      "CKV_K8S_41" | "CKV_K8S_42" | "CKV_K8S_43" | "CKV_K8S_44" |
      "CKV_K8S_45" =>
    (Pattern.Category.Security, None, Some(Pattern.ScanType.IaC))
  case _ => (Pattern.Category.ErrorProne, None, Some(Pattern.ScanType.IaC))
}

val patternSpecifications = checkovChecks.map { check =>
  val (category, subcategory, scanType) = categoryAndSubcategoryOf(check.Id)
  Pattern.Specification(
    Pattern.Id(check.Id),
    Result.Level.Warn,
    category,
    subcategory,
    scanType,
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

// historically, compiled md files were checked in and then discarded at build time
// by this generator. With AI-generated docs, we will keep those checked in so resetting
// the directory is not necessary and in fact unhelpful.
// os.remove.all(os.pwd / "docs" / "description")

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
