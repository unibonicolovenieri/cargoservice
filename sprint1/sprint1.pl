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
reply( loadedProduct, slot(slot_id) ).  %%for load_product
request( move_product, product(ID,SLOT) ).
reply( movedProduct, result(SLOT) ).  %%for load_product
request( moverobot, moverobot(TARGETX,TARGETY) ).
reply( moverobotdone, moverobotok(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
request( engage, engage(OWNER,STEPTIME) ).
reply( engagedone, engagedone(ARG) ).  %%for engage
reply( engagerefused, engagerefused(ARG) ).  %%for engage
%====================================================================================
context(ctx_productservice, "127.0.0.1",  "TCP", "8111").
context(ctx_cargotest, "localhost",  "TCP", "8112").
context(ctx_basicrobot, "127.0.0.1",  "TCP", "8020").
context(ctx_cargoservice, "localhost",  "TCP", "8110").
 qactor( basicrobot, ctx_basicrobot, "external").
  qactor( productservice, ctx_productservice, "external").
  qactor( cargoservice, ctx_cargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( cargorobot, ctx_cargoservice, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( test, ctx_cargotest, "it.unibo.test.Test").
 static(test).
