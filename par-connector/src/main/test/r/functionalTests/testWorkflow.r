source("./utils.r")

connectForTests()

PADebug(TRUE)

# simple split merge
r=PASolve(
  PAM("sum",
      PA(function(x) {x*x},
         PAS("identity", 1:4)))) 

val <- PAWaitFor(r, TEN_MINUTES)
print(val)

if (val[["t6"]] != 30) {
  msg <- paste0("Error when comparing val[t6]=",val[["t6"]], " with sum(1,2^2,3^2,4^2)=30\n")
  stop(msg) 
}
