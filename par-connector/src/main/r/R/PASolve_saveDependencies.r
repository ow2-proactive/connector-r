
# Toplevel function used to analyse the dependencies, it will start the recursion and return :
# - the list of dependant variable names
# - an environment containing all variables definition
# - the list of R packages that are used by the hierarchy
.PASolve_computeDependencies <- function(funcOrFunName, envir = environment(), variableNames = NULL, newenvir = new.env(), .do.verbose=PADebug()) {

  recurseResult <- .recurseSaveFunctionDependencies(funcOrFunName, envir = envir, newenvir = newenvir, .visitedNames = variableNames, .libraryDependencies={},.do.verbose=.do.verbose, level = 1)
  if (.do.verbose) {
    cat("The environment remotely sent will contain the following variables : \n")
    print(ls(recurseResult[["newenvir"]], all.names = TRUE))
    cat("The following librairies will be loaded : \n")
    print(recurseResult[["libs"]])
  }
  return(list(variableNames = c(variableNames,recurseResult[["deps"]]), newenvir = recurseResult[["newenvir"]], libraryDependencies=recurseResult[["libs"]]))
}

# internal recursive function to analyse dependencies. 
# It will :
#  - take the function parameter as either a character name or a lambda
#  - find all names used in a given function, inside a given environment (workspace)
#  - use an accumulator to store the functions already inspected (.visitedNames)
#  - use an accumulator to store all R packages used (.libraryDependencies)
#  - produce a new environment which will store all the necessary functions and variable definitions
#  - return the name of the function if it must be stored in dependencies
.recurseSaveFunctionDependencies <- function(funcOrFuncName, envir=NULL, newenvir=new.env(), .visitedNames={}, .libraryDependencies = {}, .do.verbose=PADebug(), level = 1) {
  if (typeof(funcOrFuncName) == "character") {
    # function defined by it's name
    
    if (is.element(funcOrFuncName, .visitedNames)) {
      # if in buffer, then already inspected, return the current accumulators
      return(list(deps = NULL, visited = .visitedNames, libs = .libraryDependencies, newenvir = newenvir))
    }
    
    .visitedNames <- union( .visitedNames, funcOrFuncName)
    
    .print.debug.message(.do.verbose, str_c("processing function: '", funcOrFuncName,"'"), level)
    
    # if it's a reserved function, end recursion
    if (is.element(funcOrFuncName, c("install.packages","library","require", "set_progress"))) {
      return(list(deps = NULL, visited = .visitedNames, libs = .libraryDependencies, newenvir = newenvir))
    }
    
    # find the function definition 
    answerToFind <- .findAndStoreName(funcOrFuncName, envir = envir, .listpackages = .libraryDependencies, newenvir=newenvir, .do.verbose = .do.verbose, level = level)

    if (is.null(answerToFind)) {
      # function not found, end recursion
      return(list(deps = NULL, visited = .visitedNames, libs = .libraryDependencies, newenvir = newenvir))
    }
    .libraryDependencies <- answerToFind[["listpackages"]]
    
    if (!answerToFind[["continue"]]) {
      # function inside a package, end recursion
      return(list(deps = NULL, visited = .visitedNames, libs = .libraryDependencies, newenvir = newenvir))
    }
    # ok, function is a dependency, return it, and store it
    computedDependencies <- {funcOrFuncName};
    functionDefinition <- answerToFind[["object"]]
    
  } else {
    .print.debug.message(.do.verbose, "processing closure variable", level)

    functionDefinition <- funcOrFuncName;
    computedDependencies <- {};
  }
  
  # if no environment is provided, use R to grab the function environment
  if (is.null(envir)) {
    envir <- environment(functionDefinition)
  }
  # find all globals (i.e. variables, functions) defined in this environment
  globs <- findGlobals(functionDefinition)

  .print.debug.message(.do.verbose, str_c(" --> found globals : ", toString(globs)), level)
  
  for (varName in globs) {
    # skip all globals related to proactive or library loading
    if (!is.element(varName, c("install.packages","library","require", "set_progress", "inputspace","outputspace","globalspace","userspace"))) {
      # analyse this global
      answerToFind <- .findAndStoreName(varName, envir = envir, .listpackages = .libraryDependencies, newenvir=newenvir, .do.verbose = .do.verbose, level = level)
      
      if (!is.null(answerToFind)) {
        newenvir <- answerToFind[["newenvir"]]
        .libraryDependencies <- answerToFind[["listpackages"]]
        if (!answerToFind[["continue"]]) {
          next
        }        
        
        varDefinition <- answerToFind[["object"]] 
        envirVar <- environment(varDefinition)
        typeofVar <- typeof(varDefinition);
        if (typeofVar == "closure") {
          # if the variable is a "closure" (function), then recurse
          recurseResult <- .recurseSaveFunctionDependencies(varName, envir = envirVar, newenvir = newenvir, .visitedNames = .visitedNames , .libraryDependencies = .libraryDependencies, .do.verbose = .do.verbose, level = level + 1)
          computedDependencies <- union(computedDependencies, recurseResult[["deps"]]);
          .visitedNames <- union(.visitedNames, recurseResult[["visited"]]);
          .libraryDependencies <- union(.libraryDependencies, recurseResult[["libs"]])
          newenvir <- recurseResult[["newenvir"]]
          
        } else if (typeofVar == "list") {
          # if the variable is a list, then recurse
          computedDependencies <- union(computedDependencies, varName); # adding the list variable name to the output
          recurseResult <- .recurseSaveListDependencies(varName, envir = envirVar, newenvir = newenvir, .visitedNames = .visitedNames, .libraryDependencies = .libraryDependencies , .do.verbose = .do.verbose, level = level + 1)
          computedDependencies <- union(computedDependencies, recurseResult[["deps"]]); # adding the dependencies of the list
          .visitedNames <- union(.visitedNames, recurseResult[["visited"]]);  # merging the already parsed functions
          .libraryDependencies <- union(.libraryDependencies, recurseResult[["libs"]])
          newenvir <- recurseResult[["newenvir"]]
          
        } else if (is.element(typeofVar,c("symbol","logical","integer", "double", "complex", "character","raw"))) {
          # if the variable is a terminal type, return it
          computedDependencies <- union(computedDependencies, varName)
        }
      }
    }
  }
  return(list(deps = computedDependencies, visited = .visitedNames, libs = .libraryDependencies, newenvir = newenvir))
};

