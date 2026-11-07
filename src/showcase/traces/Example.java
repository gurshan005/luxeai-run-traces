package showcase.traces;

import java.util.List;
import java.util.Map;
import static showcase.traces.RunGraphBuilder.*;

public final class Example {
    public static void main(String[] args) {
        var run = new Run("demo", "Index documents", "complete", 150, 0, null);
        int firstFlags = SignalFlags.fromSpan("tool", "error", "read", "disk",
                "Timeout", null, Map.of("retry", true));
        var first = new Span("read", null, "tool", "Read documents", "error", 100,
                firstFlags, "disk", null, "Timeout", null);
        var retry = new Span("retry", "read", "tool", "Read again", "ok", 50,
                0, "disk", null, null, null);
        var graph = graph(run, List.of(first, retry));
        if (graph.summary().nodeCount() != 3 || graph.summary().edgeCount() != 2
                || !graph.edges().get(0).source().equals("run:demo")
                || !graph.edges().get(1).source().equals("span:read")
                || graph.summary().signalFlags() !=
                (SignalFlags.TOOL_FAILED | SignalFlags.TIMEOUT | SignalFlags.RETRY)) {
            throw new AssertionError("Unexpected trace graph");
        }
        var empty = graph(run, List.of());
        if (empty.nodes().size() != 1 || !empty.edges().isEmpty()) {
            throw new AssertionError("An empty run should have one root");
        }
        for (var edge : graph.edges()) {
            System.out.printf("%s -> %s%n", edge.source(), edge.target());
        }
        graph.summary().flags().forEach((name, raised) -> {
            if (raised) System.out.println("signal: " + name);
        });
    }
}
