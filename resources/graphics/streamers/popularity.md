==========================
Summary of model fit
==========================

Formula:   net ~ edges + nodecov("followers")

Iterations:  10 out of 20 

Monte Carlo MLE Results:
                    Estimate Std. Error MCMC % z value Pr(>|z|)    
edges             -9.094e+00  4.527e-02      0 -200.91   <1e-04 ***
nodecov.followers  2.160e-06  2.436e-08      0   88.67   <1e-04 ***
---
Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

     Null Deviance: 5542405  on 3998000  degrees of freedom
 Residual Deviance:   12949  on 3997998  degrees of freedom
 
AIC: 12953    BIC: 12980    (Smaller is better.) 