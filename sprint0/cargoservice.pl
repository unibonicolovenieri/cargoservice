%====================================================================================
% cargoservice description   
%====================================================================================
request( createProduct, createProduct(Name) ).
reply( createdProduct, createdProduct(PID) ).  %%for createProduct
request( deleteProduct, deleteProduct(PID) ).
reply( deletedProduct, deletedProduct(Name) ).  %%for deleteProduct
request( getProduct, getProduct(PID) ).
reply( addedProduct, product(Name) ).  %%for getProduct
request( loadProduct, loadProduct(PID,Weigth) ).
reply( loadAccepted, loadAccepted(Ok) ).  %%for loadProduct
reply( loadRefused, loadRefused(Cause) ).  %%for loadProduct
dispatch( containerWaiting, containerWaiting(V) ).
dispatch( distance, distance(D) ).
event( stop, stop(V) ).
event( resume, resume(V) ).
dispatch( goToWait, goToWait(V) ).
%====================================================================================
context(ctxcargoservice, "localhost",  "TCP", "8001").
context(ctxioport, "localhost",  "TCP", "8002").
context(ctxcargorobot, "localhost",  "TCP", "8003").
context(ctxtestcargo, "localhost",  "TCP", "8004").
 qactor( productservice, ctxcargoservice, "it.unibo.productservice.Productservice").
 static(productservice).
  qactor( cargoservice, ctxcargoservice, "it.unibo.cargoservice.Cargoservice").
 static(cargoservice).
  qactor( holdmanager, ctxcargoservice, "it.unibo.holdmanager.Holdmanager").
 static(holdmanager).
  qactor( dbwrapper, ctxcargoservice, "it.unibo.dbwrapper.Dbwrapper").
 static(dbwrapper).
  qactor( holdstatusgui, ctxcargoservice, "it.unibo.holdstatusgui.Holdstatusgui").
 static(holdstatusgui).
  qactor( sonarwrapper, ctxioport, "it.unibo.sonarwrapper.Sonarwrapper").
 static(sonarwrapper).
  qactor( alarmdevice, ctxioport, "it.unibo.alarmdevice.Alarmdevice").
 static(alarmdevice).
  qactor( cargorobot, ctxcargorobot, "it.unibo.cargorobot.Cargorobot").
 static(cargorobot).
  qactor( testcargo, ctxtestcargo, "it.unibo.testcargo.Testcargo").
 static(testcargo).
