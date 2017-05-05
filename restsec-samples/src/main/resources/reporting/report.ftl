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



<!-- Fixed navbar -->
<!--
<nav class="navbar navbar-inverse navbar-fixed-top">
  <div class="container">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="#">Bootstrap theme</a>
    </div>
    <div id="navbar" class="navbar-collapse collapse">
      <ul class="nav navbar-nav">
        <li class="active"><a href="#">Home</a></li>
        <li><a href="#about">About</a></li>
        <li><a href="#contact">Contact</a></li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Dropdown <span class="caret"></span></a>
          <ul class="dropdown-menu">
            <li><a href="#">Action</a></li>
            <li><a href="#">Another action</a></li>
            <li><a href="#">Something else here</a></li>
            <li role="separator" class="divider"></li>
            <li class="dropdown-header">Nav header</li>
            <li><a href="#">Separated link</a></li>
            <li><a href="#">One more separated link</a></li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
</nav>

-->

<div class="container theme-showcase" role="main">

    <!-- Main jumbotron for a primary marketing message or call to action -->
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
    </div>

    <!--
  <div class="page-header">
    <h1>Alerts</h1>
  </div>
    -->

    <#if counter_vulns == 0>
        <div class="alert alert-success" role="alert">
            <strong>Success!</strong> No vulnerabilites found.
        </div>
    <#else>
        <div class="alert alert-danger" role="alert">
            <strong>Danger!</strong> ${counter_vulns} payload(s) executed successfully.
        </div>

        <div class="page-header">
            <h1>Details</h1>
        </div>
        <div class="row">
            <div class="col-md-6">
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Vulnerability Type</th>
                        <th>Endpoint</th>
                        <th>Payload</th>
                        <th>Comment</th>
                    </tr>
                    </thead>
                    <tbody>
                    <!-- LOOP THROUGH THE VULNERABILITES FOUND -->

                    <#assign ls = ["whale", "Barbara", "zeppelin", "aardvark", "beetroot"]?sort>
                    <#list ls as i>
                        ${i}
                    </#list>

                    <#list 1..counter_vulns as i>
                        <#assign var = "vuln_" + i>
                        ${var}
                    </#list>

                    <tr>
                        <td>1</td>
                        <td><span class="label label-danger">${vuln_1.VulnType}</span></td>
                        <td>${vuln_1.Endpoint}</td>
                        <td>${vuln_1.Payload}</td>
                        <td>${vuln_1.Comment}</td>
                    </tr>

                    <tr>
                        <td>2</td>
                        <td><span class="label label-danger">${vuln_2.VulnType}</span></td>
                        <td>${vuln_2.Endpoint}</td>
                        <td>${vuln_2.Payload}</td>
                        <td>${vuln_2.Comment}</td>
                    </tr>


                    <tr>
                        <td>1</td>
                        <td><span class="label label-danger">Cross-Site Scripting (XSS)</span></td>
                        <td>Mark</td>
                        <td>Otto</td>
                        <td>@mdo</td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td><span class="label label-danger">Cross-Site Scripting (XSS)</span></td>
                        <td>Jacob</td>
                        <td>Thornton</td>
                        <td>@fat</td>
                    </tr>
                    <tr>
                        <td>3</td>
                        <td><span class="label label-primary">SQL Injection</span></td>
                        <td>Larry</td>
                        <td>the Bird</td>
                        <td>@twitter</td>
                    </tr>
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
