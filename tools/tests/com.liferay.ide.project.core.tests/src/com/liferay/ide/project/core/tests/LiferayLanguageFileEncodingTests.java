/*******************************************************************************
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************/

package com.liferay.ide.project.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.liferay.ide.core.ILiferayConstants;
import com.liferay.ide.core.LiferayLanguagePropertiesValidator;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.PropertiesUtil;
import com.liferay.ide.core.util.ZipUtil;
import com.liferay.ide.project.core.ProjectRecord;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.sdk.core.SDKManager;
import com.liferay.ide.server.util.ServerUtil;

import java.io.File;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gregory Amerson
 * @author Kuo Zhang
 */
@SuppressWarnings( "restriction" )
public class LiferayLanguageFileEncodingTests extends ProjectCoreBase
{
    /*
     * In order to test the encoding feature, mainly test the markers on the non-default encoding language files,
     * encode them to default then check if the markers are gone. Since the LiferayLanguagePropertiesListener does't
     * work so immediately after the language files get changed, manually invoke the same methods as the listener
     */
    private IRuntime runtime;

    private IRuntime getRuntime() throws Exception
    {
        if( runtime == null )
        {
            runtime = createNewRuntime( "runtime" );

            assertNotNull( runtime );
        }

        return runtime;
    }

    private boolean hasEncodingMarker( IFile file ) throws Exception
    {
        IMarker[] markers = file.findMarkers( LiferayLanguagePropertiesValidator.LIFERAY_LANGUAGE_PROPERTIES_MARKER_TYPE, false, IResource.DEPTH_ZERO );

        return markers.length > 0;
    }

    private IProject importProject( String path, String name ) throws Exception
    {
        final IPath sdkLocation = SDKManager.getInstance().getDefaultSDK().getLocation();
        final IPath hooksFolder = sdkLocation.append( path );

        final URL hookZipUrl =
            Platform.getBundle( "com.liferay.ide.project.core.tests" ).getEntry( "projects/" + name + ".zip" );

        final File hookZipFile = new File( FileLocator.toFileURL( hookZipUrl ).getFile() );

        ZipUtil.unzip( hookZipFile, hooksFolder.toFile() );

        final IPath projectFolder = hooksFolder.append( name );

        assertEquals( true, projectFolder.toFile().exists() );

        final ProjectRecord projectRecord = ProjectUtil.getProjectRecordForDir( projectFolder.toOSString() );

        assertNotNull( projectRecord );

        final IProject project =
            ProjectUtil.importProject(
                projectRecord, ServerUtil.getFacetRuntime( getRuntime() ), sdkLocation.toOSString(),
                new NullProgressMonitor() );

        assertNotNull( project );

        assertEquals( "Expected new project to exist.", true, project.exists() );

        return project;
    }

    private boolean isLanguagePropertiesFile( IFile file )
    {
        return file != null && file.exists() && PropertiesUtil.isLanguagePropertiesFile( file );
    }

    private void removeSpecifiedNode( IFile file, String nodeName, String content ) throws Exception
    {
        final IStructuredModel model = StructuredModelManager.getModelManager().getModelForEdit( file );
        final IDOMDocument document = ( (IDOMModel) model ).getDocument();

        NodeList elements = document.getElementsByTagName( nodeName );

        for( int i = 0; i < elements.getLength(); i++ )
        {
            Node node = elements.item( i );

            if( content.equals( node.getTextContent() ) )
            {
                node.getParentNode().removeChild( node );
                break;
            }
        }

        model.save( file );
        model.releaseFromEdit();
    }

    @Test
    @Ignore
    public void testHookProjectEncoding() throws Exception
    {
        final IProject hookProject = importProject( "hooks", "Hook-Encoding-Test-hook" );
        assertEquals( true, ProjectUtil.isHookProject( hookProject ) );

        final IFolder defaultDocrootFolder = CoreUtil.getDefaultDocrootFolder( hookProject );
        assertNotNull( defaultDocrootFolder );
        assertEquals( true, defaultDocrootFolder.exists() );

        final IFolder defaultSrcFolder = defaultDocrootFolder.getFolder( new Path( "WEB-INF/src/content/" ) );
        assertNotNull( defaultSrcFolder );
        assertEquals( true, defaultSrcFolder.exists() );

        final IFile fileNameWithoutUnderscore = defaultSrcFolder.getFile( "FileNameWithoutUnderscore.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore ) );

        final IFile fileNameWithoutUnderscore_CorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithoutUnderscore_CorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore_CorrectEncoding ) );

