#' Returns a PAJobResult object for a specific jobid
#'
#' \code{PAGetResult} returns a \code{PAJobResult} object from a previously submitted job.
#' It can be manipulated the same way as the object returned during the PASolve call
#' It can also be used to retrieve a job submitted during a previous R session (disconnected mode)
#'
#' @param job.id id of the proactive job
#' @param client connection handle to the scheduler, if not provided the handle created by the last call to \code{\link{PAConnect}} will be used
#' @seealso  \code{\link{PAJobResult-class}} \code{\link{PAWaitFor}} \code{\link{PAWaitAny}} \code{\link{PASolve}}
#' @examples
#'  \dontrun{
#' > r <- PASolve('cos',1:5)
#'   Job submitted (id : 2)
#'   with tasks : t1, t2, t3, t4, t5
#' > r2 <- PAGetResult(2)
#' > r2
#'   PARJob2 (id: 2)  (status: Finished)
#'   t1 : Finished at Tue Jul 07 16:54:59 CEST 2015
#'   t2 : Finished at Tue Jul 07 16:55:00 CEST 2015
#'   t3 : Finished at Tue Jul 07 16:54:55 CEST 2015
#'   t4 : Finished at Tue Jul 07 16:55:03 CEST 2015
#'   t5 : Finished at Tue Jul 07 16:54:56 CEST 2015
#' }
#' @export
PAGetResult <- function(job.id, 
                        client = PAClient()) {
  if (client == NULL || is.jnull(client) ) {
    stop("You are not currently connected to the scheduler, use PAConnect")
  } 
  
  
  job.state <- j_try_catch({
    return (J(client, "getJobState", toString(job.id)))
  })
  task.states <- job.state$getTasks()
  
  rjob <- PAJob(job.state$getName(),"ProActive R Job") 
  for (i in 1:task.states$size()) {
    task.state <- task.states$get(as.integer(i-1))
    name <- task.state$getName()
    rtask <- PATask(name=name)
    addTask(rjob) <- rtask
  }
  
  task.names <- sapply(task.states, function(task.state) {
    return (task.state$getName())
  })
  
  rjob.result <- PAJobResult(rjob, toString(job.id), task.names, client)
  return (rjob.result)
}