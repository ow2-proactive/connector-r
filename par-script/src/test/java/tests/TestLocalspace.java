package tests;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.Capability;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileContent;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.api.FileType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.ow2.parscript.PARScriptEngine;
import org.ow2.parscript.PARScriptFactory;
import org.ow2.proactive.scheduler.task.utils.LocalSpaceAdapter;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.junit.Test;


/**
 * Basic PARScript tests.
 *
 * @author Activeeon Team
 */
public class TestLocalspace {

    @Test
    public void test() throws Exception {
        File f = new File(System.getProperty("java.io.tmpdir"));
        DataSpacesFileObject dsfo = new MockedDSFO(f.toURI());

        String rScript = "result=getwd();";

        Map<String, Object> aBindings = Collections.singletonMap(PARScriptEngine.DS_SCRATCH_BINDING_NAME,
                (Object) new LocalSpaceAdapter(dsfo));
        SimpleScript ss = new SimpleScript(rScript, PARScriptFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute(aBindings, System.out, System.err);

        String resPath = (String) res.getResult();
        org.junit.Assert.assertNotNull("No result from R script", resPath);
        org.junit.Assert.assertEquals("R script working directory is incorrect", f.getCanonicalPath(),
                resPath.replace("/", File.separator));
    }

    class MockedDSFO implements DataSpacesFileObject {
        private final URI uri;

        public MockedDSFO(URI uri) {
            this.uri = uri;
        }

        @Override
        public void close() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void copyFrom(DataSpacesFileObject arg0, FileSelector arg1) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void createFile() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void createFolder() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean delete() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(FileSelector arg0) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSpacesFileObject ensureExistingOrSwitch(boolean value) throws FileSystemException,
                SpaceNotFoundException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean exists() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DataSpacesFileObject> findFiles(FileSelector arg0) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DataSpacesFileObject> findFiles(org.apache.commons.vfs2.FileSelector selector) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findFiles(FileSelector arg0, boolean arg1, List<DataSpacesFileObject> arg2)
                throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> getAllRealURIs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> getAllSpaceRootURIs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSpacesFileObject getChild(String arg0) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DataSpacesFileObject> getChildren() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileContent getContent() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSpacesFileObject getParent() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRealURI() {
            return this.uri.toString();
        }

        @Override
        public String getSpaceRootURI() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileType getType() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getVirtualURI() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasSpaceCapability(Capability arg0) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isContentOpen() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isHidden() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isReadable() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWritable() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void moveTo(DataSpacesFileObject arg0) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh() throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSpacesFileObject resolveFile(String arg0) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSpacesFileObject switchToSpaceRoot(String arg0) throws FileSystemException,
                SpaceNotFoundException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBaseName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPath() {
            throw new UnsupportedOperationException();
        }
    }
}
