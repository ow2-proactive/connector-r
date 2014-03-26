def srcDir = new File(args[0])
def destDir = new File(args[1])
 
assert run([rExe(), 'CMD', 'build', srcDir.getAbsolutePath()], env(), destDir).waitFor() == 0 : 'Error: R build command failed.'
 
def env() {
  def jreHome = System.getenv()['JAVA_HOME'] + File.separator + 'jre'
  def newEnv = []
  System.getenv().each() { k,v -> 
    if ('JAVA_HOME'.equals(k)) { v = jreHome } 
    newEnv << k+'='+v
  }
  return newEnv
}

def run(command, env, dir) {
  println "Execute command: " + command.join(" ")
  def proc = command.execute(env, dir)
  proc.waitForProcessOutput(System.out, System.err)
  return proc
}
 
def rExe() {
  def rExe
  if (windows()) {
      def ARCH = System.getenv()['ProgramFiles(x86)'] != null ? 'x64' : 'i386'
    rExe = System.getenv()['R_HOME'] + File.separator +  'bin' + File.separator + ARCH + File.separator + R.exe
  } else if (linux()) {
    rExe = System.getenv()['R_HOME'] + File.separator +  'bin' + File.separator + 'R'
  }
  return rExe
}
 
def linux() {
  return System.properties['os.name'].toLowerCase().contains('linux')
}
 
def windows() {
  return System.properties['os.name'].toLowerCase().contains('windows')
} 
