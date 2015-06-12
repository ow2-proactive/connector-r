### Common

setGeneric(
  name="getName",
  def=function(object,value) {standardGeneric("getName")}  
)

setGeneric(
  name="setName",
  def=function(object,value) {standardGeneric("setName")}  
) 

setGeneric(
  name="getJavaObject",
  def=function(object,value) {standardGeneric("getJavaObject" )}  
)

setGeneric(
  name="getDefinition",
  def=function(object,value) {standardGeneric("getDefinition")}  
)
setGeneric(
  name="setDefinition",
  def=function(object,value) {standardGeneric("setDefinition")}  
)

### PAJob

setGeneric(
  name="getProjectName",
  def=function(object,value) {standardGeneric("getProjectName")}  
)
setGeneric(
  name="setProjectName",
  def=function(object,value) {standardGeneric("setProjectName")}  
) 
setGeneric(
  name="getPriority",
  def=function(object,value) {standardGeneric("getPriority")}  
)
setGeneric(
  name="setPriority",
  def=function(object,value) {standardGeneric("setPriority")}  
) 
setGeneric(
  name="setCancelJobOnError",
  def=function(object,value) {standardGeneric("setCancelJobOnError")}  
) 
setGeneric(
  name="isCancelJobOnError",
  def=function(object,value) {standardGeneric("isCancelJobOnError")}  
) 

setGeneric(
  name="addTask<-",
  def=function(object,value) {standardGeneric("addTask<-" )}  
)


### PATask

setGeneric(
  name="getScript",
  def=function(object) {standardGeneric("getScript")}  
)

setGeneric(
  name="setScript",
  def=function(object,value) {standardGeneric("setScript")}  
) 

setGeneric(
  name="getDescription",
  def=function(object,value) {standardGeneric("getDescription" )}  
) 

setGeneric(
  name="setDescription",
  def=function(object,value) {standardGeneric("setDescription" )}  
) 

setGeneric(
  name="getQuoteExp",
  def=function(object) {standardGeneric("getQuoteExp")}  
)
setGeneric(
  name="getFileIndex",
  def=function(object) {standardGeneric("getFileIndex")}  
)


setGeneric(
  name="getInputFiles",
  def=function(object,value) {standardGeneric("getInputFiles" )}  
)

setGeneric(
  name="getOutputFiles",
  def=function(object,value) {standardGeneric("getOutputFiles" )}  
)

setGeneric(
  name="getSelectionScripts",
  def=function(object,value) {standardGeneric("getSelectionScripts" )}  
)

setGeneric(
  name="getPreScript",
  def=function(object,value) {standardGeneric("getPreScript" )}  
)

setGeneric(
  name="setPreScript",
  def=function(object,value) {standardGeneric("setPreScript" )}  
)

setGeneric(
  name="getPostScript",
  def=function(object,value) {standardGeneric("getPostScript" )}  
)

setGeneric(
  name="setPostScript",
  def=function(object,value) {standardGeneric("setPostScript" )}  
)

setGeneric(
  name="getCleanScript",
  def=function(object,value) {standardGeneric("getCleanScript" )}  
)

setGeneric(
  name="setCleanScript",
  def=function(object,value) {standardGeneric("setCleanScript" )}  
)

setGeneric(
  name="addDependency<-",
  def=function(object,value) {standardGeneric("addDependency<-" )}  
) 
setGeneric(
  name="getDependencies",
  def=function(object) {standardGeneric("getDependencies" )}  
) 

setGeneric(
  name="addInputFiles<-",
  def=function(object,value) {standardGeneric("addInputFiles<-" )}  
)

setGeneric(
  name="addOutputFiles<-",
  def=function(object,value) {standardGeneric("addOutputFiles<-" )}  
)

setGeneric(
  name="addSelectionScript",
  def=function(object,value,engine,is.dynamic) {standardGeneric("addSelectionScript" )}  
)

### PAJobResult



