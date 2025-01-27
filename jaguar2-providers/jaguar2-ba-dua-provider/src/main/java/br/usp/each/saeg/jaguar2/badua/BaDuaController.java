/**
 * Copyright (c) 2021, 2021 University of Sao Paulo and Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roberto Araujo - initial API and implementation and/or initial documentation
 */
package br.usp.each.saeg.jaguar2.badua;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import br.usp.each.saeg.badua.agent.rt.BaDuaRuntime;
import br.usp.each.saeg.badua.agent.rt.IAgent;
import br.usp.each.saeg.badua.core.data.ExecutionDataReader;
import br.usp.each.saeg.badua.core.data.ExecutionDataStore;
import br.usp.each.saeg.jaguar2.api.IBundleSpectrum;
import br.usp.each.saeg.jaguar2.api.IPackageSpectrum;
import br.usp.each.saeg.jaguar2.spi.CoverageController;

/**
 * A {@link CoverageController} service implementation that interacts
 * with BA-DUA.
 */
public class BaDuaController implements CoverageController {

    private final IAgent agent;

    private final List<ExecutionDataStore> failExecutionDataStores;

    private final List<ExecutionDataStore> successExecutionDataStores;

    /**
     * Instantiate a {@link BaDuaController} for a given agent instance of
     * the BA-DUA runtime.
     *
     * @param agent an {@link IAgent} that allow interaction with BA-DUA
     *              agent runtime.
     */
    public BaDuaController(final IAgent agent) {
        this.agent = agent;
        failExecutionDataStores = new LinkedList<ExecutionDataStore>();
        successExecutionDataStores = new LinkedList<ExecutionDataStore>();
    }

    /**
     * Instantiate a {@link BaDuaController} for the default agent
     * instance of the BA-DUA runtime in this JVM.
     */
    public BaDuaController() {
        this(BaDuaRuntime.getAgent());
    }

    @Override
    public void init() {
    }

    @Override
    public void reset() {
        agent.reset();
    }

    @Override
    public void save(final boolean testFailed) {
        /*
         * BA-DUA's execution data.
         *
         * Don't reset as it will be done before next test start.
         */
        final byte[] executionData = agent.getExecutionData(false);

        /*
         * Instantiate a reader that handles BA-DUA's internal binary format.
         */
        final ExecutionDataReader reader = new ExecutionDataReader(
                new ByteArrayInputStream(executionData));

        /*
         * Save execution data store we will save for further analysis.
         */
        final ExecutionDataStore executionDataStore = new ExecutionDataStore();
        reader.setExecutionDataVisitor(executionDataStore);

        try {
            reader.read();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (testFailed) {
            failExecutionDataStores.add(executionDataStore);
        } else {
            successExecutionDataStores.add(executionDataStore);
        }
    }

    @Override
    public IBundleSpectrum analyze() {
        return new IBundleSpectrum() {
            @Override
            public Collection<IPackageSpectrum> getPackages() {
                return Collections.emptyList();
            }
        };
    }

    @Override
    public void destroy() {
        failExecutionDataStores.clear();
        successExecutionDataStores.clear();
    }

}
