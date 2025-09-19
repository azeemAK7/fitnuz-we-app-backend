package com.fitnuz.project.Service.Implementations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fitnuz.project.Service.Definations.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service("cloudinaryFileService")
public class FileServiceCloudinaryImpl implements FileService {

    private final Cloudinary cloudinary;

    @Autowired
    public FileServiceCloudinaryImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    @Override
    public String creteFileName(MultipartFile file, String path) throws IOException {
        if (file.getSize() > 1024 * 1024) {
            throw new IllegalArgumentException("File too large");
        }

        String originalFileName = file.getOriginalFilename();
        if (!isAllowedExtension(originalFileName)) {
            throw new IllegalArgumentException("Upload File With Extension jpg, jpeg, png");
        }

        String randomId = UUID.randomUUID().toString();
        String publicId = "products/" + randomId; // store inside 'products' folder

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image"
        ));

        return (String) uploadResult.get("secure_url"); // return Cloudinary URL
    }

    private boolean isAllowedExtension(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");
        return allowedExtensions.contains(extension);
    }

}
