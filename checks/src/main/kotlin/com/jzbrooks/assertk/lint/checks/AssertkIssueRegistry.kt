package com.jzbrooks.assertk.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

class AssertkIssueRegistry : IssueRegistry() {
    override val issues =
        listOf(
            TestFrameworkAssertionDetector.ISSUE,
            AssertJDetector.ISSUE,
            GoogleTruthDetector.ISSUE,
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
