package org.gams.integration.utils;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class AwsS3Utils {

  private static final DockerImageName LOCALSTACK_DOCKER_IMAGE = DockerImageName.parse(
      "localstack/localstack:0.14.3");

  public static LocalStackContainer runS3Container() {
    // runs local s3
    final var s3Container = new LocalStackContainer(LOCALSTACK_DOCKER_IMAGE).withServices(S3);
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

    return s3Container;
  }

  public static void shutdownS3Container(LocalStackContainer s3Container) {
    if (s3Container.isRunning()) {
      log.debug("stopping s3 container");
      s3Container.stop();
    }
  }

  public static AmazonS3 getS3Client(LocalStackContainer s3Container) {
    log.debug("creating s3 client for local s3 container");

    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(
            s3Container.getEndpointConfiguration(S3)) // using testcontainers endpoint
        .withCredentials(s3Container.getDefaultCredentialsProvider())
        .build();
  }

  public static void createBucket(LocalStackContainer s3Container, String bucketName) {
    AmazonS3 client = getS3Client(s3Container);

    client.createBucket(bucketName);
  }
  public static void uploadToS3(LocalStackContainer s3Container, String bucketName, String key,
      String content) {
    AmazonS3 client = getS3Client(s3Container);

    client.createBucket(bucketName);

    client.putObject(bucketName, key, content);
  }

  @SneakyThrows(IOException.class)
  public static String downloadJsonFromS3(LocalStackContainer s3Container, String bucketName,
      String key) {
    AmazonS3 client = getS3Client(s3Container);

    S3Object s3Object = client.getObject(bucketName, key);

    return new String(s3Object.getObjectContent().readAllBytes());
  }
}
