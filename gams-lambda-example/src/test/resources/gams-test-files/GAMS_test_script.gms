
* DYNAMIC VARIABLES

set      timestamp;

parameters
           nominated_control_area_load(timestamp) 'predicted load of the Control Area CA'
           
           solar_one_prediction(timestamp) solar power station one power production (kW)
           solar_two_prediction(timestamp) solar power station two power production (kW)
          
           our_nominated_control_area_load(timestamp)
;

variables

a
b
;

*reading parameters

$GDXIN dbInVariables.gdx

$LOAD timestamp
$LOAD nominated_Control_Area_load

$LOAD solar_one_prediction
$LOAD solar_two_prediction

$GDXIN

variable z;

loop(timestamp,
           
           our_nominated_control_area_load(timestamp)=nominated_control_area_load(timestamp) - (solar_one_prediction(timestamp) + solar_two_prediction(timestamp));
          
                      
           );

equations

eq1 objective equation
eq2
eq3
eq4
eq5
;


eq1..            z =e= a+b;
eq2..            a =l= sum(timestamp,our_nominated_control_area_load(timestamp));  
eq3..            b =g= 100;
eq4..            a =g= 0;
eq5..            b =l= 200;

model battery /all/;

* Dictionary

option optcr=0;
option limrow=100000;
solve battery minimizing z using mip;

execute_unload '%dbOut%',z, our_nominated_control_area_load;
