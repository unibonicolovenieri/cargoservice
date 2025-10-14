%====================================================================================
% sprint1 description   
%====================================================================================
request( createProduct, product(String) ).
reply( createdProduct, productid(ID) ).  %%for createProduct
request( getAllProducts, dummy(ID) ).
reply( getAllProductsAnswer, products(String) ).  %%for getAllProducts
request( getProduct, product(ID) ).
reply( getProductAnswer, product(JSonString) ).  %%for getProduct
request( moverobot, moverobot(TARGETX,TARGETY) ).
reply( moverobotdone, moverobotok(ARG) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
request( engage, engage(OWNER,STEPTIME) ).
reply( engagedone, engagedone(ARG) ).  %%for engage
reply( engagerefused, engagerefused(ARG) ).  %%for engage
%====================================================================================
context(ctx_productservice, "127.0.0.1",  "TCP", "8111").
context(ctx_cargotest, "localhost",  "TCP", "8112").
context(ctxbasicrobot26usage, "127.0.0.1",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot26usage, "external").
  qactor( productservice, ctx_productservice, "external").
  qactor( cargoservice, ctx_cargotest, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
