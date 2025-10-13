%====================================================================================
% sprint1 description   
%====================================================================================
request( createProduct, product(String) ).
reply( createdProduct, productid(ID) ).  %%for createProduct
request( getAllProducts, dummy(ID) ).
reply( getAllProductsAnswer, products(String) ).  %%for getAllProducts
request( getProduct, product(ID) ).
reply( getProductAnswer, product(JSonString) ).  %%for getProduct
%====================================================================================
context(ctx_productservice, "127.0.0.1",  "TCP", "8111").
context(ctx_cargotest, "localhost",  "TCP", "8112").
 qactor( productservice, ctx_productservice, "external").
  qactor( test, ctx_cargotest, "it.unibo.test.Test").
 static(test).
