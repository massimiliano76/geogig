/* Copyright (c) 2013-2016 Boundless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 * Gabriel Roldan (Boundless) - initial implementation
 */
package org.locationtech.geogig.geotools.cli.postgis;

import java.util.Optional;
import java.util.function.Function;

import org.geotools.data.DataStore;
import org.geotools.feature.ValidatingFeatureFactoryImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.factory.Hints;
import org.locationtech.geogig.cli.CLICommand;
import org.locationtech.geogig.cli.annotation.ReadOnly;
import org.locationtech.geogig.geotools.cli.base.DataStoreExport;
import org.locationtech.geogig.geotools.plumbing.ExportOp;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * Exports features from a feature type into a PostGIS database.
 * 
 * @see ExportOp
 */
@ReadOnly
@Command(name = "export", description = "Export to PostGIS")
public class PGExport extends DataStoreExport implements CLICommand {

    public @ParentCommand PGCommandProxy commonArgs;

    final PGSupport support = new PGSupport();

    protected @Override DataStore getDataStore() {
        return support.getDataStore(commonArgs);
    }

    /**
     * Transforms all features to use a feature id that is compatible with postgres.
     * 
     * @param featureType the feature type of the features to transform
     * @return the transforming function
     */
    protected @Override Function<Feature, Optional<Feature>> getTransformingFunction(
            final SimpleFeatureType featureType) {
        Function<Feature, Optional<Feature>> function = (feature) -> {
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType,
                    new ValidatingFeatureFactoryImpl());
            builder.init((SimpleFeature) feature);
            String fid = feature.getIdentifier().getID();
            String fidPrefix = feature.getType().getName().getLocalPart();
            if (fid.startsWith(fidPrefix)) {
                fid = fid.substring(fidPrefix.length() + 1);
            }
            builder.featureUserData(Hints.PROVIDED_FID, fid);
            Feature modifiedFeature = builder.buildFeature(fid);
            return Optional.ofNullable(modifiedFeature);
        };

        return function;
    }
}
