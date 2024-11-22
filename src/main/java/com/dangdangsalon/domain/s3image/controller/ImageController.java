package com.dangdangsalon.domain.s3image.controller;

import com.dangdangsalon.domain.s3image.service.ImageService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ApiSuccess<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadImage(file);
        return ApiUtil.success(imageUrl);
    }
}
