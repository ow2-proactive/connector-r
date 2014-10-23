
.doSaveFunctionDependencies <- function(funcOrFuncName, envir=NULL, newenvir=new.env(), .visitedNames={}, .libraryDependencies = {}, .do.verbose=PADebug()) {
  if (typeof(funcOrFuncName) == "character") {
    # if in buffer, then already inspected
    if (is.element(funcOrFuncName, .visitedNames)) {
      return(list(NULL,.visitedNames, .libraryDependencies))
    }
    
    if (.do.verbose) {
      print(str_c(" // processing variable: '", funcOrFuncName,"'"))
    }
    
    # reserved functions
    if (is.element(funcOrFuncName, c("install.packages","library","require", "set_progress"))) {
      return(list(NULL,.visitedNames, .libraryDependencies))
    }
    
    answerToFind <- .findName(funcOrFuncName, envir = envir, .listpackages = .libraryDependencies, newenvir=newenvir, .do.verbose = .do.verbose)
    .visitedNames <- append( .visitedNames, funcOrFuncName)    
    if (is.null(answerToFind)) {
      return(list(NULL, .visitedNames, .libraryDependencies))
    }
    .libraryDependencies <- answerToFind[["listpackages"]]
    if (!answerToFind[["continue"]]) {
      return(list(NULL, .visitedNames, .libraryDependencies))
    }
    out <- {funcOrFuncName};
    func <- answerToFind[["object"]]
    
  } else {
    if (.do.verbose) {
      print(str_c(" // processing closure"))
    }
    func <- funcOrFuncName;
    out <- {};
  }
  
  if (is.null(envir)) {
    envir <- environment(func)
  }
  globs <- findGlobals(func)
  if (.do.verbose) {
    print(str_c(" // // found : ", toString(globs)))
  }
  
  
  for (varName in globs) {
    if (!is.element(varName, c("install.packages","library","require", "set_progress", "inputspace","outputspace","globalspace","userspace"))) {
      answerToFind <- .findName(varName, envir = envir, .listpackages = .libraryDependencies, newenvir=newenvir, .do.verbose = .do.verbose)
      
      if (!is.null(answerToFind)) {
        .visitedNames <- append( .visitedNames, funcOrFuncName)
        .libraryDependencies <- answerToFind[["listpackages"]]
        if (!answerToFind[["continue"]]) {
          next
        }
        var <- answerToFind[["object"]]
        envirvar <- environment(var)
        tovar <- typeof(var);
        if (tovar == "closure") {
          outsubpair <- .doSaveFunctionDependencies(varName, envir = envirvar, newenvir = newenvir, .visitedNames = .visitedNames , .libraryDependencies = .libraryDependencies, .do.verbose = .do.verbose)
          out <- union(out, outsubpair[[1]]);
          .visitedNames <- union(.visitedNames, outsubpair[[2]]);
          .libraryDependencies <- union(.libraryDependencies, outsubpair[[3]])
          
        } else if (tovar == "list") {
          out <- union(out, varName); # adding the function variable in the list to the output
          outsubpair <- .doSaveListDependencies(varName, envir = envirvar, newenvir = newenvir, .visitedNames = .visitedNames, .libraryDependencies = .libraryDependencies , .do.verbose = .do.verbose) # adding the dependencies of this variable
          out <- union(out, outsubpair[[1]]);
          .visitedNames <- union(.visitedNames, outsubpair[[2]]);  # merging the already parsed functions
          .libraryDependencies <- union(.libraryDependencies, outsubpair[[3]])
          
        } else if (is.element(tovar,c("symbol","logical","integer", "double", "complex", "character","list","raw"))) {
          out <- union(out, varName)
        }
      }
    }
  }
  return(list(out,.visitedNames,.libraryDependencies))
};

