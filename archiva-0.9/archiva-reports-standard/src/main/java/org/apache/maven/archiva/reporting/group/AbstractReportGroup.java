package org.apache.maven.archiva.reporting.group;

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

import org.apache.maven.archiva.reporting.processor.ArtifactReportProcessor;
import org.apache.maven.archiva.reporting.processor.MetadataReportProcessor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.model.Model;

import java.util.Iterator;
import java.util.Map;

/**
 * Basic functionality for all report groups.
 */
public abstract class AbstractReportGroup
    implements ReportGroup
{
    /**
     * @plexus.requirement role="org.apache.maven.archiva.reporting.processor.ArtifactReportProcessor"
     */
    private Map artifactReports;

    /**
     * @plexus.requirement role="org.apache.maven.archiva.reporting.processor.MetadataReportProcessor"
     */
    private Map metadataReports;

    public void processArtifact( Artifact artifact, Model model )
    {
        for ( Iterator i = artifactReports.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();

            if ( includeReport( (String) entry.getKey() ) )
            {
                ArtifactReportProcessor report = (ArtifactReportProcessor) entry.getValue();

                report.processArtifact( artifact, model );
            }
        }
    }

    public void processMetadata( RepositoryMetadata repositoryMetadata, ArtifactRepository repository )
    {
        for ( Iterator i = metadataReports.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();

            if ( includeReport( (String) entry.getKey() ) )
            {
                MetadataReportProcessor report = (MetadataReportProcessor) entry.getValue();

                report.processMetadata( repositoryMetadata, repository );
            }
        }
    }

    public String toString()
    {
        return getName();
    }
}
