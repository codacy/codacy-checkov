data "aws_iam_policy_document" "example" {
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
