package org.gams.integration.utils;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import io.micronaut.context.annotation.Replaces;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.services.s3.ClientService;
import org.gams.integration.services.s3.AwsS3ClientService;
import org.gams.integration.services.s3.S3Service;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Replaces(AwsS3ClientService.class)
@Singleton
@Slf4j
public class LocalS3ClientService implements ClientService {

  private final DockerImageName LOCALSTACK_DOCKER_IMAGE = DockerImageName.parse(
      "localstack/localstack:0.14.3");
  @Getter
  private final LocalStackContainer s3Container;

  public LocalS3ClientService() {
    log.debug("MockedS3ClientService.<init>");
    s3Container = new LocalStackContainer(LOCALSTACK_DOCKER_IMAGE).withServices(S3);

    // needs to be started for getting endpoint configuration
    s3Container.start();

    // sets up necessary props for local s3
    System.setProperty("aws.accessKeyId",
        s3Container.getDefaultCredentialsProvider().getCredentials().getAWSAccessKeyId());
    System.setProperty("aws.secretKey",
        s3Container.getDefaultCredentialsProvider().getCredentials().getAWSSecretKey());
    System.setProperty("s3.endpoint",
        s3Container.getEndpointConfiguration(S3).getServiceEndpoint());
    System.setProperty("s3.region", s3Container.getEndpointConfiguration(S3).getSigningRegion());

    log.debug("created local s3 container: '{}'", s3Container);
  }

  @Override
  public AmazonS3 getClient() {
    log.debug("creating s3 client for local s3 container");

    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(
            s3Container.getEndpointConfiguration(S3)) // using testcontainers endpoint
        .withCredentials(s3Container.getDefaultCredentialsProvider())
        .build();
  }

  public void createBucket(String bucketName) {
    AmazonS3 client = getClient();

    client.createBucket(bucketName);
  }

  public void uploadToS3(String bucketName, String key, String content) {
    AmazonS3 client = getClient();

    if (!client.doesBucketExistV2(bucketName)) {
      client.createBucket(bucketName);
    }

    client.putObject(bucketName, key, content);
  }

  public void uploadToS3(String bucketName, String key, File file) {
    AmazonS3 client = getClient();

    if (!client.doesBucketExistV2(bucketName)) {
      client.createBucket(bucketName);
    }

    client.putObject(bucketName, key, file);
  }

  @SneakyThrows(IOException.class)
  public String downloadJsonFromS3(String bucketName, String key) {
    AmazonS3 client = getClient();

    S3Object s3Object = client.getObject(bucketName, key);

    return new String(s3Object.getObjectContent().readAllBytes());
  }

  @PostConstruct
  void setUpBuckets() {
    AmazonS3 client = getClient();

    client.createBucket(S3Service.INPUT_DATA_BUCKET_NAME);
    client.createBucket(S3Service.OUTPUT_DATA_BUCKET_NAME);
  }

  @PreDestroy
  void shutdownS3() {
    if (s3Container.isRunning()) {

      log.debug("container still running, shutting down");

      s3Container.stop();
    }
  }
}
