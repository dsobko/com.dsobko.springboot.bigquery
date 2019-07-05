package com.dsobko.gcp.steps.rows.comparison

import cucumber.api.java.en.And
import org.springframework.beans.factory.annotation.Autowired

class TestTableDuplicatedRowsSteps(@Autowired private val service: TestTableDuplicatedRowsService) {

    @And("^I check that there are no duplicated rows (.*)$")
    fun checkDuplicatedRows(table: String) {
        service.checkDuplicatedRows()
    }


}