.print.debug.message <- function (.do.verbose, message, level) {
  if (.do.verbose) {
    line <- str_dup("// ", level)
    cat(str_c(line,message,"\n"))
  }
}

# search the given name anywhere, the name can be a function name or a variable name
# it returns the object found, recursion continuation decision and a new list of package dependences
# it also stores the name in the new environment if applicable (it is the better place to do this decision)
.findAndStoreName <- function(name, envir = NULL, .listpackages = NULL, newenvir = new.env(), .do.verbose = FALSE, level = 1) {

  .print.debug.message(.do.verbose, str_c("finding name : ", name), level)

  answerList <- NULL
  # this code has been commented out as it was too unstable, now we rely on R function getAnywhere systematically
  
  # first look for the object in the environment passed in parameter
  #   answerList <- tryCatch( {
  #     object <- get(name, envir)
  #     if (.do.verbose) {
  #       print(str_c(" // variable '", name,"' added to environment"))
  #     }
  #     assign(name, object, envir=newenvir);
  #     return(list(object = object, continue = TRUE, listpackages = .listpackages))
  #   }, error = function(e) {return(NULL)} );
  
  # look anywhere
  if (is.null(answerList)) {
    answerList <- tryCatch( {
      anywhereList <- getAnywhere(name)
      
      if (!is.null(anywhereList) && (length(anywhereList[["objs"]]) > 0)) {
        .print.debug.message(.do.verbose, str_c("--> found in :",str_c(anywhereList[["where"]],collapse=' ')), level)
        aL <- .appendLibraryDependencies(anywhereList[["where"]][[1]], .listpackages = .listpackages, .do.verbose = .do.verbose, level = level)
        if (aL[["continue"]]) {
           assign(name, anywhereList[["objs"]][[1]], envir = newenvir)
          .print.debug.message(.do.verbose, str_c("variable ", name, " (",typeof(anywhereList[["objs"]][[1]]), ") stored."), level)
          if (.do.verbose) {
            print(ls(newenvir, all.names = TRUE))
          }
          
        }
        
        return(list(object = anywhereList[["objs"]][[1]], continue = aL[["continue"]], listpackages = aL[["listpackages"]], newenvir = newenvir))
      }
      NULL;
    }, error = function(e) {return(NULL)} );
  }
  if (is.null(answerList)) {
    warning(str_c("Could not find object \"",name,"\""))
  }
  return(answerList);
}

