## Nicolas Homble
## Twitch Social Network Graph Calculations

library("statnet")

matrix<-as.matrix(read.table("E:/code/intellij/twitch-analytics/tw-digest/digest/target/twitch_network_companies_popular_v2.csv",
                                sep=",", 
                                header=T,
                                row.names=1,
                                quote="\""))

attr<-read.csv("E:/code/intellij/twitch-analytics/tw-digest/digest/target/twitch_network_attr_companies_popular_v2.csv",
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