        final IFile fileNameWithoutUnderscore_IncorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithoutUnderscore_IncorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore_IncorrectEncoding ) );

        final IFile fileNameWithUnderscore_CorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithUnderscore_CorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithUnderscore_CorrectEncoding ) );

        final IFile fileNameWithUnderscore_IncorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithUnderscore_IncorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithUnderscore_IncorrectEncoding ) );

        final IFile fileNameWithStar = defaultSrcFolder.getFile( "FileNameWithStar.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithStar ) );

        final IFile fileNameWithStarCorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithStarCorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithStarCorrectEncoding ) );

        final IFile fileNameWithStarIncorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithStarIncorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithStarIncorrectEncoding ) );

        final IFile removeThisLineTest = defaultSrcFolder.getFile( "RemoveThisLineTest.properties" );
        assertEquals( true, isLanguagePropertiesFile( removeThisLineTest ) );

        // test the filename without underscore
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore ) );
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore_CorrectEncoding ) );

        validateEncoding( fileNameWithoutUnderscore_IncorrectEncoding );
        assertEquals( true, hasEncodingMarker( fileNameWithoutUnderscore_IncorrectEncoding ) );

        // test the filename with underscore
        assertEquals( false, hasEncodingMarker( fileNameWithUnderscore_CorrectEncoding ) );

        validateEncoding( fileNameWithUnderscore_IncorrectEncoding );
        assertEquals( true, hasEncodingMarker( fileNameWithUnderscore_IncorrectEncoding ) );

        // test the filename with a wildcard "*"
        assertEquals( false, hasEncodingMarker( fileNameWithStar ) );
        assertEquals( false, hasEncodingMarker( fileNameWithStarCorrectEncoding ) );

        validateEncoding( fileNameWithStarIncorrectEncoding );
        assertEquals( true, hasEncodingMarker( fileNameWithStarIncorrectEncoding ) );

        // test an incorrect encoding file referenced by liferay-hook.xml
        // remove the reference line, the marker will disappear.
        validateEncoding( removeThisLineTest );
        assertEquals( true, hasEncodingMarker( removeThisLineTest ) );

        final IFile liferayHookXml = CoreUtil.getDescriptorFile( hookProject, ILiferayConstants.LIFERAY_HOOK_XML_FILE );
        removeSpecifiedNode( liferayHookXml, "language-properties", "content/RemoveThisLineTest.properties" );

        LiferayLanguagePropertiesValidator.clearUnusedValidatorsAndMarkers( hookProject );
        assertEquals( false, hasEncodingMarker( removeThisLineTest ) );

        /*
         * Both encoding action and quick fix of the encoding marker invoke method
         * PropertiesUtils.encodeLanguagePropertiesFilesToDefault(),
         * so here we only test this method and re-check the existence of markers.
         */

        // test encoding one properties file to default, take FileNameWithoutUnderscore_IncorrectEncoding for example.
        PropertiesUtil.encodeLanguagePropertiesFilesToDefault(
            fileNameWithoutUnderscore_IncorrectEncoding, new NullProgressMonitor() );

        validateEncoding( fileNameWithoutUnderscore_IncorrectEncoding );
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore_IncorrectEncoding ) );

        // test encoding all properties files of this project to default.
        PropertiesUtil.encodeLanguagePropertiesFilesToDefault( hookProject, new NullProgressMonitor() );

        validateEncoding( fileNameWithUnderscore_IncorrectEncoding );
        assertEquals( false, hasEncodingMarker( fileNameWithUnderscore_IncorrectEncoding ) );
        validateEncoding (fileNameWithStarIncorrectEncoding );
        assertEquals( false, hasEncodingMarker( fileNameWithStarIncorrectEncoding ) );
    }

    @Test
    @Ignore
    public void testPortletProjectEncoding() throws Exception
    {
        final IProject portletProject = importProject( "portlets", "Portlet-Encoding-Test-portlet" );

        assertEquals( true, ProjectUtil.isPortletProject( portletProject ) );

        final IFolder defaultDocrootFolder = CoreUtil.getDefaultDocrootFolder( portletProject );
        assertNotNull( defaultDocrootFolder );
        assertEquals( true, defaultDocrootFolder.exists() );

        final IFolder defaultSrcFolder = defaultDocrootFolder.getFolder( new Path( "WEB-INF/src/content/" ) );
        assertNotNull( defaultSrcFolder );
        assertEquals( true, defaultSrcFolder.exists() );

        // List all the properties files used to test
        final IFile fileNameWithoutUnderscore = defaultSrcFolder.getFile( "FileNameWithoutUnderscore.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore ) );

        final IFile fileNameWithoutUnderscore_CorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithoutUnderscore_CorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore_CorrectEncoding ) );

        final IFile fileNameWithoutUnderscore_IncorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithoutUnderscore_IncorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore_IncorrectEncoding ) );

        final IFile fileNameWithoutUnderscore_IncorrectEncoding1 =
            defaultSrcFolder.getFile( "FileNameWithoutUnderscore_IncorrectEncoding1.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithoutUnderscore_IncorrectEncoding1 ) );

        final IFile fileNameWithUnderscore_CorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithUnderscore_CorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithUnderscore_CorrectEncoding ) );

        final IFile fileNameWithUnderscore_IncorrectEncoding =
            defaultSrcFolder.getFile( "FileNameWithUnderscore_IncorrectEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( fileNameWithUnderscore_IncorrectEncoding ) );

        final IFile supportedLocaleEncoding = defaultSrcFolder.getFile( "SupportedLocaleEncoding.properties" );
        assertEquals( true, isLanguagePropertiesFile( supportedLocaleEncoding ) );

        final IFile supportedLocaleEncoding_en_US =
            defaultSrcFolder.getFile( "SupportedLocaleEncoding_en_US.properties" );
        assertEquals( true, isLanguagePropertiesFile( supportedLocaleEncoding_en_US ) );

        final IFile supportedLocaleEncoding_zh_CN =
            defaultSrcFolder.getFile( "SupportedLocaleEncoding_zh_CN.properties" );
        assertEquals( true, isLanguagePropertiesFile( supportedLocaleEncoding_zh_CN ) );

        // test filename with underscore
        assertEquals( false, hasEncodingMarker( fileNameWithUnderscore_CorrectEncoding ) );

        validateEncoding( fileNameWithUnderscore_IncorrectEncoding );
        assertEquals( true, hasEncodingMarker( fileNameWithUnderscore_IncorrectEncoding ) );

        // test filename without underscore
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore ) );
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore_CorrectEncoding ) );

        validateEncoding( fileNameWithoutUnderscore_IncorrectEncoding );
        assertEquals( true, hasEncodingMarker( fileNameWithoutUnderscore_IncorrectEncoding ) );
        validateEncoding( fileNameWithoutUnderscore_IncorrectEncoding1 );
        assertEquals( true, hasEncodingMarker( fileNameWithoutUnderscore_IncorrectEncoding1 ) );

        // test supported locale
        assertEquals( false, hasEncodingMarker( supportedLocaleEncoding ) );
        assertEquals( false, hasEncodingMarker( supportedLocaleEncoding_en_US ) );
        assertEquals( false, hasEncodingMarker( supportedLocaleEncoding_zh_CN ) );

        // test encoding one file to default, take FileNameWithUnderscore_IncorrectEncoding.properties for example.
        PropertiesUtil.encodeLanguagePropertiesFilesToDefault(
            fileNameWithUnderscore_IncorrectEncoding, new NullProgressMonitor() );

        validateEncoding( fileNameWithUnderscore_IncorrectEncoding );
        assertEquals( false, hasEncodingMarker( fileNameWithUnderscore_IncorrectEncoding ) );

        // test encoding all files of this project to default
        PropertiesUtil.encodeLanguagePropertiesFilesToDefault( portletProject, new NullProgressMonitor() );

        validateEncoding( fileNameWithoutUnderscore_IncorrectEncoding );
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore_IncorrectEncoding ) );
        validateEncoding( fileNameWithoutUnderscore_IncorrectEncoding1 );
        assertEquals( false, hasEncodingMarker( fileNameWithoutUnderscore_IncorrectEncoding1 ) );
    }

    private void validateEncoding( IFile file )
    {
        LiferayLanguagePropertiesValidator.getValidator( file ).validateEncoding();
    }

}
