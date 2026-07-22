data "aws_iam_policy_document" "allow_all" {
  statement {
    actions = ["*"]
    principals {
      identifiers = ["*"]
      type        = "AWS"
    }
    resources = ["*"]
  }
}
