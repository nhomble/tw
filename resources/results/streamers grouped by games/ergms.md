```r
vanilla<-ergm(net~edges)  #Create a restricted model with just edges term
summary(vanilla)
```
Consider a vanilla model with no constraints, if we

```
Formula:   net ~ edges

Iterations:  6 out of 20 

Monte Carlo MLE Results:
       Estimate Std. Error MCMC % z value Pr(>|z|)    
edges -2.483078   0.005306      0    -468   <1e-04 ***
---
Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

     Null Deviance: 692454  on 499500  degrees of freedom
 Residual Deviance: 271241  on 499499  degrees of freedom
 
AIC: 271243    BIC: 271254    (Smaller is better.) 
``` 

and the probability of an edge is

```
> plogis(coef(vanilla)[['edges']])
[1] 0.07705305
```

now let's constrain against different game titles

```
cod = ergm(net ~ edges
           + nodematch("Call.of.Duty..Black.Ops.4")
           + nodematch("Call.of.Duty..Black.Ops.II")
           + nodematch("Call.of.Duty..Black.Ops.III")
           + nodematch("Call.of.Duty..Ghosts")
           + nodematch("Call.of.Duty..Infinite.Warfare")
           + nodematch("Call.of.Duty..Modern.Warfare.Remastered")
           + nodematch("Call.of.Duty..World.at.War..Zombies")
           + nodematch("Call.of.Duty..WWII")
)
summary(cod)
> plogis(coef(cod)[['edges']])
[1] 0.0001910088
```

```
dark_souls <- ergm(net ~ edges + nodematch("Dark.Souls") + nodematch("Dark.Souls.II") + nodematch("Dark.Souls.III"))
summary(dark_souls)
plogis(coef(dark_souls)[['edges']])
[1] 0.04002958
```

```
dota <- ergm(net ~ edges + nodematch("Dota.2") + nodematch("Dota.2.1"))
summary(dota)
[1] 0.0169703
```

```
lol <- ergm(net ~ edges + nodematch("League.of.Legends"))
summary(lol)
> plogis(coef(lol)[['edges']])
[1] 0.09332467
```

```
fifa <- ergm(net ~ edges 
             + nodematch("FIFA.14")
             + nodematch("FIFA.15")
             + nodematch("FIFA.16")
             + nodematch("FIFA.17")
             + nodematch("FIFA.18")
             + nodematch("FIFA.19")
             + nodematch("fifa14")
             + nodematch("FIFA.Soccer.13")
        )
summary(fifa)
> plogis(coef(fifa)[['edges']])
[1] 9.336017e-05
```

```
csgo <- ergm(net ~ edges + nodematch("Counter.Strike..Global.Offensive"))
summary(csgo)
> plogis(coef(csgo)[['edges']])
[1] 0.09878613
```

```battlefield
battlefield <- ergm(net ~edges 
                + nodematch("Battlefield.1")
                + nodematch("Battlefield.3")
                + nodematch("Battlefield.4")
          )
> plogis(coef(battlefield)[['edges']])
[1] 0.0361809          
```

```
> monster <- ergm(net ~ edges + nodematch("Monster.Hunter.World"))
> plogis(coef(monster)[['edges']])
[1] 0.07705305
```

```
fortnite <- ergm(net ~ edges + nodematch("Fortnite"))
> plogis(coef(fortnite)[['edges']])
[1] 0.08450216
```

Working at our scale of around ~10% of users. We see that there are certain game titles that slightly unite
first degree viewers to streamers of similar genres but what's more apparent is that some titles drive
the edge probability to zero.

One question we could ask here is how dense are the streamers in a category? In our sampled dataset, 
are there too few or too many streamers for a title that is skewing the data.

To get a profile of our streamers
```
{'asmr': 46, 'destiny': 0, 'diablo': 870, 'monster': 606, 'ds': 2161, 'detroit': 445, 'fortnite': 5941, 'cod': 3206, 'bf': 1298, 'fifa': 629, 'csgo': 38, 'dayz': 1058, 'lol': 4998, 'dota': 1165}
total: 110888
```
SCRIPTS = \[ "first_order_linkage.py", "counts.py" \]
No that we have a relation by first degree viewers, let's see if we can mimic that relationship by direct streamer follows.

```
vanilla <- ergm(net ~ edges)
summary(vanilla)
plogis(coef(vanilla)['edges'])
```

```
|game|streamer prob|first deg|
|none|0.07705305|0.07705305|
|dota|0.01623015|0.0169703|
|fortnite|0.07689867|0.08450216|
|csgo|0.0871271|0.09878613|
|battlefield|0.02203259|0.0361809|
|dark souls|0.05952349|0.04002958|
|lol|0.08246528|0.09332467|
|fifa|0.0008442783|9.336017e-05|
|detroit|0.08911748|0.05210421|
|destiny|0.06708468|0.06460705|
|dayz|0.0905314|0.07692308|
|diablo|0.04800023|0.05360721|
|asmr|0.2540956|0.6346346|
|tarkov||0.05105105|
|fallout||0.03029401|
```

consider the game popularity as defined by Twitch
```
game=battlefield
data={'viewers': 105, 'popularity': 96}
game=destiny
data={'viewers': 5119, 'popularity': 5061}
game=detroit
data={'viewers': 99, 'popularity': 93}
game=battle
data={'viewers': 105, 'popularity': 96}
game=monster
data={'viewers': 1126, 'popularity': 1098}
game=League
data={'viewers': 80580, 'popularity': 77567}
game=dayz
data={'viewers': 393, 'popularity': 380}
game=CSGO
data={'viewers': 9153, 'popularity': 9342}
game=dark souls
data={'viewers': 1287, 'popularity': 1261}
game=fortnite
data={'viewers': 68311, 'popularity': 65615}
game=dota
data={'viewers': 78914, 'popularity': 76028}
game=asmr
data={'viewers': 3397, 'popularity': 3415}
game=fifa
data={'viewers': 10234, 'popularity': 10433}
game=diablo
data={'viewers': 955, 'popularity': 1193}
```