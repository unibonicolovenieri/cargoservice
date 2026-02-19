### conda install diagrams
from diagrams import Cluster, Diagram, Edge
from diagrams.custom import Custom
import os
os.environ['PATH'] += os.pathsep + 'C:/Program Files/Graphviz/bin/'

graphattr = {     #https://www.graphviz.org/doc/info/attrs.html
    'fontsize': '22',
}

nodeattr = {   
    'fontsize': '22',
    'bgcolor': 'lightyellow'
}

eventedgeattr = {
    'color': 'red',
    'style': 'dotted'
}
evattr = {
    'color': 'darkgreen',
    'style': 'dotted'
}
with Diagram('sprint2Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctx_iodevices', graph_attr=nodeattr):
          sonar=Custom('sonar','./qakicons/symActorWithobjSmall.png')
          measure_handler=Custom('measure_handler','./qakicons/symActorWithobjSmall.png')
          led=Custom('led','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_cargo', graph_attr=nodeattr):
          sprint1=Custom('sprint1(ext)','./qakicons/externalQActor.png')
     sonar >> Edge( label='sonardata', **eventedgeattr, decorate='true', fontcolor='red') >> measure_handler
     measure_handler >> Edge( label='problem_solved', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     measure_handler >> Edge( label='container_trigger', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     measure_handler >> Edge( label='sonar_error', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     sys >> Edge( label='led_on', **evattr, decorate='true', fontcolor='darkgreen') >> led
     sys >> Edge( label='led_off', **evattr, decorate='true', fontcolor='darkgreen') >> led
diag