# this function analyse a namespace to decide wether it is associated with a R package which should be included in dependencies
# depending of the namespace type (package, global environment, local environment), the enclosing recursion should continue or not 
# it returns recursion continuation decision and a new list of package dependences
.appendLibraryDependencies <- function(namespace, .listpackages, .do.verbose, level) {
  # remove the namespace prefix to get the package name
  packageName <- str_replace(str_replace(namespace, fixed("namespace:"), ""), fixed("package:"), "")
  if (!is.element(packageName, c("",".Primitive", "base", "datasets", "graphics", "methods", "stats", "utils", "R_GlobalEnv", ".GlobalEnv"))) {
    # custom packages, stop recursion
    .listpackages <- union(.listpackages, packageName);
    .print.debug.message(.do.verbose, str_c("Package ", packageName, " is required"), level)
    
    return(list( continue=FALSE, listpackages = .listpackages))
  } else if (is.element(packageName, c("","R_GlobalEnv", ".GlobalEnv"))) {
    # user-defined variables, continue recursion, store the variable
    return(list( continue=TRUE, listpackages=.listpackages))
  } else {
    # standard packages, stop recursion
    return(list( continue=FALSE, listpackages=.listpackages))
  }
}

# similarly to .recurseSaveFunctionDependencies, this function recurse on the content of a list
# which can contain other lists or even functions
.recurseSaveListDependencies <- function(lstvarName, envir=NULL, newenvir=new.env(), .visitedNames={}, .libraryDependencies={}, .do.verbose=PADebug(), level = 1) {
  .print.debug.message(.do.verbose, str_c("processing list: '", lstvarName,"'"), level)
  answerToFind <- .findAndStoreName(lstvarName, envir = envir, .listpackages = .libraryDependencies, newenvir=newenvir, .do.verbose = .do.verbose, level)
  
  if (!is.null(answerToFind)) {
    newenvir <- answerToFind[["newenvir"]]
    .visitedNames <- union( .visitedNames, lstvarName)
    .libraryDependencies <- answerToFind[["listpackages"]]
    if (!answerToFind[["continue"]]) {
      next
    }
    variableDefinition <- answerToFind[["object"]]
    computedDependencies <- {};
    for(element in variableDefinition) {
      typeofElement = typeof(element);
      if (typeofElement == "list") {
        # if the name is a list, recurse
        recurseResult <- .recurseSaveListDependencies(element,envir=envir, newenvir = newenvir, .visitedNames=.visitedNames , .libraryDependencies=.libraryDependencies, .do.verbose=.do.verbose, level = level + 1)
        computedDependencies <- union(computedDependencies, recurseResult[["deps"]]);
        .visitedNames <- union(.visitedNames, recurseResult[["visited"]]);
        .libraryDependencies <- union(.libraryDependencies, recurseResult[["libs"]])
        newenvir <- recurseResult[["newenvir"]]
      } else if (typeofElement == "closure") {
        # if the name is a function, recurse
        recurseResult <- .recurseSaveFunctionDependencies(element,envir=envir, newenvir = newenvir, .visitedNames=.visitedNames, .libraryDependencies=.libraryDependencies, .do.verbose=.do.verbose, level = level + 1)
        computedDependencies <- union(computedDependencies, recurseResult[["deps"]]);
        .visitedNames <- union(.visitedNames, recurseResult[["visited"]]);
        .libraryDependencies <- union(.libraryDependencies, recurseResult[["libs"]])
        newenvir <- recurseResult[["newenvir"]]
      }
      # any other type will be "inside" the list, no need to analyse it
    }
  }
  return(list(deps = computedDependencies, visited = .visitedNames, libs = .libraryDependencies, newenvir = newenvir))
};

