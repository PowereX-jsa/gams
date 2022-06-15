# Example app for running gams in aws lambda via java and micronaut

- example aws lambda app that takes gams files from input bucket, runs them and uploads the result
  file to output bucket

## how to build

- ```./gradlew shadowJar```
- ```docker build --platform linux/amd64 -t lambda-test .```

## how to run locally

- you need to set following environment variables (get them from your aws account)
  in `docker-compose.yml`:

```
      AWS_ACCESS_KEY_ID: ""
      AWS_SECRET_ACCESS_KEY: ""
      AWS_SESSION_TOKEN: ""
```

- ```docker-compose up```
- app is running on localhost:9000
- to test lambda invocation run it with test trigger:

```
 curl -X POST "http://localhost:9000/2015-03-31/functions/function/invocations" --data-binary "@src/test/resources/test-s3-put-event.json"
```

- it expects buckets `data-input-lambda-test` and `data-output-lambda-test` to be created
- files cloud be uploaded via web interface, aws cli, etc.
- if you want to change file that triggered lambda in test call change
  in `src/ est/resources/test-s3-put-event.json` following line:

```
        "object": {
          "key": "gams-stm-input-data_2022-06-09T12:37:08.047781Z/manifest.json",
```

## Micronaut 3.5.1 Documentation

- [User Guide](https://docs.micronaut.io/3.5.1/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.5.1/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.5.1/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)

## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)
