%====================================================================================
% sprint1 description   
%====================================================================================
request( load_product, load_product(PID) ).
reply( load_accepted, load_accepted(SLOT) ).  %%for load_product
reply( load_refused, load_refused(CAUSA) ).  %%for load_product
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "10000").
context(ctx_iodev, "localhost",  "TCP", "10001").
context(ctx_test, "localhost",  "TCP", "10002").
context(ctx_gui, "localhost",  "TCP", "10003").
 qactor( cargoservice, ctx_cargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
