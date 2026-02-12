# Build & Deploy Guide
**Train Resolver Service**

This document describes how to build the Train Resolver service with the corrected
`java-common-lib`, create a Docker image, and push it to the container registry.

---

## Prerequisites

- Java / Maven installed
- Docker with `buildx` enabled
- Access to the container registry:
  `mtr.devops.telekom.de`
- Logged in to the registry:
  ```bash
  docker login mtr.devops.telekom.de
  ```

---

## 1. Build the Service JAR

From the **project root**, build the `service` module and all required dependencies.
Tests are skipped to speed up the build.

```bash
mvn -pl service -am clean package -DskipTests
```

This command:
- Builds the corrected `java-common-lib`
- Packages the `service` module
- Produces a JAR in `service/target/`

---

## 2. Dockerfile

The following `Dockerfile` is located in the `service/` directory.

```dockerfile
FROM eclipse-temurin:21-jre

# Install native crypto dependency
RUN apt-get update  && apt-get install -y libsodium23  && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built JAR
COPY target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 3. Build the Docker Image

Change into the `service` directory:

```bash
cd service
```

Build the Docker image for the `linux/amd64` platform:

```bash
docker buildx build   --platform linux/amd64   -t mtr.devops.telekom.de/platform8ra/train-resolver:${VERSION}   .
```

---

## 4. Push the Image to the Registry

Push the newly built image:

```bash
docker push mtr.devops.telekom.de/platform8ra/train-resolver:${VERSION}
```

The image is now available in the registry and ready for deployment.

---

## 5. Kubernetes Deployment Update

Update the Kubernetes Deployment to use the new image tag:

```bash
mtr.devops.telekom.de/platform8ra/train-resolver:${VERSION}
```

Changing the image tag automatically triggers a **rolling update**.  
No manual restart is required.

---

## Summary

| Step | Command |
|------|--------|
| Build JAR | `mvn -pl service -am clean package -DskipTests` |
| Build image | `docker buildx build --platform linux/amd64 ...` |
| Push image | `docker push ...` |
| Deploy | `kubectl set image deployment/...` |

---

## Notes

- Always use a **new image tag** for each change.
- Avoid reusing tags like `latest`.
- Check the new pod started correctly with `kubectl get pods` and `kubectl logs`.

# Resolve Endpoint Configuration Notes

The `/resolve` method is properly working without any changes, but there
are some important configuration details described below.

------------------------------------------------------------------------

## 1. VC Trust Scheme Requirements

-   The VCs must be **self-signed (Trust-scheme VC)**.

-   Example:

    `did:web:vc-holder.ssi-a.platform.mg3.mdb.osc.live`

-   This DID has its own key which is used to:

  -   Sign its **did-configuration file**
  -   Sign the **VC referenced in the DID document as a
      serviceEndpoint**

-   The referenced VC contains:

  -   A link to the **trust-list**
  -   The **hash of the trust-list**

### How `/resolve` Works

The `/resolve` endpoint is used to verify whether the issuer can be
trusted using `trustSchemePointers`.

For a successful resolution:

-   The **trust-list must contain the issuer** provided in the request.
-   The **VC must be signed with a DID** that exists in the
    `trustSchemePointer`.

> Note:\
> This might seem unusual for the cluster permissions concept we are
> planning to implement, but this behavior follows the current code
> flow.

------------------------------------------------------------------------

## 2. Train Content Resolver Configuration

Current `application.yaml` configuration. Can be changed and adjusted to the needs, but pay attention to the DNS configuration:

``` yaml
tcr:
  dns:
    doh:
      enabled: false
    dnssec:
      enabled: false
      rootPath:
```

------------------------------------------------------------------------

## 3. DNS Resolution for federation1.train

To resolve `federation1.train`, the request must be:

``` bash
dig @160.44.4.25 federation1.train ANY
```

### DNS Host Configuration

The DNS host must be configured either:

-   On the cluster level, or
-   Locally in `application.yaml`:

``` yaml
tcr:
  dns:
    hosts: 160.44.4.25
    timeout: 500
```

------------------------------------------------------------------------

## Summary

-   `/resolve` is functioning correctly.
-   Proper Trust Scheme VC configuration is required.
-   DNS configuration must match the specified resolver host.
-   The trust-list must include the issuer and the VC must be signed by
    a DID listed in `trustSchemePointer`.



