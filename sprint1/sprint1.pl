%====================================================================================
% sprint1 description   
%====================================================================================
request( createProduct, product(String) ).
reply( createdProduct, productid(ID) ).  %%for createProduct
request( getAllProducts, dummy(ID) ).
reply( getAllProductsAnswer, products(String) ).  %%for getAllProducts
request( getProduct, product(ID) ).
reply( getProductAnswer, product(JSonString) ).  %%for getProduct
request( load_product, product(ID) ).
reply( loadedProduct, slot(SLOT) ).  %%for load_product
request( move_product, product(SLOT) ).
reply( movedProduct, result(SLOT) ).  %%for load_product
request( moverobot, moverobot(TARGETX,TARGETY) ).
reply( moverobotdone, moverobotdone(ok) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
request( engage, engage(OWNER,STEPTIME) ).
reply( engagedone, engagedone(ARG) ).  %%for engage
reply( engagerefused, engagerefused(ARG) ).  %%for engage
%====================================================================================
context(ctx_cargoservice, "127.0.0.1",  "TCP", "8111").
context(ctx_basicrobot, "127.0.0.1",  "TCP", "8020").
context(ctx_cargo, "localhost",  "TCP", "8000").
 qactor( basicrobot, ctx_basicrobot, "external").
  qactor( productservice, ctx_cargoservice, "external").
  qactor( cargorobot, ctx_cargo, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( cargoservice, ctx_cargo, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( test, ctx_cargo, "it.unibo.test.Test").
 static(test).
