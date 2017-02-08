# RestSec

`This project is still in the early phase of development.`

## Overview
- [Description](#Description)
- [Supported OWASP Top 10 (2013) Vulnerabilities](#Supported_Vulnerabilities)
- [Sources](#Sources)
- [Usage](#Usage)


## Description <a name="Description"></a>

This tool is developed with the aim of testing the REST interface of your web service for some of the __OWASP Top 10__ security vulnerabilities automatically. It uses
- [junit 4.12](https://github.com/junit-team/junit4) __(JUnit 5 is currently not supported)__
- [rest-assured](https://github.com/rest-assured/rest-assured)
- built with: Maven (adding gradle later)

## Supported OWASP Top 10 (2013) Security Vulnerabilities <a name="Supported_Vulnerabilities"></a>

Some of the following common security vulnerabilities categories __can't be properly tested__ via the REST interface, thus they are not (yet) implemented (at least I haven't found out a way to do so yet).

| # | Name | Supported | Method |
|:---:|:---|:---:|:---|
|A1|Injection|maybe|Common SQLi Payloads, but there are other tools for SQLi|
|A2|Broken Authentication and Session Management|planned|Basic Authentication, juiceshop.OAuth2 Session Management (Tokens), Are Session Cookies Returned, that shouldn't (jesessionid)?|
|A3|Cross-Site Scripting (XSS)|planned|Injecting scripts via JSON elements|
|A4|Insecure Direct Object References|no|-|
|A5|Security Misconfiguration|no|-|
|A6|Sensitive Data Exposure|no|-|
|A7|Missing Function Level Access Control|planned|Which user can call which URLs (via parsing of Excel Access Matrix)
|A8|Cross-Site Request Forgery (CSRF)|planned|Are known CSRF Tokens in the header/payload?|
|A9|Using Components with Known Vulnerabilities|planned|Checking used libraries with OWASP Dependency Checker|
|A10|Unvalidated Redirects and Forwards|planned|Can an unauthorized user with the right token also POST and DELETE?|

## Sources <a name="Sources"></a>
- [REST_Assessment_Cheat_Sheet](https://www.owasp.org/index.php/REST_Assessment_Cheat_Sheet)
- [REST_Security_Cheat_Sheet](https://www.owasp.org/index.php/REST_Security_Cheat_Sheet)

## Usage <a name="Usage"></a>

### restsec-samples (with juice-shop) <a name="restsec-samples"></a>
- run the [juice-shop](https://github.com/bkimminich/juice-shop) by [@bkimminich](https://github.com/bkimminich)
- enter the URI and port of the instance in __initTarget()__
- run all tests

### restsec-core (*no content yet*) <a name="restsec-core"></a>
- the tests from __restsec-samples__ will be moved here when generalized
