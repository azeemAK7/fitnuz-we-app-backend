package com.fitnuz.project.Service.Definations;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    String creteFileName(MultipartFile file, String path) throws IOException;
}
