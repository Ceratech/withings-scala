# Scala wrapper for the Withing (Nokia Health) API

[![Build Status](https://travis-ci.org/Ceratech/withings-scala.svg?branch=master)](https://travis-ci.org/Ceratech/withings-scala)
[![Coverage Status](https://coveralls.io/repos/github/Ceratech/withings-scala/badge.svg?branch=master)](https://coveralls.io/github/Ceratech/withings-scala?branch=master)
[ ![Download](https://api.bintray.com/packages/ceratech/maven/withings-client/images/download.svg) ](https://bintray.com/ceratech/maven/withings-client/_latestVersion)


This wrapper allows you to use the [Health API](https://developer.health.nokia.com/api/doc); it supports OAuth authentication, setting up notifications and querying measurements.

It makes use of the [ScribeJava](https://github.com/scribejava/scribejava) OAuth client library and [Play JSON](https://www.playframework.com/documentation/2.6.x/ScalaJson) for converting API results to Scala case classes.

## Usage

### Client only

Construct a `WithingsClient` using it's companion object. Provide the API key and secret.

To run authorize:

1. Call `fetchAuthorizationUrl` which will return the temporary tokens and the user authorization URL
2. Redirect user to the authorization URL
3. On your callback URL the verified result will come back when it does call `requestAccessToken` with the verified result and the temporary tokens obtained in step 1
4. If all goes well you should've obtained a permanent API token and secret to use for other calls

To call a method:

Call either `registerNotification` or `getMeasurements` with the parameters you want and suply an implicit instance of `WithingsAccessTokens` to make an authorized call.

### REST server

Start the server (`io.ceratech.withings.rest`), and provide the following environment variables:

1. `API_KEY` the Withings API key
2. `API_SECRET` the Withings API secret
4. optional `PORT` the REST server port (defaults to `8080`)

After starting open the following URL [localhost:8080/api-docs/swagger.json](localhost:8080/api-docs/swagger.json) to view the API docs (load them in a Swagger Editor/UI).
