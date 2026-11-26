package org.sequeless.build.bom;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.sequeless.automation.api.AutomationPort;
import org.sequeless.automation.api.ResolvedWebhookRequest;
import org.sequeless.automation.inprocess.InProcessAutomationPort;
import org.sequeless.automation.inprocess.InProcessAutomationProperties;
import org.sequeless.automation.inprocess.InProcessTimerScheduler;
import org.sequeless.automation.testkit.AutomationContract;
import org.sequeless.automation.testkit.RecordingActionExecutor;
import org.sequeless.automation.testkit.RecordingTriggerEvaluator;
import org.sequeless.ontology.automation.api.Action;
import org.sequeless.ontology.automation.api.Automation;
import org.sequeless.ontology.automation.api.TransitionRef;
import org.sequeless.ontology.schema.api.OntologyVersion;

/**
 * Runs the automation testkit contract against the in-process adapter at the versions {@code sequeless-bom}
 * lists. A failure here means a released automation artifact no longer satisfies its own contract together
 * with the rest of the BOM.
 *
 * <p>The adapter performs a real HTTP delivery inside {@code ingest}, so a local server answers the webhook,
 * as in {@code InProcessAutomationPortContractTest}. The clock moves only when the contract advances it.
 */
class AutomationBomContractTest extends AutomationContract {

    private static HttpServer server;
    private static String webhookUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/hook", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        webhookUrl = "http://localhost:" + server.getAddress().getPort() + "/hook";
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    private final RecordingActionExecutor recorder =
            new RecordingActionExecutor(new ResolvedWebhookRequest(webhookUrl, "POST", Optional.empty()));
    private final RecordingTriggerEvaluator triggerEvaluator = new RecordingTriggerEvaluator();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
    private final InProcessTimerScheduler timerScheduler = new InProcessTimerScheduler(clock, triggerEvaluator);
    private Automation automation = new Automation(new OntologyVersion(1), Map.of());
    private final AutomationPort port = new InProcessAutomationPort(
            recorder, new InProcessAutomationProperties(), triggerEvaluator, timerScheduler, tenant -> automation);

    @Override
    protected AutomationPort port() {
        return port;
    }

    @Override
    protected void serveAutomationWithActions(Map<TransitionRef, List<Action>> actionsByTransition) {
        automation = new Automation(new OntologyVersion(1), actionsByTransition);
    }

    @Override
    protected RecordingActionExecutor recordingExecutor() {
        return recorder;
    }

    @Override
    protected RecordingTriggerEvaluator recordingTriggerEvaluator() {
        return triggerEvaluator;
    }

    @Override
    protected void advanceTime(Duration by) {
        clock.advance(by);
        timerScheduler.sweep();
    }

    /** A clock that moves only when {@link #advance} is called, so timers fire without sleeping. */
    private static final class MutableClock extends Clock {

        private final ZoneId zone;
        private Instant now;

        MutableClock(Instant now, ZoneId zone) {
            this.now = now;
            this.zone = zone;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(now, zone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
