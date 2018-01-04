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

package uk.gov.gchq.gaffer.named.view;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.exception.CloneFailedException;

import uk.gov.gchq.gaffer.commonutil.CommonConstants;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.data.elementdefinition.view.ViewParameterDetail;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.gaffer.operation.Operation;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * A {@code AddNamedView} is an {@link Operation} for adding a {@link uk.gov.gchq.gaffer.data.elementdefinition.view.NamedView}
 * to a Gaffer graph.
 */
public class AddNamedView implements Operation {
    private static final String CHARSET_NAME = CommonConstants.UTF_8;
    private String name;
    private String view;
    private String description;
    private Map<String, ViewParameterDetail> parameters;
    private boolean overwriteFlag = false;
    private Map<String, String> options;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setView(final String view) {
        this.view = view;
    }

    @JsonSetter("view")
    public void setView(final View namedView) {
        this.view = new String(namedView.toCompactJson());
    }

    public View getView() {
        try {
            return JSONSerialiser.deserialise(view.getBytes(CHARSET_NAME), View.class);
        } catch (final UnsupportedEncodingException | SerialisationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setParameters(final Map<String, ViewParameterDetail> parameters) {
        this.parameters = parameters;
    }

    public Map<String, ViewParameterDetail> getParameters() {
        return parameters;
    }

    public void setOverwriteFlag(final boolean overwriteFlag) {
        this.overwriteFlag = overwriteFlag;
    }

    public boolean isOverwriteFlag() {
        return overwriteFlag;
    }

    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public AddNamedView shallowClone() throws CloneFailedException {
        return new AddNamedView.Builder()
                .name(name)
                .view(view)
                .description(description)
                .parameters(parameters)
                .overwrite(overwriteFlag)
                .options(options)
                .build();
    }

    public static class Builder extends BaseBuilder<AddNamedView, Builder> {
        public Builder() {
            super(new AddNamedView());
        }

        public Builder name(final String name) {
            _getOp().setName(name);
            return _self();
        }

        public Builder view(final String view) {
            _getOp().setView(view);
            return _self();
        }

        public Builder view(final View view) {
            _getOp().setView(view);
            return _self();
        }

        public Builder description(final String description) {
            _getOp().setDescription(description);
            return _self();
        }

        public Builder parameters(final Map<String, ViewParameterDetail> parameters) {
            _getOp().setParameters(parameters);
            return _self();
        }

        public Builder overwrite(final boolean overwriteFlag) {
            _getOp().setOverwriteFlag(overwriteFlag);
            return _self();
        }
    }
}