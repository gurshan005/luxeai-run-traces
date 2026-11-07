package showcase.traces;

import java.util.Locale;
import java.util.Map;

public final class SignalFlags {

	public static final int APPROVAL_WAITING = 1;
	public static final int TOOL_FAILED = 1 << 1;
	public static final int SECRET_USED = 1 << 2;
	public static final int EVAL_FAILED = 1 << 3;
	public static final int TIMEOUT = 1 << 4;
	public static final int HANDOFF = 1 << 5;
	public static final int POLICY_BLOCKED = 1 << 6;
	public static final int RETRY = 1 << 7;
	public static final int CONTEXT_MISS = 1 << 8;
	public static final int KNOWN_MASK = APPROVAL_WAITING | TOOL_FAILED | SECRET_USED | EVAL_FAILED | TIMEOUT | HANDOFF | POLICY_BLOCKED | RETRY | CONTEXT_MISS;

	private SignalFlags() {
	}

	public static int fromSpan(String kind, String status, String name, String toolName, String errorType, String errorMessage, Map<String, Object> metadata) {
		int flags = 0;
		String normalizedKind = normalize(kind);
		String normalizedStatus = normalize(status);
		String normalizedName = normalize(name);
		String normalizedErrorType = normalize(errorType);
		String normalizedErrorMessage = normalize(errorMessage);
		String normalizedToolName = normalize(toolName);
		String metadataText = normalize(String.valueOf(metadata == null ? Map.of() : metadata));

		if ("tool".equals(normalizedKind) && "error".equals(normalizedStatus)) flags |= TOOL_FAILED;
		if (metadataText.contains("connectionid") || metadataText.contains("secretref") || metadataText.contains("secret_path")) flags |= SECRET_USED;
		if ("eval".equals(normalizedKind) && ("error".equals(normalizedStatus) || metadataText.contains("verdict=fail") || metadataText.contains("\"verdict\":\"fail\""))) flags |= EVAL_FAILED;
		if (metadataText.contains("approvalwaiting=true") || metadataText.contains("approval_waiting=true") || metadataText.contains("\"approvalwaiting\":true")) flags |= APPROVAL_WAITING;
		if ("handoff".equals(normalizedKind) || normalizedName.contains("handoff")) flags |= HANDOFF;
		if (normalizedErrorType.contains("timeout") || normalizedErrorMessage.contains("timeout")) flags |= TIMEOUT;
		if (metadataText.contains("policyblocked=true") || metadataText.contains("policy_blocked=true") || normalizedErrorType.contains("policy")) flags |= POLICY_BLOCKED;
		if (metadataText.contains("retry=true") || metadataText.contains("attempt")) flags |= RETRY;
		if ("context".equals(normalizedKind) && ("error".equals(normalizedStatus) || normalizedName.contains("miss") || normalizedToolName.contains("miss"))) flags |= CONTEXT_MISS;
		return flags;
	}

	public static boolean has(int flags, int flag) {
		return (flags & flag) == flag;
	}

	public static boolean onlyKnown(int flags) {
		return flags >= 0 && (flags & ~KNOWN_MASK) == 0;
	}

	private static String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT).replace(" ", "");
	}
}
