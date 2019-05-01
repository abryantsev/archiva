package org.apache.archiva.upload;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.archiva.redback.rest.services.AbstractRestServicesTest;
import org.apache.archiva.rest.api.services.ArchivaRestServiceException;
import org.apache.archiva.test.utils.ArchivaBlockJUnit4ClassRunner;
import org.apache.archiva.web.api.FileUploadService;
import org.apache.archiva.web.model.FileMetadata;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.AttachmentBuilder;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author Olivier Lamy
 */
@RunWith( ArchivaBlockJUnit4ClassRunner.class )
public class UploadArtifactsTest
    extends AbstractRestServicesTest
{


    @Override
    protected String getSpringConfigLocation( )
    {
        return "classpath*:META-INF/spring-context.xml,classpath:/spring-context-test-upload.xml";
    }

    @Override
    protected String getRestServicesPath( )
    {
        return "restServices";
    }

    protected String getBaseUrl( )
    {
        String baseUrlSysProps = System.getProperty( "archiva.baseRestUrl" );
        return StringUtils.isBlank( baseUrlSysProps ) ? "http://localhost:" + getServerPort() : baseUrlSysProps;
    }

    private FileUploadService getUploadService( )
    {
        FileUploadService service =
            JAXRSClientFactory.create( getBaseUrl( ) + "/" + getRestServicesPath( ) + "/archivaUiServices/",
                FileUploadService.class,
                Collections.singletonList( new JacksonJaxbJsonProvider( ) ) );
        log.debug( "Service class {}", service.getClass( ).getName( ) );
        WebClient.client( service ).header( "Authorization", authorizationHeader );
        WebClient.client( service ).header( "Referer", "http://localhost:" + getServerPort() );

        WebClient.client( service ).header( "Referer", "http://localhost" );
        WebClient.getConfig( service ).getRequestContext( ).put( Message.MAINTAIN_SESSION, true );
        WebClient.getConfig( service).getRequestContext().put(Message.EXCEPTION_MESSAGE_CAUSE_ENABLED, true);
        WebClient.getConfig( service).getRequestContext().put(Message.FAULT_STACKTRACE_ENABLED, true);
        WebClient.getConfig( service).getRequestContext().put(Message.PROPOGATE_EXCEPTION, true);
        WebClient.getConfig( service).getRequestContext().put("org.apache.cxf.transport.no_io_exceptions", true);

        // WebClient.client( service ).
        return service;
    }

    @Test
    public void clearUploadedFiles( )
        throws Exception
    {
        FileUploadService service = getUploadService( );
        service.clearUploadedFiles( );
    }

    @Test
    public void uploadFile( ) throws IOException, ArchivaRestServiceException
    {
        FileUploadService service = getUploadService( );
        try
        {
            Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );
            final Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"" + file.getFileName( ).toString( ) + "\"; name=\"files[]\"" ) ).build( );
            MultipartBody body = new MultipartBody( fileAttachment );
            service.post( body );
        }
        finally
        {
            service.clearUploadedFiles( );
        }
    }

    @Test
    public void failUploadFileWithBadFileName( ) throws IOException, ArchivaRestServiceException
    {
        FileUploadService service = getUploadService( );
        try
        {
            Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );
            final Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"/../TestFile.testext\"; name=\"files[]\"" ) ).build( );
            MultipartBody body = new MultipartBody( fileAttachment );
            try
            {
                service.post( body );
                fail( "FileNames with path contents should not be allowed." );
            }
            catch ( ArchivaRestServiceException e )
            {
                // OK
            }
        }
        finally
        {
            service.clearUploadedFiles( );
        }
    }

    @Test
    public void uploadAndDeleteFile( ) throws IOException, ArchivaRestServiceException
    {
        FileUploadService service = getUploadService( );
        try
        {
            Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );
            final Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"" + file.getFileName( ).toString( ) + "\"; name=\"files[]\"" ) ).build( );
            MultipartBody body = new MultipartBody( fileAttachment );
            service.post( body );
            service.deleteFile( file.getFileName( ).toString( ) );
        }
        finally
        {
            service.clearUploadedFiles( );
        }
    }

    @Test
    public void failUploadAndDeleteWrongFile( ) throws IOException, ArchivaRestServiceException
    {
        FileUploadService service = getUploadService( );
        try
        {
            Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );
            final Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"" + file.getFileName( ).toString( ) + "\"; name=\"files[]\"" ) ).build( );
            MultipartBody body = new MultipartBody( fileAttachment );
            service.post( body );
            assertFalse( service.deleteFile( "file123" + file.getFileName( ).toString( ) ) );
        }
        finally
        {
            service.clearUploadedFiles( );
        }
    }

    @Test
    public void failUploadAndDeleteFileInOtherDir( ) throws IOException, ArchivaRestServiceException
    {
        Path testFile = null;
        try
        {
            FileUploadService service = getUploadService( );
            Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );
            Path targetDir = Paths.get( "target/testDelete" ).toAbsolutePath( );
            if ( !Files.exists( targetDir ) ) Files.createDirectories( targetDir );
            Path tempDir = SystemUtils.getJavaIoTmpDir( ).toPath( );
            testFile = Files.createTempFile( targetDir, "TestFile", ".txt" );
            log.debug( "Test file {}", testFile.toAbsolutePath( ) );
            log.debug( "Tmp dir {}", tempDir.toAbsolutePath( ) );
            assertTrue( Files.exists( testFile ) );
            Path relativePath = tempDir.relativize( testFile.toAbsolutePath( ) );
            final Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"" + file.getFileName( ).toString( ) + "\"; name=\"files[]\"" ) ).build( );
            MultipartBody body = new MultipartBody( fileAttachment );
            service.post( body );
            String relativePathEncoded = URLEncoder.encode( "../target/" + relativePath.toString( ), "UTF-8" );
            log.debug( "Trying to delete with path traversal: {}, {}", relativePath, relativePathEncoded );
            try
            {
                service.deleteFile( relativePathEncoded );
            }
            catch ( ArchivaRestServiceException ex )
            {
                // Expected exception
            }
            assertTrue( "File in another directory may not be deleted", Files.exists( testFile ) );
        }
        finally
        {
            if ( testFile != null )
            {
                Files.deleteIfExists( testFile );
            }
        }
    }

    @Test
    public void failSaveFileWithBadParams( ) throws IOException, ArchivaRestServiceException
    {
        FileUploadService service = getUploadService( );
        Path targetFile = Paths.get( "target/test/test-testSave.4" );
        Path targetPom = Paths.get( "target/test/test-testSave.pom" );
        try
        {
            Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );

            Path targetDir = Paths.get( "target/appserver-base/test/testSave" ).toAbsolutePath( );
            Path repoDir = Paths.get("target/appserver-base/repositories/internal/org");
            log.info("Repo dir {}", repoDir.toAbsolutePath());
            if (!Files.exists(repoDir)) Files.createDirectories(repoDir);
            assertTrue(Files.exists(repoDir));
            if ( !Files.exists( targetDir ) ) Files.createDirectories( targetDir );
            Files.deleteIfExists( targetFile );
            Files.deleteIfExists( targetPom );
            Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"archiva-model-1.2.jar\"; name=\"files[]\"" ) ).build( );
            MultipartBody body = new MultipartBody( fileAttachment );
            FileMetadata meta = service.post( body );
            log.debug( "Metadata {}", meta.toString( ) );
            assertTrue( service.save( "internal", "org.archiva", "archiva-model", "1.2", "jar", true ) );

            fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"/../TestFile.FileExt\"; name=\"files[]\"" ) ).build( );
            body = new MultipartBody( fileAttachment );
            meta = service.post( body );
            log.debug( "Metadata {}", meta.toString( ) );
            try {
                service.save("internal", "org", URLEncoder.encode("../../../test", "UTF-8"), URLEncoder.encode("testSave", "UTF-8"), "4", true);
                fail("Error expected, if the content contains bad characters.");
            } catch (ArchivaRestServiceException e) {
                // OK
            }
            assertFalse( Files.exists( Paths.get( "target/test-testSave.4" ) ) );
        }
        finally
        {
            // service.clearUploadedFiles( );
            Files.deleteIfExists( targetFile );
            Files.deleteIfExists( targetPom );
        }
    }

    @Test
    public void saveFile( ) throws IOException, ArchivaRestServiceException
    {

        Path path = Paths.get("target/appserver-base/repositories/internal/data/repositories/internal/org/apache/archiva/archiva-model/1.2/archiva-model-1.2.jar");
        Files.deleteIfExists( path );
        FileUploadService service = getUploadService( );
        Path file = Paths.get( "src/test/repositories/snapshot-repo/org/apache/archiva/archiva-model/1.4-M4-SNAPSHOT/archiva-model-1.4-M4-20130425.081822-1.jar" );
        final Attachment fileAttachment = new AttachmentBuilder( ).object( Files.newInputStream( file ) ).contentDisposition( new ContentDisposition( "form-data; filename=\"archiva-model.jar\"; name=\"files[]\"" ) ).build( );
        MultipartBody body = new MultipartBody( fileAttachment );
        service.post( body );
        service.save( "internal", "org.apache.archiva", "archiva-model", "1.2", "jar", true );
    }
}