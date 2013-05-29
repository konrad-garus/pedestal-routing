Demo of client-side routing with Pedestal using goog.History.

There are two implementations of it available under different git tags: 
 * `simple-hardcoded` - very easy to grasp, but quite inelegant and not 
 reusable.
 * `generic` - not much more complicated, shows one way to generalize that
 solution. Your mileage may vary, there is more than one way to do it.

To launch the application, run the following:

```
lein repl
(dev)
(run)
```

Once it is started, browse to [http://localhost:3000/routing-app-dev.html](http://localhost:3000/routing-app-dev.html).
