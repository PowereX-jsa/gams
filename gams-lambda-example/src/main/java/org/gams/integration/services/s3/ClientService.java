package org.gams.integration.services.s3;

import com.amazonaws.services.s3.AmazonS3;

public interface ClientService {

  AmazonS3 getClient();
}
