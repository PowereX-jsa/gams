package org.gams.integration.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.services.s3.S3Service;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class ManifestReader {

  private final S3Service s3Service;
  private final ObjectMapper objectMapper;

  @SneakyThrows(IOException.class)
  public List<String> getFilenamesFromManifest(String s3ManifestLocation) {
    byte[] manifestAsBytes = s3Service.downloadFile(s3ManifestLocation);

    TypeReference<List<String>> typeReference = new TypeReference<>() {
    };

    List<String> filesList = objectMapper.readValue(manifestAsBytes, typeReference);
    log.debug("relevant files: '{}'", filesList);

    return filesList;
  }
}
