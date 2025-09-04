package knu.team1.be.boost.file.service;

import knu.team1.be.boost.file.dto.FileRequest;
import knu.team1.be.boost.file.dto.FileResponse;

public interface FileService {

    FileResponse uploadFile(FileRequest fileRequest);
}
