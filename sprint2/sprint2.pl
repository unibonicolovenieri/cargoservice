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
event( sonardata, distance(D) ).
%====================================================================================
context(ctx_iodevices, "localhost",  "TCP", "7777").
context(ctx_sprint1, "10.249.112.148",  "TCP", "8000").
 qactor( sprint1, ctx_sprint1, "external").
  qactor( sonar, ctx_iodevices, "it.unibo.sonar.Sonar").
 static(sonar).