#' Waits for all results controlled by  a PAJobResult object
#'
#' PAWaitFor is used on a \code{\link{PAJobResult-class}} object to block the R interpreter until all results are available.
#' The R result objects will be then returned inside a list. 
#' It is possible to wait for a subset instead of the whole list by using subscript indexing : PAWaitFor(res[1:3]) will wait for only the results at index 1,2,3.
#'
#' @param paresult a PAJobResult object
#' @param timeout a long value specifying an optional timeout in milisecond
#' @param client connection handle to the scheduler, if not provided the handle created by the last call to \code{\link{PAConnect}} will be used  (internal)
#' @param callback a single parameter function which can be called when results are received. It can be useful to udapte graphical user interfaces for examples. Default to NULL.
#' @param ... Additional argument list which are not used
#' 
#' @return A list of results
#' @rdname PAWaitFor
#' @seealso \code{\link{PASolve}}, \code{\link{PAJobResult-class}} and \code{\link{PAWaitAny}}
#' @export
setGeneric(
  name="PAWaitFor",
  def=function(paresult = PALastResult(), ...) {standardGeneric("PAWaitFor" )}  
)


#' Waits for the first available result
#'
#' PAWaitAny is used on a \code{\link{PAJobResult-class}} object to block the R interpreter until the first result is available.
#' The R result object will be then returned as a factor, named by the task name. 
#' If the PAWaitAny is called a second time, then the second result will be waited and returned. After all results are consumed, a call to PAWaitAny will return NA.
#'
#' @param paresult a PAJobResult object
#' @param timeout a long value specifying an optional timeout in milisecond
#' @param client connection handle to the scheduler, if not provided the handle created by the last call to \code{\link{PAConnect}} will be used (internal)
#' @param callback a single parameter function which can be called when results are received. It can be useful to udapte graphical user interfaces for examples. Default to NULL.
#' @param ... Additional argument list which are not used
#' 
#' @return A result
#' @rdname PAWaitAny
#' @seealso \code{\link{PASolve}}, \code{\link{PAJobResult-class}} and \code{\link{PAWaitAny}}
#' @export
setGeneric(
  name="PAWaitAny",
  def=function(paresult = PALastResult(), ...) {standardGeneric("PAWaitAny" )}  
)

### PAFile

setGeneric(
  name="setHash<-",
  def=function(object, value) {standardGeneric("setHash<-")}  
)

setGeneric(
  name="pushFile",
  def=function(object, ...) {standardGeneric("pushFile")}  
)

setGeneric(
  name="pullFile",
  def=function(object, ...) {standardGeneric("pullFile")}  
)

setGeneric(
  name="getMode",
  def=function(object,input) {standardGeneric("getMode")}  
)

setGeneric(
  name="getSelector",
  def=function(object) {standardGeneric("getSelector")}  
)

setGeneric(
  name="isFileTransfer",
  def=function(object) {standardGeneric("isFileTransfer")}  
)


cacheEnv <- new.env()

PAClient <- function(client = NULL) {
  if (exists(".scheduler.client", envir=cacheEnv)){
    .scheduler.client <- get(".scheduler.client", envir=cacheEnv)
  } else {
    .scheduler.client <- NULL
  }
  if (nargs() == 1) {
    .scheduler.client <- client        
  }
  assign(".scheduler.client", .scheduler.client, envir=cacheEnv)
  return(.scheduler.client)
}

#' sets PARConnector Debug mode
#'
#' PADebug can be used either to set the Debug mode to on/off or to know the current state of the debug mode.
#' 
#' In Debug mode a lot of verbose information will be printed (detailed content of PATask created, code analysis debugging, remote execution trace)
#' 
#' @param debug to set the debug mode to on (TRUE) or off (FALSE)
#' @return the current or new state of the debug mode
#' 
#' @export
#' @rdname PADebug
PADebug <- function(debug=FALSE) {  
  if (exists(".is.debug", envir=cacheEnv)){
    .is.debug <- get(".is.debug", envir=cacheEnv)
  } else {
    .is.debug <- FALSE
  }
  
  if (nargs() == 1) {
    .is.debug <- debug        
  }
  assign(".is.debug", .is.debug, envir=cacheEnv)
  return(.is.debug)
}

PAHandler <- function(e, .print.stack=TRUE) {    
  if (.print.stack || PADebug()) {
    if (PADebug()) {
      print("Java Error in :")
      traceback(4)
    }
    e$jobj$printStackTrace()
  }
  stop(e)
}

PALastResult <- function(res = NULL) {    
  if (exists(".last.result", envir=cacheEnv)){
    .last.result <- get(".last.result", envir=cacheEnv)
  } else {
    .last.result <- NULL
  }
  if (nargs() == 1) {
    .last.result <- res        
  }
  assign(".last.result", .last.result, envir=cacheEnv)
  return(.last.result)
}


