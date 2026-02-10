package io.redlink.utils.test.testcontainers;

import java.util.ArrayList;
import java.util.List;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * copied from {@link org.testcontainers.containers.FailureDetectingExternalResource}
 */
abstract class TestContainerRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<>();

                try {
                    starting(description);
                    base.evaluate();
                    succeeded(description);
                } catch (Throwable e) {
                    errors.add(e);
                    failed(e, description);
                } finally {
                    finished(description);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    protected void starting(Description description) {}

    protected void succeeded(Description description) {}

    protected void failed(Throwable e, Description description) {}

    protected void finished(Description description) {}
}
