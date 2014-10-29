source("./utils.r")

connectForTests()

PADebug(F)

f <- function(x) list(1, 2)

res <- PASolve(f, 1)
val <- PAWaitFor(res, TEN_MINUTES)

print(val)

if (!identical(list(1, 2), val[[1]])) {
  msg <- paste0("Expected: list(1,2), got: ", toString(val[[1]]), "\n")
  stop(msg)
}

