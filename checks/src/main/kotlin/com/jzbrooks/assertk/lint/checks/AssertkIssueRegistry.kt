package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class AssertkIssueRegistry : IssueRegistry() {
    override val issues =
        listOf(
            AssertJDetector.ISSUE,
            BooleanExpressionSubjectDetector.NULL_EXPR_ISSUE,
            BooleanExpressionSubjectDetector.EQUALITY_EXPR_ISSUE,
            CollectionAssertionDetector.SIZE_READ_ISSUE,
            GoogleTruthDetector.ISSUE,
            IndexDetector.ISSUE,
            KotlinAssertionDetector.ISSUE,
            MapAssertionDetector.DIRECT_READ_ISSUE,
            MapAssertionDetector.KEYS_SET_ABSENT_ISSUE,
            MapAssertionDetector.KEYS_SET_PRESENT_ISSUE,
            TestFrameworkAssertionDetector.ISSUE,
            TryCatchDetector.ISSUE,
            UnusedAssertionDetector.ISSUE,
        )

    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = 8

    override val vendor: Vendor =
        Vendor(
            vendorName = "jzbrooks",
            feedbackUrl = "https://github.com/jzbrooks/assertk-lint/issues",
            contact = "https://github.com/jzbrooks/assertk-lint",
        )
}
