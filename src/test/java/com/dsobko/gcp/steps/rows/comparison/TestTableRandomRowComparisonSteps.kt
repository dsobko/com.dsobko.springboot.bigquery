package com.dsobko.gcp.steps.rows.comparison

import cucumber.api.java.en.And
import org.springframework.beans.factory.annotation.Autowired

class TestTableRandomRowComparisonSteps(@Autowired private val service: TestTableRandomRowComparisonService) {

    @And("^I compare random rows (.*)$")
    fun compareRandomRows(table: String) {
        service.compareRandomRows()
    }


}
