package knu.team1.be.boost.file.service;

import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;

public interface FileService {

    FileResponseDto uploadFile(FileRequestDto request);

    FileResponseDto downloadFile(UUID fileId);

    FileCompleteResponseDto completeUpload(UUID fileId, FileCompleteRequestDto request);
}
