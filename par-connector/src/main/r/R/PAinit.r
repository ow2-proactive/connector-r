.onLoad <- function(libname, pkgname) {

  .jpackage(pkgname, lib.loc = libname)
  
  # callback engine
  .jengine(start=TRUE)

  #.jinit()
 
  
  # if this fails, there is a problem with classloading
  J("org.ow2.proactive.scripting.Script")
  
  # activate some debug info
  options(error = utils::dump.frames)
  
}


local({
  if (Sys.getenv("RSTUDIO") == "1") {
    require(devtools)
    wd(pkg = "PARConnector", path = "")
  }

  require(rJava)
  pkg.root <- getwd()
  if ((basename(pkg.root) == "R") && (length(list.files(pattern="DESCRIPTION")) == 0)) {
    pkg.root <- dirname(pkg.root)
  }
  cat("Building PARConnector from :\n")
  print(pkg.root)
  .jinit()
  .jaddClassPath(dir(file.path(pkg.root,"inst","java"), full.names=TRUE))
  cat("---- Classpath of PARConnector (make sure it contains all ProActive jars) : ----\n")
  print(.jclassPath())
  cat("-------------------------------  End of class path -----------------------------\n")
  cat("If an error occurs in the following code, then there is a problem with the classpath or jar versions\n\n")


  # if this fails, there is a problem with classloading
  J("org.ow2.proactive.scripting.Script")

 })

.findroot <- function() {
  getwd()
}
  


