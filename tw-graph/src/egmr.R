## Nicolas Homble
## Twitch Social Network Graph Calculations

library("statnet")

matrix<-as.matrix(read.table("E:/code/intellij/twitch-analytics/tw-digest/digest/target/twitch_network_companies.csv",
                             sep=",", 
                             header=T,
                             row.names=1,
                             quote="\""))

attr<-read.csv("E:/code/intellij/twitch-analytics/tw-digest/digest/target/twitch_network_attr_companies.csv",
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

vanilla = ergm(net ~ edges)
summary(vanilla)

dota <- ergm(net ~ edges + nodematch("dota_c"))
summary(dota)

riot <- ergm(net ~ edges + nodematch("riot"))
summary(riot)

fortnite <- ergm(net ~ edges + nodematch("fortnite_c"))
summary(fortnite)

pubg <- ergm(net ~ edges + nodematch("pubg"))
summary(pubg)

sc <- ergm(net ~ edges + nodematch("starcraft"))
csgo <- ergm(net ~ edges + nodematch("csgo"))
bethesda <- ergm(net ~ edges + nodematch("bethesda"))

dota = ergm(net ~ edges + nodematch("dota"))
summary(dota)

fortnite = ergm(net ~ edges + nodematch("fortnite"))
summary(fortnite)

lol  = ergm(net ~ edges + nodematch("lol"))
summary(lol)

cod = ergm(net ~ edges + nodematch("cod") + nodematch("fortnite"))
summary(cod)

fifa <- ergm(net ~ edges + nodematch("fifa"))
summary(fifa)

wc <- ergm(net ~ edges + nodematch("warcraft"))
summary(wc)

ds <- ergm(net ~ edges + nodematch("darksouls"))
summary(ds)

sc <- ergm(net ~ edges + nodematch("sc"))
summary(sc)

diablo <- ergm(net ~ edges + nodematch("diablo"))
summary(diablo)

ergms <- c(vanilla, dota, fortnite, lol, cod, fifa, wc, ds, sc, diablo)
sapply(ergms, function(x) plogis(coef(x)[['edges']]))
#e5<-ergm(net~edges+nodematch("type")+nodematch("LeagueofLegends"))  #Testing game homophily hypothesis - LoL players follow each other
#summary(e5)
#e6<-ergm(net~edges+nodematch("type")+nodematch("AStoryAboutMyUncle"))  #Testing game homophily hypothesis - AStoryAboutMyUncle is less popular
#summary(e6)