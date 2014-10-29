source("./utils.r")

connectForTests()

n <- 4

progressfunc <- function(x) {
  for (i in 1:100) {
    Sys.sleep(0.1)
    set_progress(i)    
  } 
  return(x)
} 

# testing with standard indexes replacements


res <- PASolve(progressfunc,1:n)
# progress should be updated in the following lines 
# when the progress will be correctly exposed by the rest api (for now it is not updated)
res
Sys.sleep(4)
res
Sys.sleep(4)
res
Sys.sleep(4)
val <- PAWaitFor(res, TEN_MINUTES)
print(val)

if (!all(unlist(val) == 1:4)) {
  msg <- paste0("Error when comparing val=",toString(unlist(val)), " with 1:4\n")
  stop(msg) 
}

printspaces <- function(x) {
  eval(parse(text=paste0(x,"space")))
  return(eval(parse(text=paste0(x,"space"))))
} 
# check that spaces urls are correct
res <- PASolve(printspaces,c("local","input","output", "global","user"))
val <- PAWaitFor(res, TEN_MINUTES)
print(val)
