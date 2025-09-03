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
with Diagram('cargoserviceArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctxcargoservice', graph_attr=nodeattr):
          productservice=Custom('productservice','./qakicons/symActorWithobjSmall.png')
          cargoservice=Custom('cargoservice','./qakicons/symActorWithobjSmall.png')
          holdmanager=Custom('holdmanager','./qakicons/symActorWithobjSmall.png')
          dbwrapper=Custom('dbwrapper','./qakicons/symActorWithobjSmall.png')
          holdstatusgui=Custom('holdstatusgui','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctxioport', graph_attr=nodeattr):
          sonarwrapper=Custom('sonarwrapper','./qakicons/symActorWithobjSmall.png')
          alarmdevice=Custom('alarmdevice','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctxcargorobot', graph_attr=nodeattr):
          cargorobot=Custom('cargorobot','./qakicons/symActorWithobjSmall.png')
     with Cluster('ctxtestcargo', graph_attr=nodeattr):
          testcargo=Custom('testcargo','./qakicons/symActorWithobjSmall.png')
     sys >> Edge( label='stop', **evattr, decorate='true', fontcolor='darkgreen') >> cargoservice
     sys >> Edge( label='resume', **evattr, decorate='true', fontcolor='darkgreen') >> cargoservice
     alarmdevice >> Edge( label='stop', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     alarmdevice >> Edge( label='resume', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     sys >> Edge( label='stop', **evattr, decorate='true', fontcolor='darkgreen') >> cargorobot
     sys >> Edge( label='resume', **evattr, decorate='true', fontcolor='darkgreen') >> cargorobot
     productservice >> Edge(color='magenta', style='solid', decorate='true', label='<createProduct<font color="darkgreen"> createdProduct</font> &nbsp; deleteProduct<font color="darkgreen"> deletedProduct</font> &nbsp; getProduct<font color="darkgreen"> addedProduct</font> &nbsp; >',  fontcolor='magenta') >> dbwrapper
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<getProduct<font color="darkgreen"> addedProduct</font> &nbsp; >',  fontcolor='magenta') >> dbwrapper
     testcargo >> Edge(color='magenta', style='solid', decorate='true', label='<loadProduct<font color="darkgreen"> loadAccepted loadRefused</font> &nbsp; >',  fontcolor='magenta') >> cargoservice
diag
