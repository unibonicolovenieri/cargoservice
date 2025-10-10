%====================================================================================
% sprint1 description   
%====================================================================================
request( createProduct, product(String) ).
reply( createdProduct, productid(ID) ).  %%for createProduct
%====================================================================================
context(ctxcargoservice, "127.0.0.1",  "TCP", "8111").
context(ctx_cargotest, "localhost",  "TCP", "8112").
 qactor( ctxcargoservice, ctxcargoservice, "external").
  qactor( test, ctx_cargotest, "it.unibo.test.Test").
 static(test).
