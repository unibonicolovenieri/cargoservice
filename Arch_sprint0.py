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
with Diagram('sprint0Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctx_cargoservice', graph_attr=nodeattr):
          cargoservice=Custom('cargoservice','./qakicons/symActorWithobjSmall.png')
          webgui=Custom('webgui','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_iodev', graph_attr=nodeattr):
          sonar=Custom('sonar','./qakicons/symActorWithobjSmall.png')
          led=Custom('led','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_client', graph_attr=nodeattr):
          client=Custom('client','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctx_test', graph_attr=nodeattr):
          cargotest=Custom('cargotest','./qakicons/symActorWithobjSmall.png')
     cargotest >> Edge(color='magenta', style='solid', decorate='true', label='<load_product<font color="darkgreen"> load_accepted load_refused</font> &nbsp; >',  fontcolor='magenta') >> cargoservice
diag
