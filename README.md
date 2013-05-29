Demo of client-side routing with Pedestal using goog.History.

There are two implementations of it available under different git tags: 
 * `simple-hardcoded` - very easy to grasp, but quite inelegant and not 
 reusable. Makes heavy use of Pedestal - following a route means pushing a 
 message to the input queue and DOM reaction doesn't happen until a renderer.
 * `generic` - not much more complicated, shows one way to generalize that
 solution. Your mileage may vary, there is more than one way to do it. It also
 uses Pedestal heavily.
 * `generic-light` - lightweight alternative to `generic`. It does not need
 Pedestal for routing at all, but it enables you to interact with Pedestal if
 that's what you want.

To launch the application, run the following:

```
lein repl
(dev)
(run)
```

Once it is started, browse to [http://localhost:3000/routing-app-dev.html](http://localhost:3000/routing-app-dev.html).
