<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="src/main/resources/reporting/favicon.ico" type="image/x-icon">

    <title>Theme Template for Bootstrap</title>

    <!-- Bootstrap core CSS -->
    <link href="src/main/resources/reporting/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap theme -->
    <link href="src/main/resources/reporting/bootstrap-theme.min.css" rel="stylesheet">
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <!--
    <link href="../../assets/css/ie10-viewport-bug-workaround.css" rel="stylesheet">
    -->

    <!-- Custom styles for this template -->
    <link href="src/main/resources/reporting/report.css" rel="stylesheet">

</head>

<body>

<div class="container theme-showcase" role="main">

    <div class="jumbotron">
        <h1>RestSec Scan Results</h1>
        <div class="alert alert-info" role="alert">
            <strong>IP:</strong> ${TargetIP}<br>
            <strong>Port:</strong> ${TargetPort}<br>
            <strong>Path:</strong> ${TargetPath}
        </div>
        <div class="alert alert-info" role="alert">
            <strong>Crawler:</strong> ${CrawlerType}<br>
            <strong>Scanner:</strong> ${ScannerType}
        </div>
        <div class="alert alert-info" role="alert">
            <strong>Scan started:</strong> unknown <br>
            <strong>Scan finished:</strong> unknown <br>
            <strong>Report generated:</strong> ${ReportGeneratedTime}
        </div>
        <div class="alert alert-info" role="alert">
            <strong>Packets fired:</strong> ${packageStatistics.numberOfSentPackets}<br>
            <strong>Packets accepted:</strong> ${packageStatistics.acceptedPackets}     (${packageStatistics.acceptRatio}%)
        </div>
    </div>

    <#if counter_vulns == 0>
        <div class="alert alert-success" role="alert">
            <strong>Success!</strong> No vulnerabilites found.
        </div>
    <#else>
        <div class="alert alert-danger" role="alert">
            <strong>Danger!</strong> ${counter_vulns} possible security vulnerabilities found!
        </div>

        <div class="page-header">
            <h1>Details</h1>
        </div>
        <div class="row">
            <div class="col-md-6">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th class="col-xs-1">#</th>
                        <th class="col-xs-2">Vulnerability Type</th>
                        <th class="col-xs-2">Endpoint</th>
                        <th class="col-xs-2">Payload</th>
                        <th class="col-xs-5">Comment</th>
                        <!-- col-xs-# should equal 12 (1+1+1+1+8) -->
                    </tr>
                    </thead>
                    <tbody>
                    <!-- LOOP THROUGH THE VULNERABILITES FOUND -->

                    <#list vulnerabilites?keys?sort as key>
                        <tr>
                            <td>${key}</td>
                            <#switch vulnerabilites[key].VulnType>
                                <#case ("XSS")>
                                    <td><span class="label label-danger">${vulnerabilites[key].VulnType}</span></td>
                                <#break>
                                <#case ("XSS (alert)")>
                                    <td><span class="label label-danger">${vulnerabilites[key].VulnType}</span></td>
                                <#break>
                                <#case ("SQLi")>
                                    <td><span class="label label-primary">${vulnerabilites[key].VulnType}</span></td>
                                <#break>
                                <#default>
                                    <td><span class="label label-default">${vulnerabilites[key].VulnType}</span></td>
                            </#switch>
                            <td>${vulnerabilites[key].Endpoint}</td>
                            <td>${vulnerabilites[key].Payload}</td>
                            <td>${vulnerabilites[key].Comment}</td>
                        </tr>
                    </#list>

                    </tbody>
                </table>
            </div>
        </div>

        </#if>





    <#if counter_headers == 0>
        <div class="alert alert-info" role="alert">
            <strong>Info!</strong> There were no tests run for security headers!
        </div>
    <#elseif counter_insecure_headers == 0>
        <div class="alert alert-success" role="alert">
            <strong>Success!</strong> All tested HTTP (security) Headers are set!
        </div>
    <#else >
        <#if counter_insecure_headers == counter_headers>
            <div class="alert alert-danger" role="alert">
                <strong>Warning!</strong> <i>None</i> of the tested HTTP (security) Headers are set!
            </div>
        <#else>
            <div class="alert alert-warning" role="alert">
                <strong>Attention!</strong> ${counter_insecure_headers} <i>possible</i> security issues regarding HTTP Headers found!
            </div>
        </#if>

        <div class="page-header">
            <h1>HTTP Security Headers</h1>
        </div>

        <!-- TRYING TO MAKE THE TABLE RESPONSIVE -->

        <div class="row">
            <div class="col-md-6">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Vulnerability Type</th>
                        <th>Endpoint</th>
                        <th>Header</th>
                        <th>Comment</th>
                    </tr>
                    </thead>
                    <tbody>
                    <!-- LOOP THROUGH THE VULNERABILITES FOUND -->

                    <#list securityHeaders?keys?sort as key>
                    <tr>
                        <td>${key}</td>
                        <#switch securityHeaders[key].VulnType>
                            <#case ("Insecure HTTP Header")>
                                <td><span class="label label-danger">${securityHeaders[key].VulnType}</span></td>
                                <#break>
                            <#case ("Secure HTTP Header")>
                                <td><span class="label label-success">${securityHeaders[key].VulnType}</span></td>
                                <#break>
                            <#default>
                                <td><span class="label label-default">${securityHeaders[key].VulnType}</span></td>
                        </#switch>
                        <td>${securityHeaders[key].Endpoint}</td>
                        <td><span class="label label-default">${securityHeaders[key].Header}</span></td>
                        <td>${securityHeaders[key].Comment}</td>
                    </tr>
                    </#list>

                    </tbody>
                </table>
            </div>
        </div>

    </#if>
    <!--
          <div class="page-header">
            <h1>Labels</h1>
          </div>
          <h1>
            <span class="label label-default">Default</span>
            <span class="label label-primary">Primary</span>
            <span class="label label-success">Success</span>
            <span class="label label-info">Info</span>
            <span class="label label-warning">Warning</span>
            <span class="label label-danger">Danger</span>
          </h1>
          <h2>
            <span class="label label-default">Default</span>
            <span class="label label-primary">Primary</span>
            <span class="label label-success">Success</span>
            <span class="label label-info">Info</span>
            <span class="label label-warning">Warning</span>
            <span class="label label-danger">Danger</span>
          </h2>
          <p>
            <span class="label label-default">Unclear</span>
            <span class="label label-primary">SQL Injection</span>
            <span class="label label-success">Success</span>
            <span class="label label-info">Info</span>
            <span class="label label-warning">Warning</span>
            <span class="label label-danger">Cross-Site Scripting (XSS)</span>
          </p>
    -->


    <div class="page-header">
        <h1>Disclaimer</h1>
    </div>
    <div class="well">
        <p>This tool attacks automatically with a predefined set of payloads. Even if there are no vulnerabilities found,
            this does not guarantee that there are no security vulnerabilities in the tested REST API. Please pentest properly.</p>
    </div>

    Generated by RestSec Reporting Tool 2017

</div>

</body>
</html>
