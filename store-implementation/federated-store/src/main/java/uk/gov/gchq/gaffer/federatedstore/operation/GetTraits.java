/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.federatedstore.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.exception.CloneFailedException;

import uk.gov.gchq.gaffer.operation.io.Output;
import uk.gov.gchq.gaffer.store.StoreTrait;

import java.util.Map;

import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreConstants.KEY_OPERATION_OPTIONS_GRAPH_IDS;

/**
 * An Operation used for getting traits from the FederatedStore.
 */
public class GetTraits implements FederatedOperation, Output<Iterable<? extends StoreTrait>> {

    public final boolean defaultIsSupportedTraits = false;
    private boolean isSupportedTraits = defaultIsSupportedTraits;
    private Map<String, String> options;

    public GetTraits() {
        addOption(KEY_OPERATION_OPTIONS_GRAPH_IDS, null);
    }

    public boolean getIsSupportedTraits() {
        return isSupportedTraits;
    }

    public void setIsSupportedTraits(final boolean isSupportedTraits) {
        this.isSupportedTraits = isSupportedTraits;
    }

    @Override
    public GetTraits shallowClone() throws CloneFailedException {
        return new Builder()
                .options(options)
                .isSupportedTraits(isSupportedTraits)
                .build();
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    @Override
    public TypeReference<Iterable<? extends StoreTrait>> getOutputTypeReference() {
        return new IterableStoreTrait();
    }

    public static class Builder extends BaseBuilder<GetTraits, Builder> {
        public Builder() {
            super(new GetTraits());
        }

        public Builder isSupportedTraits(final boolean isSupportedTraits) {
            _getOp().setIsSupportedTraits(isSupportedTraits);
            return _self();
        }
    }

    public static class IterableStoreTrait extends TypeReference<Iterable<? extends StoreTrait>> {
    }
}