package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Service.Definations.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service("localFileService")
public class FileServiceImpl implements FileService {
    @Override
    public String creteFileName(MultipartFile file, String path) throws IOException {
        if (file.getSize() > 5 * 1024 * 1024) { // 5 MB
            throw new IllegalArgumentException("File too large");
        }
        String originalFileName = file.getOriginalFilename();
        if(isAllowedExtension(originalFileName)){
            String randomId = UUID.randomUUID().toString();
            String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
            String filePath = path + File.separator + fileName;

            File folder = new File(path);
            if(!folder.exists()){
                folder.mkdir();
            }
            Files.copy(file.getInputStream(), Paths.get(filePath));
            return fileName;
        }else{
            throw new IllegalArgumentException("Upload File With Extension jpg,jpeg,png ");
        }
    }

    private boolean isAllowedExtension(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();

        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

        return allowedExtensions.contains(extension);
    }
}