.findName <- function(name, envir = NULL, .listpackages = NULL, newenvir, .do.verbose = FALSE) {
  # first look for the object in the environment passed in parameter
  answerList <- NULL
  #   answerList <- tryCatch( {
  #     object <- get(name, envir)
  #     if (.do.verbose) {
  #       print(str_c(" // variable '", name,"' added to environment"))
  #     }
  #     assign(name, object, envir=newenvir);
  #     return(list(object = object, continue = TRUE, listpackages = .listpackages))
  #   }, error = function(e) {return(NULL)} );
  
  # then look anywhere
  if (is.null(answerList)) {
    answerList <- tryCatch( {
      anywhereList <- getAnywhere(name)
      if (!is.null(anywhereList) && (length(anywhereList[["objs"]]) > 0)) {
        aL <- .appendLibraryDependencies(anywhereList[["where"]][[1]], .listpackages = .listpackages, .do.verbose = .do.verbose)
        return(list(object = anywhereList[["objs"]][[1]], continue = aL[["continue"]], listpackages = aL[["listpackages"]]))
      }
      NULL;
    }, error = function(e) {return(NULL)} );
  }
  if (is.null(answerList)) {
    warning(str_c("Could not find object \"",name,"\"")) 
  }
  return(answerList);
}

.appendLibraryDependencies <- function(packageName, .listpackages, .do.verbose) {
  packageName <- str_replace(str_replace(packageName, fixed("namespace:"), ""), fixed("package:"), "")
  if (!is.element(packageName, c("",".Primitive", "base", "datasets", "graphics", "methods", "stats", "utils", "R_GlobalEnv", ".GlobalEnv"))) {
    # custom packages, stop recursion
    .listpackages <- union(.listpackages, packageName);
    if (.do.verbose) {
      print(str_c("Package ", packageName, " is required"))
    }
    return(list( continue=FALSE, listpackages = .listpackages))
  } else if (is.element(packageName, c("","R_GlobalEnv", ".GlobalEnv"))) {
    # user defined variables, continue recursion
    return(list( continue=TRUE, listpackages=.listpackages))
  } else {
    # standard packages, stop recursion
    return(list( continue=FALSE, listpackages=.listpackages))
  }
}

.doSaveListDependencies <- function(lstvarName, envir=NULL, newenvir=new.env(), .visitedNames={}, .libraryDependencies={}, .do.verbose=PADebug()) {
  answerToFind <- .findName(lstvarName, envir = envir, .listpackages = .libraryDependencies, newenvir=newenvir, .do.verbose = .do.verbose)
  
  if (!is.null(answerToFind)) {
    .visitedNames <- append( .visitedNames, funcOrFuncName)
    .libraryDependencies <- answerToFind[["listpackages"]]
    if (!answerToFind[["continue"]]) {
      next
    }
    lstvar <- answerToFind[["object"]]
    out <- {};
    for(el in lstvar) {
      toelem = typeof(el);
      if (toelem == "list") {
        outsubpair <- .doSaveListDependencies(el,envir=envir, .visitedNames=.visitedNames , .libraryDependencies=.libraryDependencies, .do.verbose=.do.verbose)
        out <- union(out, outsubpair[[1]]);
        .visitedNames <- union(.visitedNames, outsubpair[[2]]);
      } else if (tovar == "closure") {
        outsubpair <- .doSaveFunctionDependencies(el,envir=envir, .visitedNames=.visitedNames, .libraryDependencies=.libraryDependencies, .do.verbose=.do.verbose)
        out <- union(out, outsubpair[[1]]);
        .visitedNames <- union(.visitedNames, outsubpair[[2]]);
      }
    }
  }
  return(list(out,.visitedNames, .libraryDependencies))
};

.PASolve_saveDependencies <- function(subpair,newenvir,file) {
  
  # print(str_c("saving ",file))
  save(list = subpair,file = file, envir = newenvir);
  # print(str_c(file," saved"))
};

.PASolve_computeDependencies <- function(funcOrFunName, envir = environment(), variableNames = NULL, newenvir = new.env(), .do.verbose=PADebug()) {
  if (typeof(funcOrFunName) == "character") {
    func <- get(funcOrFunName,envir)
    assign(funcOrFunName, func, envir = newenvir)
  }
  subpair <- .doSaveFunctionDependencies(funcOrFunName, envir = envir, newenvir = newenvir, .visitedNames = variableNames, .libraryDependencies={},.do.verbose=.do.verbose)
  return(list(variableNames = c(variableNames,subpair[[1]]), newenvir = newenvir, libraryDependencies=subpair[[3]]))
}