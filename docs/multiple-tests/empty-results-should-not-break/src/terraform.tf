data "aws_iam_policy_document" "example" {
  statement {
    effect = "Deny"
    actions = [
        "lambda:CreateFunction",
        "lambda:CreateEventSourceMapping",
        "dynamodb:CreateTable",
    ]
    principals {
      identifiers = ["*"]
      type        = "AWS"
    }
    resources = ["foo"]
  }
}
