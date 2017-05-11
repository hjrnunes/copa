# COPA - Culinary Operating Procedure Adviser

COPA is a Clojure/Clojurescript recipe manager. It is a bit rough around the edges, and it's purpose is mainly for me to learn Clojurescript and related frontend technologies. But, it's a fairly complete example of a Clojure/Clojurescript SPA with an API backend so it might be useful for beginners.

Possibly interesting features to more advanced coders are Markdown support for the recipes and frontend localization in Clojurescript (only English and Portuguese, but trivial to add more).

COPA is based on a [Luminus][1] template.

It's Clojure on the backend with a Swagger API via [compojure-api][2]. Database access via [HugSQL][9] on an H2 database.

Frontend is Clojurescript with [reagent][3] and [re-frame][4]. At one point it also included [re-com][5], but it's now Semantic-UI.

URL based navigation with [pushy][7] and [bidi][8].

Authentication is JWT via [buddy][6].

[1]: http://www.luminusweb.net
[2]: https://github.com/metosin/compojure-api
[3]: https://github.com/reagent-project/reagent
[4]: https://github.com/Day8/re-frame
[5]: https://github.com/Day8/re-com
[6]: https://github.com/funcool/buddy
[7]: https://github.com/funcool/buddy
[8]: https://github.com/funcool/buddy
[9]: https://github.com/funcool/buddy

#todo fix links

## Prerequisites

You will need [Leiningen][10] 2.0 or above installed.

[10]: https://github.com/technomancy/leiningen

## Initalization

You'll need to run 
    
    lein migratus
    
to execute the migrations that initialize the H2 database on the backend. 

The H2 database location can be configured in ```profiles.clj``` like this:
 
 ```clojure
 {:profiles/dev  {:env {:database-url "jdbc:h2:./copa;MVCC=true"}}
  :profiles/test {:env {:database-url "jdbc:h2:./copa_test;MVCC=true"}}}
 ```

There should be a default user with which you can login initially: 

```username: admin```

```password: admin```


## Running

To start a web server for the application, run:

    lein run

To start Clojurescript auto-compiling via [figwheel][8], run:

    lein figwheel

[8]: https://github.com/bhauman/lein-figwheel

## Deploying

Build a uberjar with

    lein uberjar

And then run it with

    java -jar copa.jar

COPA will start on localhost:3000

## Copyright and License

The MIT License (MIT)

Copyright © 2017 Henrique Nunes

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
