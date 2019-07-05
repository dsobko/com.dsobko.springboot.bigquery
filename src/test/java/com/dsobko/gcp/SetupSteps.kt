package com.dsobko.gcp

import cucumber.api.Scenario
import cucumber.api.java.Before

import org.slf4j.LoggerFactory.getLogger


@CucumberStep
class SetupSteps {

    private val log = getLogger(SetupSteps::class.java)


    @Before
    fun putScenarioNameToMDCContextMap(scenario: Scenario) {
        val scenarioName = scenario.name
        //        MDC.put("scenarioName", scenarioName);  Comment out, to have scenario name as part of MDC context
        log.info("Scenario \"$scenarioName\" started")
    }

}
