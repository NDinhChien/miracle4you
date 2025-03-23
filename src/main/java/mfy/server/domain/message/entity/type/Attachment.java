package mfy.server.domain.message.entity.type;

import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class Attachment {
    private Long id;

    private String key;

    private String uploadUrl;

    private String name;

    private String type;

    private Long size;

    private Boolean isSuccess;

    private LocalDateTime uploadedAt;

    public Attachment(Long id, String key, String uploadUrl, String name, String type, Long size) {
        this.id = id;
        this.key = key;
        this.uploadUrl = uploadUrl;
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public Attachment setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
        return this;
    }

    public Attachment setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
        return this;
    }

}
