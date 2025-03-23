package mfy.server.global.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.message.dto.MessageRequestDto.AttachtmentRequestDto;
import mfy.server.domain.message.entity.type.Attachment;
import mfy.server.global.exception.AwsS3Exception;
import mfy.server.global.exception.BusinessException;
import mfy.server.global.exception.ErrorConfig.ErrorMessage;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    private static final int MAX_IMAGE_UPLOAD_SIZE = 5 * 1024 * 1024;
    private static final List<String> LIMIT_IMAGE_TYPE_LIST = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    private static final int MAX_ATTACHMENT_UPLOAD_SIZE = 10 * 1024 * 1024;
    private static final List<String> LIMIT_ATTACHMENT_TYPE_LIST = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/vnd.ms-powerpoint",
            "video/mp4",
            "audio/mpeg",
            "application/zip");

    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.bucket.public}")
    private String publicBucket;

    @Value("${cloud.aws.bucket.private}")
    private String privateBucket;

    public List<Attachment> presignedAttachments(List<AttachtmentRequestDto> attachments) {
        attachments.forEach(attachment -> {
            validateAttachment(attachment.getType(), attachment.getSize());
        });

        List<Attachment> result = new ArrayList<Attachment>();
        for (int i = 0; i < attachments.size(); i++) {
            try {
                var attachment = attachments.get(i);
                String type = attachment.getType();
                Long size = attachment.getSize();
                String name = attachment.getName();
                String key = getKeyFromName(name);
                String uploadUrl = generatePutUrl(key);
                result.add(new Attachment((long) i, key, uploadUrl, name, type, size));
            } catch (Exception e) {
                log.info("Failed to generate upload url", e);
            }
        }
        return result;
    }

    public List<Attachment> uploadAttachments(List<MultipartFile> attachments) {
        attachments.forEach(attachment -> {
            validateAttachment(attachment.getContentType(), attachment.getSize());
        });

        List<Attachment> result = new ArrayList<Attachment>();
        for (int i = 0; i < attachments.size(); i++) {
            try {
                MultipartFile attachment = attachments.get(i);
                String name = attachment.getOriginalFilename();
                String type = attachment.getContentType();
                long size = attachment.getSize();
                String url = uploadAttachment(attachment);
                result.add(new Attachment((long) i, getKeyFromUrl(url), "", name, type, size));
            } catch (Exception e) {
                log.info("Failed to upload attachment", e);
            }
        }
        return result;
    }

    public String generateGetUrl(String key) {
        Date expiration = new DateTime().plusMinutes(5).toDate();
        return s3Client.generatePresignedUrl(privateBucket, key, expiration, HttpMethod.GET).toString();
    }

    private String generatePutUrl(String key) {
        Date expiration = new DateTime().plusMinutes(5).toDate();
        return s3Client.generatePresignedUrl(privateBucket, key, expiration, HttpMethod.PUT).toString();

    }

    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file.getContentType(), file.getSize());
        return uploadFile(file, publicBucket);
    }

    public String uploadAttachment(MultipartFile file) {
        validateAttachment(file.getContentType(), file.getSize());
        return uploadFile(file, privateBucket);
    }

    private String uploadFile(MultipartFile file, String bucket) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            String key = getKeyFromName(file.getOriginalFilename());
            PutObjectRequest request = new PutObjectRequest(bucket, key, file.getInputStream(), metadata);

            s3Client.putObject(request);

            return s3Client.getUrl(bucket, key).toString();
        } catch (Exception e) {
            throw new AwsS3Exception(ErrorMessage.S3_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String key, String bucket) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucket, key));
        } catch (Exception e) {
            throw new AwsS3Exception(ErrorMessage.S3_DELETE_FAILED);
        }
    }

    private void validateImageFile(String contentType, long size) {
        if (!LIMIT_IMAGE_TYPE_LIST.contains(contentType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.UNSUPPORTED_FILE_TYPE);
        }
        if (size > MAX_IMAGE_UPLOAD_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateAttachment(String contentType, long size) {
        if (!LIMIT_ATTACHMENT_TYPE_LIST.contains(contentType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.UNSUPPORTED_FILE_TYPE);
        }
        if (size > MAX_ATTACHMENT_UPLOAD_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ErrorMessage.FILE_SIZE_EXCEEDED);
        }
    }

    private String getKeyFromName(String originalFilename) {
        String extension = "";
        if (originalFilename != null) {
            int i = originalFilename.lastIndexOf(".");
            if (i >= 0) {
                extension = originalFilename.substring(i);
            }
        }
        return UUID.randomUUID() + extension;
    }

    public String getKeyFromUrl(String url) {
        try {
            return new URL(url).getPath().substring(1);
        } catch (Exception e) {
            return null;
        }
    }
}
