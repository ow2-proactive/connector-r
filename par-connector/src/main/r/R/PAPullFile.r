#' Transfer a file from a ProActive Data space to the local machine
#' 
#' \code{PAPullFile} will transfer a file existing in a shared data space to the local computer. The Scheduler controls two main spaces :\cr
#' 
#' \itemize{
#'      \item{The USER Space}{ : a data space reserved for a specific user.}
#'      \item{The GLOBAL Space}{ : a data space accessible to all users.}
#'  } 
#' 
#'  @param space name of the data space to transfer the file to 
#'  @param pathname location of the file inside the remote data space
#'  @param outputFile local path of the file where the file will be copied to. The file must be absolute
#'  @param client connection handle to the scheduler, if not provided the handle created by the last call to \code{\link{PAConnect}} will be used
#'  @param .nb.tries number of total tries (in case of error during the transfer), default to 2
#'  @param .print.stack is the Java stack trace printed in case of error, default to TRUE
#'  @seealso  \code{\link{PAPushFile}}
#'  @examples
#'  \dontrun{
#'  PAPullFile("USER","/in.txt",file.path(getwd(),"in2.txt")) # will transfer file at USER/in.txt to a local file in2.txt
#'  }
PAPullFile <- function(space, pathname, outputFile, 
                       client = PAClient(), .nb.tries = 2, .print.stack = TRUE) {
  
  if (client == NULL || is.jnull(client) ) {
    stop("You are not currently connected to the scheduler, use PAConnect")
  } 
  
  pulled <- FALSE
  
  # convert to absolute path
  jfile <- .jnew(J("java.io.File"),outputFile)
  if (!jfile$isAbsolute()) {
    outputFile <- file.path(getwd(),outputFile)
  }
  
  while(.nb.tries > 0) {    
    j_try_catch(
    {
      pulled <- J(client, "pullFile", .getSpaceName(space),pathname, outputFile)
      .nb.tries <- 0      
    },
    .handler = function(e, .print.stack) {
      .nb.tries <<- .nb.tries - 1
      if (.nb.tries <= 0) {
        if (.print.stack) {
          print(str_c("Error in PAPullFile(",space,",",pathname,",",outputFile,") : ",e$jobj$getMessage()))
        }
        PAHandler(e, .print.stack)       
      }   
    }
    ,.print.stack = .print.stack)
  }
  return (pulled)
}