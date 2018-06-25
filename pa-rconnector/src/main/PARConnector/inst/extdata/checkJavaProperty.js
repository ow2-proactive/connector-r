load("nashorn:mozilla_compat.js");

importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(java.exception);
importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);




var propertyName = args[0];
var expectedValue = args[1];

/* Check if java property 'a.jvm.property' value is 'toto' */
if (SelectionUtils.checkJavaProperty(args[0], args[1]) )
{
    selected = true;
    print(args[0] + " = " + args[1] + " ==> selected");
}
else
{
    selected = false;
    print(args[0] + " <> " + args[1] + " ==> not selected");
}