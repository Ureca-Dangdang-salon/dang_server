package com.dangdangsalon.domain.s3image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Service s3Service;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public String uploadImage(MultipartFile file) {
        String fileName = s3Service.uploadFile(file);
        return String.format("https://%s/%s", cloudFrontDomain, fileName);
    }
}
