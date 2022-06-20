package org.gams.integration.services;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class S3ClientService {

  public AmazonS3 getClient() {
    return AmazonS3ClientBuilder
        .standard()
        .withRegion(
            Regions.EU_CENTRAL_1) // TODO should be configurable (maybe via env var as in R apis?) - it`s different in dev/prod aws accounts
        .build();
  }
}
