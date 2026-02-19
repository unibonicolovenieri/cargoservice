%====================================================================================
% sprint3 description   
%====================================================================================
event( slot_changed, slot_changed(ID,status) ).
event( sonar_changed, sonar_changed(status) ).
event( led_changed, led_changed(status) ).
event( container_trigger, container_trigger(X) ).
event( container_absence, container_absence(X) ).
event( sonar_error, sonar_error(CAUSA) ).
event( problem_solved, problem_solved(CAUSA) ).
%====================================================================================
context(ctx_sonartest, "localhost",  "TCP", "8999").
 qactor( sonar_sim, ctx_sonartest, "it.unibo.sonar_sim.Sonar_sim").
 static(sonar_sim).
