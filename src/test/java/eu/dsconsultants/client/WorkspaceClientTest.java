package eu.dsconsultants.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class WorkspaceClientTest {

    private static final String USER = "";
    private static final String PASSWORD = "";
    private static final String IFC_FILE_NAME = "file.ifc";
    private CloseableHttpClient client;
    private HttpPost post;
    private CloseableHttpResponse response;

    @Before
    public final void before() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(USER, PASSWORD);
        provider.setCredentials(AuthScope.ANY, credentials);
        client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        post = new HttpPost("http://localhost:8080/etc/pages/workspace/jcr:content.rest/bimer-workspace-bundle.workspaceUploadFile");

    }

    @After
    public final void after() throws IllegalStateException, IOException {
        post.completed();
        client.close();
        response.close();
    }

    @Test
    public final void uploadIfcModel() throws IOException {
        final File file = getFile();
        final FileBody ifcFile = new FileBody(file, ContentType.DEFAULT_BINARY);
        final StringBody path = new StringBody("/upload/endpoint/test", ContentType.MULTIPART_FORM_DATA);
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", ifcFile);
        builder.addPart("path", path);
        final HttpEntity entity = builder.build();
        post.setEntity(entity);
        response = client.execute(post);
        final int statusCode = response.getStatusLine()
                .getStatusCode();
        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
    }

    private File getFile() {
        final URL url = Thread.currentThread()
                .getContextClassLoader()
                .getResource("ifc/" + IFC_FILE_NAME);
        return new File(url.getPath());
    }
}
