Income Tax Subscription MicroService
====================================
[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)[
![Build Status](https://travis-ci.org/hmrc/income-tax-subscription.svg?branch=master)](https://travis-ci.org/hmrc/income-tax-subscription) [ ![Download](https://api.bintray.com/packages/hmrc/releases/income-tax-subscription/images/download.svg) ](https://bintray.com/hmrc/releases/income-tax-subscription/_latestVersion)

This MicroService provides Third Party Applications with RESTful APIs to allow a User to Subscribe for the Making Tax Digital - Income Tax (MTD-IT) Service.

## API Sandbox Endpoints
It currently supports the following end-points:

#### POST /sandbox/subscription
This resource requires user token as well as the 'Accept' header.
```
Authorization: Bearer {OAuth2.0 bearer token}
Accept: application/vnd.hmrc.1.0+json
```

request:
```
POST /sandbox/subscription
{
  "email": "test@test.com",
  "acceptsTermsAndConditions": true
}
```

response:
```
CREATED (201)
```

## API Definition
API definition for the service will be available under `/api/definition` endpoint.
See definition in `/conf/api-definition.json` for the format.

## API Version
Version of API is 1.0 and needs to be provided in `Accept` request header
```
Accept: application/vnd.hmrc.1.0+json
```

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
 
