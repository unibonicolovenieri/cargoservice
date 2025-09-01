%====================================================================================
% sprint0 description   
%====================================================================================
request( load_product, load_product(PID) ).
reply( load_accepted, load_accepted(SLOT) ).  %%for load_product
reply( load_refused, load_refused(CAUSA) ).  %%for load_product
%====================================================================================
context(ctx_cargoservice, "localhost",  "TCP", "8000").
context(ctx_iodev, "localhost",  "TCP", "8001").
context(ctx_client, "localhost",  "TCP", "8002").
context(ctx_test, "localhost",  "TCP", "9000").
 qactor( cargoservice, ctx_cargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
