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
event( slot_changed, slot_changed(ID,status) ).
event( sonar_changed, sonar_changed(status) ).
event( led_changed, led_changed(status) ).
request( move_product, product(SLOT) ).
reply( movedProduct, result(SLOT) ).  %%for move_product
reply( moveProductFailed, fail(failed) ).  %%for move_product
event( stop, stop(X) ).
event( resume, resume(X) ).
event( alarm, alarm(X) ).
dispatch( nextmove, nextmove(M) ).
dispatch( nomoremove, nomoremove(M) ).
dispatch( setdirection, dir(D) ).
request( moverobot, moverobot(TARGETX,TARGETY) ).
reply( moverobotdone, moverobotdone(ok) ).  %%for moverobot
reply( moverobotfailed, moverobotfailed(PLANDONE,PLANTODO) ).  %%for moverobot
request( engage, engage(OWNER,STEPTIME) ).
reply( engagedone, engagedone(ARG) ).  %%for engage
reply( engagerefused, engagerefused(ARG) ).  %%for engage
event( container_trigger, container_trigger(X) ).
event( container_absence, container_absence(X) ).
event( sonar_error, sonar_error(CAUSA) ).
event( problem_solved, problem_solved(CAUSA) ).
%====================================================================================
context(ctx_productservice, "cargoserviceqak",  "TCP", "8111").
context(ctx_basicrobot, "basicrobot24",  "TCP", "8020").
context(ctx_cargo, "localhost",  "TCP", "8000").
 qactor( basicrobot, ctx_basicrobot, "external").
  qactor( productservice, ctx_productservice, "external").
  qactor( cargorobot, ctx_cargo, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( cargoservice, ctx_cargo, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( producservice_test, ctx_cargo, "it.unibo.producservice_test.Producservice_test").
 static(producservice_test).
