package knu.team1.be.boost.file.dto;

import java.util.Map;
import java.util.UUID;

public record FileResponse(
    UUID fileId,
    String key,
    String url,
    String method,
    Map<String, String> headers,
    Integer expiresInSeconds
) {

}
