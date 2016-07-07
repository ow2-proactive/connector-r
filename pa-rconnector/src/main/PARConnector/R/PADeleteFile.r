#' Delete a file inside a ProActive Data space (userspace or globalspace)
#'
#' \code{PADeleteFile} will delete a file stored inside a shared data space. The Scheduler controls two main spaces :\cr
#'
#' \itemize{
#'      \item{The USER Space}{ : a data space reserved for a specific user.}
#'      \item{The GLOBAL Space}{ : a data space accessible to all users.}
#'  }
#'
#' @param space name of the data space where the file must be deleted
#' @param pathname pathname to the file which must be deleted, relative to the space root
#' @param .client connection handle to the scheduler, if not provided the handle created by the last call to \code{\link{PAConnect}} will be used
#' @seealso  \code{\link{PAPullFile}} \code{\link{PAPushFile}}
#' @examples
#'  \dontrun{
#'  PADeleteFile("USER","/myfile.txt") # will delete the file myfile.txt in the USER space
#'  }
#' @export
PADeleteFile <- function(space, pathname,
                         .client = PAClient(), .print.stack = FALSE) {
  if (.client == NULL || is.jnull(.client) ) {
      stop("You are not currently connected to the scheduler, use PAConnect")
  }
  if (!startsWith(pathname,'/')) {
      stop("Error in PADeleteFile('",space,"','",pathname,"') : ", "pathname should start with a /")
  }
  deleted <- FALSE
  j_try_catch(

    deleted <- J(.client, "deleteFile", .getSpaceName(space), pathname),
    .handler = function(e,.print.stack) {
        print(str_c("Error in PADeleteFile('",space,"','",pathname,"') : ", e$jobj$getMessage()))
        if (.print.stack) {
            PAHandler(e,.print.stack)
        }
    }
    ,.print.stack = .print.stack)
  
  return (deleted)
}