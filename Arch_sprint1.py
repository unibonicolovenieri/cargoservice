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
with Diagram('sprint1Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
### see https://renenyffenegger.ch/notes/tools/Graphviz/attributes/label/HTML-like/index
     with Cluster('ctx_cargoservice', graph_attr=nodeattr):
          productservice=Custom('productservice(ext)','./qakicons/externalQActor.png')
     with Cluster('ctx_basicrobot', graph_attr=nodeattr):
          basicrobot=Custom('basicrobot(ext)','./qakicons/externalQActor.png')
     with Cluster('ctx_cargo', graph_attr=nodeattr):
          cargorobot=Custom('cargorobot','./qakicons/symActorWithobjSmall.png')
          cargoservice=Custom('cargoservice','./qakicons/symActorWithobjSmall.png')
          test=Custom('test','./qakicons/symActorWithobjSmall.png')
          sonar_test=Custom('sonar_test','./qakicons/symActorWithobjSmall.png')
     sonar_test >> Edge( label='container_arrived', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     sonar_test >> Edge( label='container_absent', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     sonar_test >> Edge( label='interrompi_tutto', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     sonar_test >> Edge( label='riprendi_tutto', **eventedgeattr, decorate='true', fontcolor='red') >> sys
     cargorobot >> Edge(color='magenta', style='solid', decorate='true', label='<engage<font color="darkgreen"> engagedone engagerefused</font> &nbsp; moverobot<font color="darkgreen"> moverobotdone moverobotfailed</font> &nbsp; >',  fontcolor='magenta') >> basicrobot
     test >> Edge(color='magenta', style='solid', decorate='true', label='<load_product<font color="darkgreen"> loadedProduct movedProduct</font> &nbsp; >',  fontcolor='magenta') >> cargoservice
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<getProduct<font color="darkgreen"> getProductAnswer</font> &nbsp; >',  fontcolor='magenta') >> productservice
     test >> Edge(color='magenta', style='solid', decorate='true', label='<createProduct<font color="darkgreen"> createdProduct</font> &nbsp; >',  fontcolor='magenta') >> productservice
     cargoservice >> Edge(color='magenta', style='solid', decorate='true', label='<move_product &nbsp; >',  fontcolor='magenta') >> cargorobot
diag
