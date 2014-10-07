library("PARConnector");
# Some function usefull functions for tests

# Defautl url of the rest server
REST_SERVER_URL <- "http://localhost:8080/rest"

# Default timeout for PAWaitFor
TEN_MINUTES <- 1000 * 60 * 60

# error handler to quit the R session on error
options(
  error = bquote(
{ifelse(is.element("QUITONERROR",commandArgs(TRUE)), 
        q(save = "no", status=1), stop("Error during test"))
}))

# Create nb files named like prefix1, prefix2, ... prefixn
createFiles <- function(prefix, nb) {
  for (i in 1:nb) {
    name <- paste0(prefix,i)
    if (file.exists(name)) {
      file.remove(name)
    }
    file.create(name, showWarnings = TRUE)
    fileConn<-file(name)
    writeLines(c("Hello","World"), fileConn)
    close(fileConn)
  }
}

# Removes nb files named like prefix1, prefix2, ... prefixn
removeFiles <- function(prefix, nb) {
  for (i in 1:nb) {
    name <- paste0(prefix,i)    
    if(file.exists(name)) {
      file.remove(name)
    }    
  }
}

connectForTests <- function() {    
  cat("*** Connecting to ", REST_SERVER_URL, "\n") 
  result = tryCatch({
      PAConnect(url=REST_SERVER_URL, login='demo', pwd='demo')
      cat("Sucessfully connected ...", "\n");
  }, warning = function(w) {
  }, error = function(e) {
      cat("!! UNABLE TO CONNECT TO ", REST_SERVER_URL, "!!\n");
  }, finally = {
  })
}