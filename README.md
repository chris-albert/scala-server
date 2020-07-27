# Scala Server

This application is a bare-bones Http4s server setup with ZIO

There are also a few utilities like logging, metrics, configuration, and reading files.

Instead of using the ZIO layers paradigm of wiring dependencies I went with just wiring them the
old-fashioned way by using function arguments. The layers way has gotten messy for me in the past 
(i also could be doing it wrong) and also makes it harder for new scala devs to understand.

Also instead of using typed errors for each service, I'm just using an `Error` co-product and having 
all the errors in one spot. Having typed errors is nice and all, but I have noticed that it
creates lots of boiler-plate when you need to make between errors. I also don't like just using 
`Exception` since I like being able to pattern match on my errors sometimes in controller error handling.

### Commands

Running the app
```
sbt run
```

Compiling on every change
```
sbt ~compile
```