# returns a growing id used in PASolve
.peekNewSolveId <- function() {  
  # emulating local static variable
  if (exists("pasolve.id", envir=cacheEnv)){
    id <- get("pasolve.id", envir=cacheEnv)
  } else {
    id <- 0
  }   
  id <- id + 1
      
  return(id)
}

.commitNewSolveId <- function() {
  id <- .peekNewSolveId()
  assign("pasolve.id", id, envir=cacheEnv)
  if (exists("space.hash", envir=cacheEnv)){
    remove("space.hash", envir=cacheEnv)
  }
  if (exists("patask.id", envir=cacheEnv)){
    remove("patask.id", envir=cacheEnv)
  }
}

.getNewTaskName <- function() {
  if (exists("patask.id", envir=cacheEnv)){
    id <- get("patask.id", envir=cacheEnv)
  } else {
    id <- 0
  }   
  id <- id + 1
  assign("patask.id", id, envir=cacheEnv)
  
  return(str_c("t",id))
}




# computes a hash based on the hostname & the solve id & a time stamp
# this is to guaranty that files used by a job will be put in a unique folder

.getSpaceHash <- function() {
  
  if (exists("space.hash", envir=cacheEnv)){
    hash <- get("space.hash", envir=cacheEnv)
  } else {
    id <- .peekNewSolveId()
    localhost <- J("java.net.InetAddress")$getLocalHost()
    hname <- localhost$getHostName()
    time <- Sys.time()
    full_str <- str_c(hname, toString(id), time)
    j_str <- .jnew(J("java.lang.String"),full_str)
    hcode <- j_str$hashCode()
    hash <- str_replace_all(toString(hcode),fixed("-"), "_")
    assign("space.hash", hash, envir=cacheEnv)
  }     
  return(hash)
}

j_try_catch <- defmacro(FUN, .print.stack = TRUE, .handler = NULL, expr={
  tryCatch ({
    return (FUN)
  } , Exception = function(e) {
    if (is.null(.handler)) {
      PAHandler(e, .print.stack=.print.stack)
    } else {
      .handler(e, .print.stack=.print.stack)
    }
  })
})

.getSpaceName <- function(space) {
  if (toupper(space) == "INPUT") {
    return ("INPUTSPACE")
  } else if (toupper(space) == "OUTPUT") {
    return ("OUTPUTSPACE")
  } else if (toupper(space) == "GLOBAL") {
    return ("GLOBALSPACE")
  } else if (toupper(space) == "USER") {
    return ("USERSPACE")
  }
  return(space)
}

# macro which escapes parameters to evaluatable strings
.param.to.remote.eval <- defmacro(param, expr = {
  # print(str_c("eval(parse(text = '",deparse(substitute(param)),"'))"))
  .ptre.lines <- deparse(substitute(param))
  .ptre.output <- "eval(parse(text = c("
  for (.ptre.i in 1:length(.ptre.lines)) {
    .ptre.output <- str_c(.ptre.output,"\"",str_replace_all(.ptre.lines[.ptre.i],fixed("\""),"\\\""),"\"")
    if (.ptre.i < length(.ptre.lines)) {
      .ptre.output <- str_c(.ptre.output,", ")
    }
  }
  .ptre.output <- str_c(.ptre.output,")))")
  return(.ptre.output)
})

.cat_list <- function(ll) {
  cat(.toString_list(ll))  
}

.toString_list <- function(ll) {
  output <- t("{ ")
  for (k in 1:length(ll)) {
    output <- str_c(output,toString(ll[[k]])," ")
    if (k < length(ll) ) {        
      output <- str_c(output,",")     
    }
  }
  output <- str_c(output,"}")
  return(output)
}

.enum <- function ( name, ...)
{
  choices <- list( ... )
  names   <- attr(choices,"names")
  
  pos <- pmatch( name, names )
  
  max <- length(names)
  
  if ( any( is.na(pos) ) )
    stop("symbolic value must be chosen from ", list(names) )
  else if ( (max+1) %in% pos )
    pos <- seq_along(names)
  
  id  <- unlist(choices[pos])
  
  if ( length(id) > 1 )
    stop("multiple choices not allowed")
  
  return( id )
}