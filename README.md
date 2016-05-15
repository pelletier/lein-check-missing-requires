# lein-check-missing-requires

A Leiningen plugin to check if you didn't forget to `:require` your `:import`s.

## Usage

Put `[lein-check-missing-requires "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

    $ lein check-missing-requires
    
Will emit some warnings if any `:import` isn't `:require`d. For example:

```clj
(ns my.super-ns
    (:import (my.other-ns MyClass))
```

Will emit the warning:

```
example.clj: Some imports are not required: #{my.other-ns}
```

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
