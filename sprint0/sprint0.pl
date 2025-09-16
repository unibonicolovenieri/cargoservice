%====================================================================================
% sprint0 description   
%====================================================================================
request( load_product, load_product(PID) ).
reply( load_accepted, load_accepted(SLOT) ).  %%for load_product
reply( load_refused, load_refused(CAUSA) ).  %%for load_product
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "10000").
context(ctx_iodev, "localhost",  "TCP", "10001").
context(ctx_test, "localhost",  "TCP", "10002").
 qactor( cargoservice, ctx_cargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( webgui, ctx_cargoservice, "it.unibo.webgui.Webgui").
 static(webgui).
  qactor( sonar, ctx_iodev, "it.unibo.sonar.Sonar").
 static(sonar).
  qactor( led, ctx_iodev, "it.unibo.led.Led").
 static(led).
  qactor( cargotest, ctx_cargoservice, "it.unibo.cargotest.Cargotest").
 static(cargotest).
