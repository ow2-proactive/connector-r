source("./utils.r")

connectForTests()

r = PASolve('sin', 1)

r2 <- PAGetResult(r@job.id)

val <- PAWaitFor(r2, TEN_MINUTES)

if (unlist(val) != sin(1)) {
  msg <- paste0("Error when comparing val=",toString(unlist(val)), " with sin(1)=",toString(sin(1)),"\n" )
  stop(msg) 
}
