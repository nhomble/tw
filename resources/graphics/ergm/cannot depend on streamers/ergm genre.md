==========================
Summary of model fit
==========================

Formula:   net ~ edges + nodematch("genre")

Iterations:  8 out of 20 

Monte Carlo MLE Results:
                Estimate Std. Error MCMC % z value Pr(>|z|)    
edges           -6.61754    0.09584      0 -69.045   <1e-04 ***
nodematch.genre  0.62258    0.24354      0   2.556   0.0106 *  
---
Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

     Null Deviance: 124351  on 89700  degrees of freedom
 Residual Deviance:   1941  on 89698  degrees of freedom
 
AIC: 1945    BIC: 1963    (Smaller is better.) 