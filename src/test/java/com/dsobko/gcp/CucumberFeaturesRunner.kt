import courgette.api.CourgetteOptions
import courgette.api.CourgetteRunLevel
import courgette.api.junit.Courgette
import cucumber.api.CucumberOptions
import org.junit.runner.RunWith


@RunWith(Courgette::class)
@CourgetteOptions(
        threads = 2,
        runLevel = CourgetteRunLevel.FEATURE,
        rerunFailedScenarios = false,
        rerunAttempts = 1,
        showTestOutput = true,
        reportTargetDir = "build",
        cucumberOptions = CucumberOptions(
                features = arrayOf("src/test/resources/features"),
                glue = arrayOf("com.dsobko.gcp"),
                tags = arrayOf("not @Ignore"),
                plugin = arrayOf(
                        "pretty",
                        "json:build/cucumber.json",
                        "html:build/cucumber-report"), strict = true))
object CucumberFeaturesRunner