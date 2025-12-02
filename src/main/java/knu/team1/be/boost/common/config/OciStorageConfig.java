package knu.team1.be.boost.common.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class OciStorageConfig {

    @Value("${boost.oci.region}")
    private String region;

    @Value("${boost.oci.namespace}")
    private String namespace;

    @Value("${boost.oci.credentials.access-key}")
    private String accessKey;

    @Value("${boost.oci.credentials.secret-key}")
    private String secretKey;

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner() {
        String endpoint = String.format(
            "https://%s.compat.objectstorage.%s.oraclecloud.com",
            namespace,
            region
        );

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
}

