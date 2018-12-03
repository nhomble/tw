## Nicolas Homble
## Twitch Social Network Graph Calculations

library("statnet")

matrix<-as.matrix(read.table("<>",
                                sep=",", 
                                header=T,
                                row.names=1,
                                quote="\""))

attr<-read.csv("<>",
                  header=TRUE,
                  sep=",",
                  stringsAsFactors = FALSE)

net<-network(matrix,
                vertex.attr = attr,
                vertex.attrnames = colnames(attr),
                directed=T,
                loops=F,
                multiple=F,
                bipartite=F,
                hyper=F)

plot(net,
     vertex.col="blue",
     label=get.vertex.attribute(net, "vertex.names"),
     label.cex=.7)