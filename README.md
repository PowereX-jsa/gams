# gams

## Images

### `powerex/gams-init`
Base image that installs GAMS from a self-extracting binary. Used as a build stage source for other images.

> **Note:** This image must be built manually to avoid redistributing the GAMS installer and violating its license terms. It requires the GAMS self-extracting installer binary to be provided as a build argument. The installer can be downloaded from [gams.com/download][gams-download]

#### Single-platform build

```bash
docker build \
  --build-arg GAMS_INSTALL_FILE=linux_x64_64_sfx.exe \
  -t powerex/gams-init:51.4.0 \
  images/gams-init/
```

#### Multi-platform build (amd64 + arm64)

Since each platform requires a different GAMS installer binary, the two variants must be built and pushed separately, then combined into a multi-arch manifest:

```bash
# Build and push amd64 variant
docker buildx build --platform linux/amd64 \
  --build-arg GAMS_INSTALL_FILE=./linux_x64_64_sfx.exe \
  -t powerex/gams-init:51.4.0-amd64 \
  --push images/gams-init/

# Build and push arm64 variant
docker buildx build --platform linux/arm64 \
  --build-arg GAMS_INSTALL_FILE=./linux_arm64_sfx.exe \
  -t powerex/gams-init:51.4.0-arm64 \
  --push images/gams-init/

# Combine into a single multi-arch manifest
docker buildx imagetools create \
  -t powerex/gams-init:51.4.0 \
  powerex/gams-init:51.4.0-amd64 \
  powerex/gams-init:51.4.0-arm64
```

Downstream images using `FROM powerex/gams-init:51.4.0` will automatically pull the correct variant for their target platform.

#### Verify multi-arch manifest

```bash
docker buildx imagetools inspect powerex/gams-init:51.4.0
```

The output should show `application/vnd.docker.distribution.manifest.list.v2+json` with both `linux/amd64` and `linux/arm64` platforms listed.

### `powerex/gams`
General-purpose GAMS image based on Amazon Corretto 25. Includes GAMS and Java.

## License

GAMS requires a `gamslice.txt` license file to run. **Do not bake the license into the image.**

### Running with a license file

Mount the license file into the GAMS system directory:

```bash
docker run \
  -v /path/to/gamslice.txt:/opt/gams/gamslice.txt \
  powerex/gams
```

### Running with an environment variable

Pass the license content via the `GAMS_LICENSE` environment variable:

```bash
docker run \
  -e GAMS_LICENSE="$(cat /path/to/gamslice.txt)" \
  powerex/gams \
  bash -c 'echo "$GAMS_LICENSE" > /opt/gams/gamslice.txt && gams model.gms'
```

## Testing

Run a simple LP optimization test using the included `test-file.gms`:

```bash
docker run --rm \
  -v ./gamslice.txt:/opt/gams/gamslice.txt \
  -v ./test-file.gms:/app/test-file.gms \
  powerex/gams \
  test-file.gms
```

A successful run will output something like:

```
...
Reduced LP has 2 rows, 3 columns, and 6 nonzeros.
Presolve time = 0.00 sec. (0.00 ticks)

Iteration      Dual Objective            In Variable           Out Variable
     1  I            0.000000                 Xwheat            labor slack
     2            9950.000000                  Xcorn             land slack

--- LP status (1): optimal.
--- Cplex Time: 0.00sec (det. 0.01 ticks)


Optimal solution found
Objective:         9950.000000

--- Reading solution for model farmproblem
*** Status: Normal completion
--- Job test-file.gms Stop 04/10/26 11:59:18 elapsed 0:00:00.049 
```

## CI

The [`gams.yml`](.github/workflows/gams.yml) workflow builds and tests the `powerex/gams` image — it does **not** publish any image. It is triggered on pull requests, pushes to `master` (when `images/gams/**` changes), or manually via `workflow_dispatch`.

### Workflow

1. Build `powerex/gams-init` locally and push it to Docker Hub (see [above](#powerexgams-init)).
2. Update the tag in [`images/gams/Dockerfile`](images/gams/Dockerfile) (e.g. `COPY --from=powerex/gams-init:51.4.0`) if a new version was built.
3. The CI workflow will automatically build and test the `powerex/gams` image on the next push or pull request. You can also trigger it manually from **Actions → gams docker build → Run workflow**.
4. To test locally before pushing, run the test command from the [Testing](#testing) section.

---
[gams-download]: https://www.gams.com/download/
