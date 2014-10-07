# this test tests function dependency transfer, both for main function and for parameter functions
source("./utils.r")

connectForTests()

PADebug(TRUE)

# use foo as main function, check that dependencies of foo are transferred
a <- 1
foo <- function(x)x*a
bar <- function(x)foo(x)


# use foo as main function
res <- PASolve(foo,1:3)
val <- PAWaitFor(res, TEN_MINUTES)
print(val)

if (!all(unlist(val) == 1:3)) {
  msg <- paste0("Error when comparing val=",toString(unlist(val)), " with foo(1:3)=",toString(1:3),"\n")
  stop(msg) 
}

# use foo as parameter, check that dependencies of foo are transferred

boo <- function(f,x)f(x)

res <- PASolve(boo,foo,1:3)

val <- PAWaitFor(res, TEN_MINUTES)
print(val)

if (!all(unlist(val) == 1:3)) {
  msg <- paste0("Error when comparing val=",toString(unlist(val)), " with foo(1:3)=",toString(1:3),"\n")
  stop(msg) 
}
