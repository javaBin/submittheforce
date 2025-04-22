# Submit the Force

The latest incarnation of the Submit, the interface provided to potential speakers to submit their talks to the JavaZone conference.


## Starting development environment

### Configuration of Moresleep

To run Moresleep locally, it may be configured using a `.env.moresleep` file. The following is an example of file to make it load talks from the official database:

```dotenv
SLEEPINGPILL_AUTH=username:password
LOAD_FROM_SLEEPINGPILL=true
```

To get it up and running:

```shell
docker compose up -d moresleep
```


### Configuration of Submit

To configure Submit, use a `.env` file. Content may be similar to the following:

```dotenv
# Configuration of the application itself (default values)
APP_URL=http://localhost:8080
APP_SECRET=JavaZoneForever

# Configuration of the moresleep service (default values)
TALKS_LOCATION=http://localhost:8082
TALKS_USERNAME=anon
TALKS_PASSWORD=anon

# Configuration of OIDC (default values)
QUARKUS_OIDC_AUTH_SERVER_URL=[configure me]
QUARKUS_OIDC_CLIENT_ID=[configure me]
QUARKUS_OIDC_CREDENTIALS_SECRET=[configure me]
```

For active development, run the following command to start the application:

```shell
make dev
```

To just get it up and running, run the following command:

```shell
make build
docker compose up -d submit
```