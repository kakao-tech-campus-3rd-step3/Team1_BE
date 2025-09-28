package knu.team1.be.boost.comment.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import knu.team1.be.boost.file.entity.File;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileInfo {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Column(name = "file_page")
    private Integer filePage;

    @Column(name = "file_x")
    private Float fileX;

    @Column(name = "file_y")
    private Float fileY;

    public FileInfo(File file, Integer filePage, Float fileX, Float fileY) {
        this.file = file;
        this.filePage = filePage;
        this.fileX = fileX;
        this.fileY = fileY;
    }
}
