%====================================================================================
% sprint2 description   
%====================================================================================
event( slot_changed, slot_changed(ID,status) ).
event( sonar_changed, sonar_changed(status) ).
event( led_changed, led_changed(status) ).
event( container_trigger, container_trigger(X) ).
event( container_absence, container_absence(X) ).
event( sonar_error, sonar_error(CAUSA) ).
event( problem_solved, problem_solved(CAUSA) ).
event( led_on, led_on(X) ).
event( led_off, led_off(X) ).
event( sonardata, distance(D) ).
%====================================================================================
context(ctx_iodevices, "localhost",  "TCP", "7777").
context(ctx_cargo, "10.249.112.148",  "TCP", "8000").
 qactor( sprint1, ctx_cargo, "external").
  qactor( sonar, ctx_iodevices, "it.unibo.sonar.Sonar").
 static(sonar).
  qactor( measure_handler, ctx_iodevices, "it.unibo.measure_handler.Measure_handler").
 static(measure_handler).
  qactor( led, ctx_iodevices, "it.unibo.led.Led").
 static(led).
