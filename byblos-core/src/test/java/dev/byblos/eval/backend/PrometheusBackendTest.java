package dev.byblos.eval.backend;

import com.typesafe.config.ConfigFactory;
import dev.byblos.model.DataExpr;
import dev.byblos.model.EvalContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

public class PrometheusBackendTest {

    @Test
    void query() throws Exception {
        var config = ConfigFactory.parseMap(Map.of("endpoint", "https://demo.promlabs.com", "auth", "none"));
        var backend = new PrometheusBackend(config);
        var end = Instant.now();
        var context = new EvalContext(end.minusSeconds(60 * 5).toEpochMilli(), end.toEpochMilli(), 60 * 1000);
        var expr = new DataExpr("node_disk_read_time_seconds_total");
        var res = backend.query(context, expr);
        System.out.println(res);
    }
}
