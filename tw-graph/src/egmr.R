## Nicolas Homble
## Twitch Social Network Graph Calculations

library("statnet")

matrix<-as.matrix(read.table("E:/code/intellij/twitch-analytics/tw-digest/digest/target/user_followers.csv",
                             sep=",", 
                             header=T,
                             row.names=1,
                             quote="\""))

attr<-read.csv("E:/code/intellij/twitch-analytics/tw-digest/digest/target/user_games.csv",  #the name of the attributes file
               header=TRUE,
               sep=",",
               stringsAsFactors = FALSE)

#Convert the data into a network object in statnet
net<-network(matrix,
             vertex.attr = attr,
             vertex.attrnames = colnames(attr),
             directed=T,
             loops=F,
             multiple=F,
             bipartite=F,
             hyper=F)

# Conduct Exponential Random Graph Models (ERGM) Analysis

e2<-ergm(net~edges)  #Create a restricted model with just edges term
summary(e2)

e3<-ergm(net~edges+triangle)  #include a triadic effect in the model
summary(e3)

e4<-ergm(net~edges+triangle+nodematch("Fortnite"))  #Create an unrestricted model
summary(e4)

#e5<-ergm(net~edges+nodematch("type")+nodematch("LeagueofLegends"))  #Testing game homophily hypothesis - LoL players follow each other
#summary(e5)
#e6<-ergm(net~edges+nodematch("type")+nodematch("AStoryAboutMyUncle"))  #Testing game homophily hypothesis - AStoryAboutMyUncle is less popular
#summary(e6)