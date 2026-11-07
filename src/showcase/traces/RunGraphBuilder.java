package showcase.traces;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RunGraphBuilder {
    private RunGraphBuilder() {}

    public record Run(String id, String name, String status, int latencyMs,
                      int signalFlags, String externalId) {}
    public record Span(String spanId, String parentSpanId, String kind, String name,
                       String status, int latencyMs, int signalFlags, String toolName,
                       String model, String errorType, String metadataJson) {}

    public static RunGraph graph(Run run, List<Span> spans) {
		List<GraphNode> nodes = new ArrayList<>();
		List<GraphEdge> edges = new ArrayList<>();
		int combinedFlags = run.signalFlags();

		nodes.add(new GraphNode(
			"run:" + run.id(),
			run.id(),
			"run",
			run.name(),
			run.status(),
			run.latencyMs(),
			run.signalFlags(),
			flags(run.signalFlags()),
			Map.of("externalId", nullable(run.externalId()))
		));

		for (Span span : spans) {
			combinedFlags |= span.signalFlags();
			nodes.add(new GraphNode(
				"span:" + span.spanId(),
				span.spanId(),
				span.kind(),
				span.name(),
				span.status(),
				span.latencyMs(),
				span.signalFlags(),
				flags(span.signalFlags()),
				Map.of(
					"toolName", nullable(span.toolName()),
					"model", nullable(span.model()),
					"errorType", nullable(span.errorType()),
					"metadataJson", nullable(span.metadataJson())
				)
			));
			String source = span.parentSpanId() == null || span.parentSpanId().isBlank() ? "run:" + run.id() : "span:" + span.parentSpanId();
			edges.add(new GraphEdge(source, "span:" + span.spanId(), span.kind()));
		}

		return new RunGraph(
			run,
			nodes,
			edges,
			new GraphSummary(nodes.size(), edges.size(), combinedFlags, flags(combinedFlags))
		);
	}

	private static Map<String, Boolean> flags(int signalFlags) {
		Map<String, Boolean> flags = new LinkedHashMap<>();
		flags.put("approvalWaiting", SignalFlags.has(signalFlags, SignalFlags.APPROVAL_WAITING));
		flags.put("toolFailed", SignalFlags.has(signalFlags, SignalFlags.TOOL_FAILED));
		flags.put("secretUsed", SignalFlags.has(signalFlags, SignalFlags.SECRET_USED));
		flags.put("evalFailed", SignalFlags.has(signalFlags, SignalFlags.EVAL_FAILED));
		flags.put("timeout", SignalFlags.has(signalFlags, SignalFlags.TIMEOUT));
		flags.put("handoff", SignalFlags.has(signalFlags, SignalFlags.HANDOFF));
		flags.put("policyBlocked", SignalFlags.has(signalFlags, SignalFlags.POLICY_BLOCKED));
		flags.put("retry", SignalFlags.has(signalFlags, SignalFlags.RETRY));
		flags.put("contextMiss", SignalFlags.has(signalFlags, SignalFlags.CONTEXT_MISS));
		return flags;
	}

	private static String nullable(String value) {
		return value == null ? "" : value;
	}

	public record RunGraph(
		Run run,
		List<GraphNode> nodes,
		List<GraphEdge> edges,
		GraphSummary summary
	) {}

	public record GraphNode(
		String id,
		String traceId,
		String type,
		String label,
		String status,
		int latencyMs,
		int signalFlags,
		Map<String, Boolean> flags,
		Map<String, String> detail
	) {}

	public record GraphEdge(String source, String target, String label) {}

	public record GraphSummary(int nodeCount, int edgeCount, int signalFlags, Map<String, Boolean> flags) {}
}
