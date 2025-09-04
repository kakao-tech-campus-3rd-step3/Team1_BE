package knu.team1.be.boost.file.service;

import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequest;
import knu.team1.be.boost.file.dto.FileCompleteResponse;
import knu.team1.be.boost.file.dto.FileRequest;
import knu.team1.be.boost.file.dto.FileResponse;

public interface FileService {

    FileResponse uploadFile(FileRequest fileRequest);

    FileResponse downloadFile(UUID fileId);

    FileCompleteResponse completeUpload(UUID fileId, FileCompleteRequest fileCompleteRequest);
}
