# closurvey

Closurvey is a small, simple web-based app for conducting surveys.

## Goal

* The goal is to provide a simple yet customizable, self-contained web app for conducting surveys.
* Data should be in a readily accessible format, e.g. json or edn

## Non-goals

* This app does not include user management and does not collect user identification. Data ownership is managed entirely by pass-phrases. Unless customized to integrate with authentication systems, pass-phrases can only be reset manually.

* This app does not collect or send emails. It is up to the survey organizer to provide the link to the respondents.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run 

## License

Copyright © 2018 Kean Santos
