data "aws_iam_policy_document" "allow_all" {
  statement {
    effect = "Allow"
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
